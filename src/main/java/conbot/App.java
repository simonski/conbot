package conbot;

import java.io.File;
import java.lang.reflect.Method;

import conbot.bq.BQ;
import conbot.converter.Converter;
import conbot.datacreator.DataCreator;
import conbot.datacreator.SchemaCreator;
import conbot.utils.CLI;

public class App {

	public static String NAME = "conbot";
	public static String VERSION = "0.0.1";

	CLI cli;

	public App(CLI cli) {
		this.cli = cli;
	}

	public static void main(String[] args) throws Exception {
		CLI cli = new CLI(args);
		App app = new App(cli);
		if (args.length == 0) {
			usage();
			CLI.die("");
		}
		String command = cli.args[0];
		try {
			if (command == null) {
				usage();
				CLI.die("");
			} else {
				String safe_command = command.replace("-", "_");
				Method m = app.getClass().getMethod(safe_command);
				if (m == null) {
					usage();
					CLI.die("");
				} else {
					m.invoke(app);
				}
			}
		} catch (NoSuchMethodException e) {
			CLI.err("I don't know how to '" + command + "' - try 'help'.");
			CLI.die("");
		}
	}

	public void version() {
		CLI.out(VERSION);
	}

	public void convert() throws Exception {
		Converter.main(cli.args);
	}

	public void ls() throws Exception {
		File file = new File(cli.getStringOrDie("-dir"));
		for (File f : file.listFiles()) {
			CLI.out(f.getAbsolutePath());
		}
	}

	public void data() throws Exception {
		DataCreator.main(cli.args);
	}

	public void bq() throws Exception {
		BQ.main(cli.args);
	}

	/**
	 * create random data, optionally from a schema
	 */
	public void schema() throws Exception {
		SchemaCreator.main(cli.args);
	}

	public void help() {
		String helpCommand = cli.args[1];
		if ("convert".equals(helpCommand)) {
			Converter.help();
		} else if ("data".equals(helpCommand)) {
			DataCreator.help();
		} else if ("schema".equals(helpCommand)) {
			SchemaCreator.help();
		} else if ("bq".equals(helpCommand)) {
			BQ.help();
		} else {
			CLI.out("I don't have any help for '" + helpCommand + "'.");
		}
	}

	public static void usage() {
		CLI.out("conbot is a data file converter.");
		CLI.out("");
		CLI.out("Usage: conbot command [arguments]");
		CLI.out("");
		CLI.out("The commands are:");
		CLI.out("");
		CLI.out("        convert     converts a source file");
		CLI.out("        data        creates sample data.");
		CLI.out("        schema      creates a schema to use when creating data.");
		CLI.out("        version     prints version .");
		CLI.out("");
		CLI.out("Usage \"conbot help <COMMAND>\" for more information about a command.");
		CLI.out("");
	}

}
