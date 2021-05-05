package conbot.converter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.zip.GZIPInputStream;

import conbot.converter.csv.ConsumerCSV;
import conbot.converter.empty.ConsumerEmpty;
import conbot.utils.BQLoader;
import conbot.utils.CLI;
import conbot.utils.EXIT_CODES;
import conbot.utils.CloudStorage;
import conbot.utils.FileUtils;
import conbot.utils.Report;
import conbot.utils.StringUtils;

public class Converter {

	CLI cli; // options passed via terminal
	Report report; // the report we will upload

	String source;
	String target;
	File dir;

	File sourceFile; // source file to convert
	String sourceFileName; // source file to convert

	int threads; // the number of threads in the pool
	ThreadPoolExecutor threadPool;
	List<Consumer> consumers; // the list of consumers that will do the work

	Producer producer; // the reader
	int totalRowsInFile; // total number of rows in the file (allows ETA reporting)
	int totalRowsToProcess; // total number of rows to read
	int reportEveryMS; // delay between reporting in millis
	int maxQ;
	Context context; // the shared state between producer/consumers

	long timeStarted = System.currentTimeMillis();
	long timeLastSplit = System.currentTimeMillis();
	long timeThisSplit = System.currentTimeMillis();

	public static void main(String[] args) throws Exception {
		CLI cli = new CLI(args);
		Converter app = new Converter(cli);
		if (cli.args.length == 0) {
			app.usage();
		} else {
			app.execute();
		}
	}

	public static void help() {
		CLI.out("convert         reads and converts a gzip, quickly");
		CLI.out("Required parameters");
		CLI.out("");
		CLI.out(" -source      FILE          file gzip file to convert");
		CLI.out(" -target      PATH          the path to store the converted files");
		CLI.out(" -consumer    class.Name    worker implementation class (e.g. 'csv, 'empty')");
		CLI.out("");
		CLI.out("Optional parameters");
		CLI.out("");
		CLI.out(" -threads     N             set number of workers");
		CLI.out(" -readBuffer  N             read buffer size in k (default 8)");
		CLI.out(" -writeBuffer N             write buffer size in k (default 8)");
		CLI.out(" -dir         DIR           the directory to store data during conversion");
		CLI.out("");
		CLI.out(" -rows        N             process only N rows");
		CLI.out(" -rowcount    N             number of rows in source data (for ETA reporting)");
		CLI.out(" -report      N             report every N milliseconds on progress (default 1000)");
		CLI.out(" -maxq        N             maximum allowed queue size before the reader will pause");
		CLI.out("");
	}

	public Converter(CLI cli) throws Exception {
		this.cli = cli;
		if (cli.contains("-help") || cli.contains("-h") || cli.args.length < 3) {
			help();
			System.exit(1);
		}
		boolean quiet = cli.contains("-quiet");
		context = new Context();
		report = new Report(!quiet);
		context.report = report;

		this.source = cli.getStringOrDie("-source");
		this.target = cli.getStringOrDie("-target");

		initialiseFiles();

		cli.getStringOrDie("-consumer");

		this.context.sourceFile = sourceFile;
		this.totalRowsInFile = cli.getIntOrDefault("-rowcount", -1);
		this.totalRowsToProcess = cli.getIntOrDefault("-rows", -1);
		if (totalRowsToProcess == -1) {
			totalRowsToProcess = totalRowsInFile;
		}
		this.maxQ = cli.getIntOrDefault("-maxq", 1000000);
		this.reportEveryMS = cli.getIntOrDefault("-report", 1000);
		this.consumers = buildConsumers(cli);
		this.producer = buildProducer(cli);
	}

	public Producer buildProducer(CLI cli) throws Exception {
		int readBufferSize = 1024 * cli.getIntOrDefault("-readBuffer", 8);
		boolean readInMemory = cli.contains("-inMemory");

		String defaultClassName = Producer.class.getName();
		String className = cli.getStringOrDefault("-producer", defaultClassName);
		String encoding = cli.getStringOrDefault("-encoding", "UTF-8");
		Producer p = (Producer) Class.forName(className).newInstance();
		p.setContext(this.context);
		p.setEncoding(encoding);
		p.setSourceFile(this.sourceFile);
		p.setConsumers(this.consumers);
		p.setTotalRowsToProcess(totalRowsToProcess);
		p.setReadBufferSize(readBufferSize);
		p.setReadInMemory(readInMemory);
		p.setMaxQ(maxQ);
		return p;
	}

