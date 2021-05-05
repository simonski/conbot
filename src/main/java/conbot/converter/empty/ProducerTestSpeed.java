package conbot.converter.empty;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.zip.GZIPInputStream;

import conbot.converter.Producer;
import conbot.converter.Record;
import conbot.utils.FileUtils;

/**
 * This Producer reads rows as fast as
 *
 */
public class ProducerTestSpeed extends Producer {

	public ProducerTestSpeed() {
		super();
	}

	public void run() {
		BufferedReader reader = null;
		try {
			// UTF-8 faster
			// threads 3, buffer 131072 -
			// threads 3, buffer 131072 -
			// 236k read windows :
			// 183k read macOS Intel :
			// ???? read macOS M1 :

			if (isReadInMemory()) {
				byte[] data = FileUtils.readFileToBytes(getSourceFile());
				ByteArrayInputStream bis = new ByteArrayInputStream(data);
				GZIPInputStream in = new GZIPInputStream(bis, readBufferSize);
				reader = new BufferedReader(new InputStreamReader(in, getEncoding()), readBufferSize);
			} else {
				FileInputStream fis = new FileInputStream(getSourceFile());
				GZIPInputStream gis = new GZIPInputStream(fis, readBufferSize);
				Reader rreader = new InputStreamReader(gis, getEncoding());
				reader = new BufferedReader(rreader, readBufferSize);
			}

			row = 0;
			String line = reader.readLine();
			while (line != null) {
				Record r = new Record(line, row);
				line = reader.readLine();
				row += 1;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			FileUtils.close(reader);
			isComplete = true;
		}
	}

	public long getTotalQueueSize() {
		return 0;
	}
}
