package conbot.bq;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.google.cloud.bigquery.Field;
import com.google.cloud.bigquery.Schema;
import com.google.cloud.bigquery.StandardSQLTypeName;

import conbot.utils.BQLoader;
import conbot.utils.CLI;

public class BQ {

	CLI cli; // options passed via terminal

	public static void main(String[] args) throws Exception {
		CLI cli = new CLI(args);
		BQ app = new BQ(cli);
		if (cli.args.length == 0) {
			usage();
		} else {
			app.execute();
		}
	}

	public static void help() {
		CLI.out("bq                          help BQ");
		CLI.out("");
	}

	public static void usage() {
		CLI.out("bq                          usage BQ");
		CLI.out("");
	}

	public void execute() throws Exception {
		String command = cli.getStringOrDie("bq");
		if ("load".equals(command)) {
			load();
		} else {
			CLI.out("?");
		}
	}

	public void load() throws Exception {
		CLI.out("load");
		String datasetName = cli.getStringOrDie("-dataset");
		String tableName = cli.getStringOrDie("-table");
		String sourceUri = cli.getStringOrDie("-bqsource");
		String schemaFilename = cli.getStringOrDie("-bqschema");

		Schema schema = loadSchema(new File(schemaFilename));
		BQLoader.loadCsvFromGcs(datasetName, tableName, sourceUri, schema);

	}

	public BQ(CLI cli) throws Exception {
		this.cli = cli;
	}

	public Schema loadSchema(File file) throws Exception {
		FileInputStream in = new FileInputStream(file);
		JSONTokener tokener = new JSONTokener(in);
		JSONArray arr = new JSONArray(tokener);
		List<Field> fields = new ArrayList<Field>();
		for (int index = 0; index < arr.length(); index++) {
			JSONObject obj = arr.getJSONObject(index);
			String name = obj.getString("name");
			Field.Mode mode = Field.Mode.valueOf(obj.getString("mode"));
			StandardSQLTypeName type = StandardSQLTypeName.valueOf(obj.getString("type"));
			String description = obj.getString("description");
			Field f = Field.newBuilder(name, type).setMode(mode).setDescription(description).build();
			fields.add(f);
		}
		return Schema.of(fields);
	}

}