	public List<Consumer> buildConsumers(CLI cli) throws Exception {
		int writeBufferSize = 1024 * cli.getIntOrDefault("-writeBuffer", 8);
		int maxAllowedConsumers = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
		int numberOfConsumers = cli.getIntOrDefault("-threads", maxAllowedConsumers); // number of workers to run on
		List<Consumer> consumers = new ArrayList<Consumer>();

		if (numberOfConsumers < 1 || numberOfConsumers > maxAllowedConsumers) {
			numberOfConsumers = maxAllowedConsumers;
		}
		report.out("Using " + numberOfConsumers + " threads (max available is " + maxAllowedConsumers + ").");
		report.out("Using max queue size " + maxQ);
		threads = numberOfConsumers + 1;
		threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(threads);
		for (int i = 0; i < numberOfConsumers; i++) {
			String className = cli.getStringOrDie("-consumer");
			Consumer consumer = null;
			if (className.equalsIgnoreCase("csv")) {
				consumer = new ConsumerCSV();
			} else if (className.equalsIgnoreCase("empty")) {
				consumer = new ConsumerEmpty();
			} else {
				consumer = (Consumer) Class.forName(className).newInstance();
			}
			consumer.setup(cli, dir, context, i, writeBufferSize);
			consumers.add(consumer);
		}

		return consumers;
	}

	public void usage() {
		CLI.out("convert: usage");
	}

	// creates the working directory, source file reference, output directory
	// if necessary copies the source file from (gs/s3)
	// creates the report
	void initialiseFiles() {

		if (CloudStorage.isProtocolFile(target)) {
			dir = FileUtils.resolveFile(target);
		} else {
			// it is a cloud final target; for this reason we need a root dir
			dir = FileUtils.resolveFile(cli.getStringOrDefault("-dir", UUID.randomUUID().toString()));
		}
		dir.mkdirs();

		// if source file is local it must exist
		if (CloudStorage.isProtocolFile(source)) {
			// otherwise it is local and we need to ensure it exists, or die.
			sourceFile = FileUtils.resolveFile(source);
			if (!sourceFile.isFile() || !sourceFile.exists()) {
				// then the source file does not exist.
				String msg = "Error, Source file '" + source + "' does not exist.";
//				report.out(msg);
				CLI.die(msg, EXIT_CODES.SOURCE_FILE_DOES_NOT_EXIST);

			} else {
				// looks like a file but let's see if we really can read it before we kick it
				// all off
				GZIPInputStream in = null;
				try {
					in = new GZIPInputStream(new FileInputStream(sourceFile));
				} catch (Exception e) {
					CLI.die(e, EXIT_CODES.SOURCE_FILE_NOT_GZIP);
				}
				try {
					BufferedReader reader = new BufferedReader(new InputStreamReader(in));
					reader.readLine();
				} catch (Exception e) {
					CLI.die(e, EXIT_CODES.SOURCE_FILE_CANNOT_BE_READ);
				}
				try {
					in.close();
				} catch (Exception e) {

				}

			}
		} else {
			// if it is not local, work out what the local file 'will' be
			String filename = FileUtils.getFilename(source);
			sourceFile = new File(dir + filename);

		}

		if (!CloudStorage.isProtocolFile(source)) {
			// if it is not local, work out what the local file 'will' be
			String filename = FileUtils.getFilename(source);
			sourceFile = new File(dir + "/" + filename);
			sourceFile.getParentFile().mkdirs();

		} else {
			// otherwise it is local and we need to ensure it exists, or die.
			sourceFile = FileUtils.resolveFile(source);
			if (!sourceFile.isFile() || !sourceFile.exists()) {
				// then the source file does not exist.
				String msg = "Source file is local but '" + source + "' does not exist.";
				report.out(msg);
				CLI.die(msg, EXIT_CODES.SOURCE_FILE_DOES_NOT_EXIST);
			} else {
				report.out("Source file is local and exists.");
			}
		}

	}

	void copySourceFile() {
		if (!CloudStorage.isProtocolFile(source)) {
			// then the source file is not local - we need to copy it
			report.out("Source file '" + source + "' is remote, copying to local storage: '" + sourceFile + "'.");
			CloudStorage copier = new CloudStorage();
			try {
				long startCopy = System.currentTimeMillis();
				report.metric("copy_source_start", startCopy);
				copier.copy(source, sourceFile);
				long endCopy = System.currentTimeMillis();
				report.metric("copy_source_end", endCopy);
				report.metric("copy_source_duration", endCopy - startCopy);
				long sinceLast = report.timeSinceLast();
				report.out("Copy success in " + sinceLast + "ms.");
			} catch (Exception e) {
				long sinceLast = report.timeSinceLast();
				report.out("Copy failed in " + sinceLast + "ms.", e);
				CLI.die(e, EXIT_CODES.SOURCE_FILE_COPY_EXCEPTION);
			}
		} else {
			sourceFile = FileUtils.resolveFile(source);
			if (!sourceFile.isFile()) {
				// then the source file does not exist.
				String msg = "Source file is local but '" + source + "' does not exist.";
				report.out(msg);
				CLI.die(msg, EXIT_CODES.SOURCE_FILE_DOES_NOT_EXIST);
			} else {
				report.out("Source file is local and exists.");
			}
		}
		report.metric("source_file_size", sourceFile.length());

	}

