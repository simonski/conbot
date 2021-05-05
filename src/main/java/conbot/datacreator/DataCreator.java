package conbot.datacreator;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

import conbot.datacreator.Schema.Column;
import conbot.utils.CLI;
import conbot.utils.RandomData;

/**
 * Helper class acts as entry point to various test utilities
 *
 */
public class DataCreator {

	/**
	 * create random data, optionally from a schema
	 */
	static final String DELIMITER = ",";
	final long ONE_MINUTE = 1000 * 60;
	final long ONE_HOUR = ONE_MINUTE * 60;
	final long ONE_DAY = ONE_HOUR * 24;
	final long ONE_YEAR = ONE_DAY * 365;

	Random random;
	RandomData randomData;

	public DataCreator() {
	}

	interface Writer {
		public void write(String line) throws IOException;

		public void write(byte[] data, int length) throws IOException;

		public void close() throws IOException;

		public void flush() throws IOException;
	}

	public class ConsoleWriter implements Writer {
		public void write(String msg) {
			System.out.println(msg);
		}

		public void close() {
		}

		public void flush() {
		}

		public void write(byte[] data, int length) throws IOException {
		}
	}

	public static void help() {
		CLI.out("data         creates sample data");
		CLI.out("Optional parameters");
		CLI.out("");
		CLI.out("-schema");
		CLI.out("-rows");
		CLI.out("-schemafactory");
		CLI.out("-seed");
		CLI.out("-optional");
		CLI.out("-sparsity");
		CLI.out("-cols");
	}

	public void execute(CLI cli) throws Exception {
		if (cli.contains("-help") || cli.contains("-h") || cli.args.length < 3) {
			help();
		}
		File schemaFile = cli.getFileOrDefault("-schema", null);
		int max_rows = cli.getIntOrDefault("-rows", -1);

		String schemaFactoryClassname = cli.getStringOrDefault("-schemafactory", SchemaFactory.class.getName());
		SchemaFactory schemaFactory = (SchemaFactory) Class.forName(schemaFactoryClassname).newInstance();
		Schema schema = null;
		int seed = cli.getIntOrDefault("-seed", SchemaCreator.DEFAULT_SEED);
		random = new Random(seed);
		randomData = new RandomData();
		float optionalProbability = cli.getFloatOrDefault("-optional", random.nextFloat()); // the seed governs
		float sparsityProbability = cli.getFloatOrDefault("-sparsity", random.nextFloat()); // the seed governs

		if (schemaFile == null) {
			int cols = cli.getIntOrDefault("-cols", 80);
			schema = schemaFactory.buildRandom(cols, seed, optionalProbability);
		} else {
			schema = schemaFactory.build(schemaFile);
		}
		Writer writer = new ConsoleWriter();

		int row = 0;
		boolean quit = false;
		while (!quit) {
			row += 1;
			String line = makeLine(schema, row, sparsityProbability);
			writer.write(line);
			if (max_rows > -1 && row >= max_rows) {
				quit = true;
			}
		}

		writer.flush();
		writer.close();

	}

	public String makeLine(Schema schema, int count, float sparsityProbability) {
		StringBuilder sb = new StringBuilder();
		boolean start = true;
		for (Column col : schema.getColumns()) {
			if (!start) {
				sb.append(DELIMITER);
			}
			start = false;
			boolean col_empty = random.nextBoolean();

			if (col.optional) {
				float probability = random.nextFloat();
				if (probability < col.sparsity || probability < sparsityProbability) {
					col_empty = true;
				} else {
					col_empty = false;
				}
			}

			if (col.optional && col_empty) {
				// don't write anything
			} else {
				switch (col.type) {
				case BOOLEAN:
					if (random.nextBoolean()) {
						sb.append("0");
					} else {
						sb.append("1");
					}
					break;
				case DOUBLE:
					if (col.pattern != null) {
						String randomword = randomData.create(col.pattern, random);
						sb.append(randomword);
					} else {
						sb.append(random.nextDouble());
					}
					break;
				case FLOAT:
					if (col.pattern != null) {
						String randomword = randomData.create(col.pattern, random);
						sb.append(randomword);
					} else {
						sb.append(random.nextFloat());
					}
					break;
				case INTEGER:
					if (col.pattern != null) {
						String randomword = randomData.create(col.pattern, random);
						sb.append(randomword);
					} else {
						sb.append(random.nextInt());
					}
					break;
				case UNSIGNED_INTEGER:
					if (col.pattern != null) {
						String randomword = randomData.create(col.pattern, random);
						sb.append(randomword);
					} else {
						sb.append(Math.abs(random.nextInt()));
					}
					break;
				case STRING:
					// TODO split to own logic, unit tests
					if (col.range != null) {
						int index = random.nextInt(col.range.length) % col.range.length;
						sb.append(col.range[index]);
					} else if (col.pattern != null) {
						String randomword = randomData.create(col.pattern, random);
						sb.append(randomword);
					} else {
						StringBuilder word = new StringBuilder();
						for (int i = 0; i < 10; i++) {
							char c = (char) (random.nextInt(26) + 65);
							sb.append(c);
						}
						sb.append(word.toString());
					}
					break;
				case COUNT:
					sb.append(count);
					break;
				case UUID:
					sb.append(UUID.randomUUID().toString());
					break;
				case EPOCH:
				case DOB: // fallthrough intentional
					int days_old = random.nextInt(365 * 50);
					long dob_timestamp = System.currentTimeMillis() - (ONE_DAY * days_old);
					sb.append(dob_timestamp);
					break;
				case ADDRESS:
					StringBuilder word = new StringBuilder();
					for (int i = 0; i < 10; i++) {
						char c = (char) (random.nextInt(26) + 65);
						sb.append(c);
					}
					sb.append(word.toString());
					break;
				case DATE:
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					Instant now = Instant.ofEpochMilli(random.nextInt(Math.abs((int) System.currentTimeMillis())));
					Date d = new Date(now.toEpochMilli());
					sb.append(sdf.format(d));
					break;
				case DATETIME:
					sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					now = Instant.ofEpochMilli(random.nextInt(Math.abs((int) System.currentTimeMillis())));
					d = new Date(now.toEpochMilli());
					sb.append(sdf.format(d));
					break;
				case TIME:
					sdf = new SimpleDateFormat("HH:mm:ss");
					now = Instant.ofEpochMilli(random.nextInt(Math.abs((int) System.currentTimeMillis())));
					d = new Date(now.toEpochMilli());
					sb.append(sdf.format(d));
					break;
				case EMAIL:
					word = new StringBuilder();
					for (int i = 0; i < 10; i++) {
						char c = (char) (random.nextInt(26) + 65);
						word.append(c);
					}

					StringBuilder word2 = new StringBuilder();
					for (int i = 0; i < 10; i++) {
						char c = (char) (random.nextInt(26) + 65);
						word2.append(c);
					}

					sb.append(word.toString().toLowerCase() + "@" + word2.toString().toLowerCase() + ".com");
					break;

				default:
					break;
				}
			}
		}
		return sb.toString();

	}

	public static void main(String[] args) throws Exception {
		CLI cli = new CLI(args);
		DataCreator g = new DataCreator();
		g.execute(cli);
	}

}
