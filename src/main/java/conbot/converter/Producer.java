package conbot.converter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.zip.GZIPInputStream;

import conbot.utils.FileUtils;

public class Producer implements Runnable {

	private List<Consumer> consumers;
	Consumer[] consumers_array;
	public boolean isComplete = false;
	File sourceFile;
	public int row = 0;
	int totalRowsToProcess = -1;
	public int readBufferSize;
	boolean readInMemory = false;
	Context context;
	int maxQ;
	String encoding;

	public Producer() {
	}

	public void run() {
		BufferedReader reader = null;
		consumers_array = new Consumer[getConsumers().size()];
		for (int index = 0; index < getConsumers().size(); index++) {
			consumers_array[index] = getConsumers().get(index);
		}
		try {
			// UTF-8 faster
			// threads 3, buffer 131072 -
			// threads 3, buffer 131072 -
			// 236k read windows :
			// 183k read macOS Intel :
			// ???? read macOS M1 :
			
			if  (isReadInMemory()) {
				byte[] data = FileUtils.readFileToBytes(getSourceFile());
				ByteArrayInputStream bis = new ByteArrayInputStream(data);
				GZIPInputStream in = new GZIPInputStream(bis, getReadBufferSize());
				reader = new BufferedReader(new InputStreamReader(in, getEncoding()), getReadBufferSize());
			} else {
				GZIPInputStream in = new GZIPInputStream(new FileInputStream(getSourceFile()), getReadBufferSize());
				reader = new BufferedReader(new InputStreamReader(in, getEncoding()), getReadBufferSize());
			}

			int index = 0;
			row = 0;

			if (context.skipHeader) {
				reader.readLine();
			}

			String line = reader.readLine();
			Consumer consumer = null;
			long startTime = System.currentTimeMillis();
			while (line != null) {
				consumer = consumers_array[index];
				Record r = new Record(line,row);
				consumer.queue.add(r);
				line = reader.readLine();
				index += 1;
				index = index % consumers_array.length;
				row += 1;

				// bit of defence as if the overall queue size gets too large
				// we GC and stop the world
				// this comes at low-to-no hit
				if (row % 1000 == 0 ) {
					try {
						long queueSize = getTotalQueueSize();
						while (getTotalQueueSize() > getMaxQ()) {
							context.report.out("Producer: Sleeping. ( queue size is " + queueSize + ", maxQ is " + getMaxQ());
							// stop reading for a while
							Thread.sleep(10);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

			}
			context.report.metric("rows", row);
			context.report.metric("total_read_time_ms", System.currentTimeMillis()-startTime);
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			FileUtils.close(reader);
			isComplete = true;
		}
		
		
	}

	public long getTotalQueueSize() {
		long current_q = 0;
		for (int index=0; index<consumers_array.length; index++) {
			current_q += consumers_array[index].queue.size();
		}
		return current_q;
	}

	public List<Consumer> getConsumers() {
		return consumers;
	}

	public void setConsumers(List<Consumer> consumers) {
		this.consumers = consumers;
	}

	public File getSourceFile() {
		return sourceFile;
	}

	public void setSourceFile(File file) {
		this.sourceFile = file;
	}

	public int getTotalRowsToProcess() {
		return totalRowsToProcess;
	}

	public void setTotalRowsToProcess(int maxRows) {
		this.totalRowsToProcess = maxRows;
	}

	public int getReadBufferSize() {
		return readBufferSize;
	}

	public void setReadBufferSize(int readBufferSize) {
		this.readBufferSize = readBufferSize;
	}

	public boolean isReadInMemory() {
		return readInMemory;
	}

	public void setReadInMemory(boolean readInMemory) {
		this.readInMemory = readInMemory;
	}

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public int getMaxQ() {
		return maxQ;
	}

	public void setMaxQ(int maxq) {
		this.maxQ = maxq;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public String getEncoding() {
		return encoding;
	}


}