	void copyReportFile(File reportFile, String target) throws Exception {
		if (!CloudStorage.isProtocolFile(target)) {
			CloudStorage copier = new CloudStorage();
			copier.copy(reportFile, target);
		}
	}

	void copyTargetFilesToCloud() {
		if (!CloudStorage.isProtocolFile(target)) {
			// then the source file is not local - we need to copy it
			report.out("Target '" + target + "' is remote, copying local files to target.");
			long overallCopyStart = System.currentTimeMillis();
			report.metric("copy_target_start", overallCopyStart);
			CloudStorage copier = new CloudStorage();
			try {
				report.fact("copy_file_count", consumers.size());
				for (int index = 0; index < consumers.size(); index++) {
					Consumer consumer = consumers.get(index);
					long copyStart = System.currentTimeMillis();
					String prefix = "copy_file_" + (index + 1) + "_of_" + consumers.size() + "_";

					report.fact(prefix + "name", consumer.file.getName());
					report.fact(prefix + "size", consumer.file.length());

					report.metric(prefix + "start", copyStart);
					copier.copy(consumer.file, target);
					long copyEnd = System.currentTimeMillis();
					report.metric(prefix + "end", copyEnd);
					report.metric(prefix + "duration", copyEnd - copyStart);
				}
				long overallCopyEnd = System.currentTimeMillis();
				report.metric("copy_target_end", overallCopyEnd);
				report.metric("copy_target_duration", overallCopyEnd - overallCopyStart);

				long sinceLast = report.timeSinceLast();
				report.out("Copy success in " + sinceLast + "ms.");
			} catch (Exception e) {
				long sinceLast = report.timeSinceLast();
				report.out("Copy failed in " + sinceLast + "ms.", e);
				CLI.die(e, EXIT_CODES.SOURCE_FILE_COPY_EXCEPTION);
			}
		} else {
			sourceFile = FileUtils.resolveFile(source);
			if (!sourceFile.isFile()) {
				// then the source file does not exist.
				String msg = "Source file is local but '" + source + "' does not exist.";
				report.out(msg);
				CLI.die(msg, EXIT_CODES.SOURCE_FILE_DOES_NOT_EXIST);
			} else {
				report.out("Source file is local and exists.");
			}
		}

	}

	void copyTargetFilesToBQ() {
		if (!CloudStorage.isProtocolFile(target)) {
			// then the source file is not local - we need to copy it
			report.out("Target '" + target + "' is remote, copying local files to target.");
			long overallCopyStart = System.currentTimeMillis();
			report.metric("copy_target_start", overallCopyStart);
			CloudStorage copier = new CloudStorage();
			try {
				report.fact("copy_file_count", consumers.size());
				for (int index = 0; index < consumers.size(); index++) {
					Consumer consumer = consumers.get(index);
					long copyStart = System.currentTimeMillis();
					String prefix = "copy_file_" + (index + 1) + "_of_" + consumers.size() + "_";

					report.fact(prefix + "name", consumer.file.getName());
					report.fact(prefix + "size", consumer.file.length());

					report.metric(prefix + "start", copyStart);
					copier.copy(consumer.file, target);
					long copyEnd = System.currentTimeMillis();
					report.metric(prefix + "end", copyEnd);
					report.metric(prefix + "duration", copyEnd - copyStart);
				}
				long overallCopyEnd = System.currentTimeMillis();
				report.metric("copy_target_end", overallCopyEnd);
				report.metric("copy_target_duration", overallCopyEnd - overallCopyStart);

				long sinceLast = report.timeSinceLast();
				report.out("Copy success in " + sinceLast + "ms.");
			} catch (Exception e) {
				long sinceLast = report.timeSinceLast();
				report.out("Copy failed in " + sinceLast + "ms.", e);
				CLI.die(e, EXIT_CODES.SOURCE_FILE_COPY_EXCEPTION);
			}
		} else {
			sourceFile = FileUtils.resolveFile(source);
			if (!sourceFile.isFile()) {
				// then the source file does not exist.
				String msg = "Source file is local but '" + source + "' does not exist.";
				report.out(msg);
				CLI.die(msg, EXIT_CODES.SOURCE_FILE_DOES_NOT_EXIST);
			} else {
				report.out("Source file is local and exists.");
			}
		}

	}

