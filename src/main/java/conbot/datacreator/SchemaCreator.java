package conbot.datacreator;

import java.io.File;
import java.util.Random;

import conbot.utils.CLI;
import conbot.utils.FileUtils;

public class SchemaCreator {

	public static int DEFAULT_SEED = 0; // UUID and TIME first 2 column;
	public static int DEFAULT_COLS = 16;

	public void execute(CLI cli) throws Exception {
		if (cli.contains("-help") || cli.contains("-h") || cli.args.length < 3) {
			help();
			return;
		}

		if (cli.contains("-f")) {
			// load and print the schema to stdout (see if it is parseable)
			Schema schema = load(cli);
			System.out.println(schema.toString());
		} else {
			// create a schema instead
			Schema schema = create(cli);
			System.out.println(schema.toString());
		}
	}

	public static void help() {
		CLI.out("schema         creates sample schemas");
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

	public Schema create(CLI cli) throws Exception {
		int cols = cli.getIntOrDefault("-cols", DEFAULT_COLS);
		int seed = cli.getIntOrDefault("-seed", DEFAULT_SEED);
		Random random = new Random(seed);
		float optionalProbability = cli.getFloatOrDefault("-optional", random.nextFloat()); // the seed governs
		String schemaFactoryClassname = cli.getStringOrDefault("-schemafactory", SchemaFactory.class.getName());
		SchemaFactory schemaFactory = (SchemaFactory) Class.forName(schemaFactoryClassname).newInstance();

		Schema schema = schemaFactory.buildRandom(cols, seed, optionalProbability);
		return schema;
	}

	public Schema load(CLI cli) throws Exception {
		SchemaFactory f = new SchemaFactory();
		File file = FileUtils.resolveFile(cli.getStringOrDie("-f"));
		return f.build(file);
	}

	public static void main(String[] args) throws Exception {
		CLI cli = new CLI(args);
		SchemaCreator g = new SchemaCreator();
		g.execute(cli);
	}

}