	public void execute() {

		timeStarted = System.currentTimeMillis();
		report.metric("start", timeStarted);

		copySourceFile();

		// start the workers
		for (Consumer worker : consumers) {
			threadPool.execute(worker);
		}
		// start pushing work into each one
		threadPool.execute(producer);

		// wait until they are complete
		boolean quit = false;
		while (!quit) {
			if (producer.isComplete) {
				// default to quitting as we think all work is complete
				quit = true;
				for (Consumer worker : consumers) {

					if (worker.quit == false) {
						if (worker.queue.size() > 0 || !worker.isIdle) {
							// the worker still has work to do
							quit = false;
							break;
						} else {
							// the worker does not have work to do
							worker.quit = true;
							try {
								worker.close();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
			}

			if (!quit) {
				try {
					Thread.sleep(reportEveryMS);
					report();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		// every consumer has completed
		// every producer has completed
		// now we may need to copy the files to some remote storage
		threadPool.shutdown();

		if (CloudStorage.isProtocolBigQuery(target)) {
			// BQ copy.. erk
			copyTargetFilesToBQ();
		} else if (!CloudStorage.isProtocolFile(target)) {
			// we need to take our output files and the report and copy them someplace.
			report.out("Need to copy output files to '" + target + "'");
			copyTargetFilesToCloud();
		}

		long rowsWrittenPerSecond = finalReport();
		long end = System.currentTimeMillis();
		report.metric("end", end);
		report.metric("duration", end - timeStarted);
		report.metric("rows_per_second", rowsWrittenPerSecond);
		report.config("source", source);
		report.config("target", target);
		report.config("consumer", cli.getStringOrDefault("-consumer", ""));
		report.config("threads", threads);

		File reportFile = new File(dir + "/report.json");
		try {
			report.save(reportFile);

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(EXIT_CODES.FAILED_TO_WRITE_REPORT_FILE);
		}

		if (!CloudStorage.isProtocolFile(target)) {
			try {
				copyReportFile(reportFile, target);
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(EXIT_CODES.FAILED_TO_UPLOAD_REPORT_FILE);
			}

		}

	}

	long finalReport() {

		timeThisSplit = System.currentTimeMillis();
		int totalSuccess = 0;
		int totalFails = 0;
		int totalProcessed = 0;
		long queueSize = 0;
		for (Consumer worker : consumers) {
			totalSuccess += worker.total_success;
			totalFails += worker.total_fails;
			totalProcessed = totalSuccess + totalFails;
			queueSize += worker.queue.size();
		}
		long duration = timeThisSplit - timeStarted;
		float rowsReadPerMS = this.producer.row / duration;
		float rowsWrittenPerMS = totalProcessed / duration;
		long rowsWrittenPerS = (int) (rowsWrittenPerMS * 1000);
		long rowsReadPerS = (int) (rowsReadPerMS * 1000);
//		String actualWritten = StringUtils.friendlyTime((timeThisSplit - timeStarted));
//		String actualRead = StringUtils.friendlyTime((timeThisSplit - timeStarted));
		report.out(rowsWrittenPerS + " write/s, " + rowsReadPerS + " read/s.");
		return rowsWrittenPerS;
	}

	void report() {
		timeThisSplit = System.currentTimeMillis();
		int totalSuccess = 0;
		int totalFails = 0;
		int totalProcessed = 0;
		int totalRead = producer.row;
		long queueSize = 0;
		for (Consumer worker : consumers) {
			totalSuccess += worker.total_success;
			totalFails += worker.total_fails;
			totalProcessed = totalSuccess + totalFails;
			queueSize += worker.queue.size();
		}

		long duration = timeThisSplit - timeStarted;
		float rowsWrittenPerMS = totalProcessed / duration;
		long rowsWrittenPerS = (int) (rowsWrittenPerMS * 1000);

		float rowsReadPerMS = totalRead / duration;
		long rowsReadPerS = (int) (rowsReadPerMS * 1000);

		// String actual = StringUtils.friendlyTime(duration);
		String actual = duration / 1000 + "s";

		if (totalRowsInFile > -1) {
			float pct = 100f / (float) totalRowsInFile;
			float pctSoFar = pct * totalProcessed;
			long etaSeconds = -1;
			if (rowsWrittenPerS != 0) {
				etaSeconds = (int) totalRowsToProcess / rowsWrittenPerS;
			}

			String eta = StringUtils.friendlyTime(etaSeconds);

			report.out(pctSoFar + "%, " + totalProcessed + "/" + totalRowsToProcess + " (" + totalFails + " fails.) ("
					+ rowsWrittenPerS + "row writes/second, " + rowsReadPerS + " row reads/second), eta " + eta
					+ ", actual " + actual + ", queue size " + queueSize + ", map size " + context.map.size());
		} else {
			report.out(totalProcessed + " (" + totalFails + " fails.) (" + rowsWrittenPerS + "row writes/second, "
					+ rowsReadPerS + " row reads/second), actual " + actual + ", queue size " + queueSize
					+ ", map size " + context.map.size());

		}
		timeLastSplit = timeThisSplit;
	}

}
