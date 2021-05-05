package conbot.datacreator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;

import conbot.datacreator.Schema.Column;
import conbot.datacreator.Schema.ColumnType;
import conbot.utils.CLI;
import conbot.utils.FileUtils;

/**
 * A SchemaFactory is responsible for building a Schema
 *
 */
public class SchemaFactory {

	/**
	 * create a random schema based on the seed
	 * 
	 * @param cols
	 * @param seed
	 * @param optionalProbability
	 * @return
	 */
	public Schema buildRandom(int cols, int seed, float optionalProbability) {
		Random random = new Random(seed);
		List<Column> columns = new ArrayList<Column>();
		if (seed == 0) {
			String[] range = null;
			Column uuid = new Column("random_0", ColumnType.UUID, false, 0, "", range);
			Column time = new Column("random_1", ColumnType.DATETIME, false, 0, "", range);
			columns.add(uuid);
			columns.add(time);
		}
		int minCol = seed == 0 ? 2 : 0;
		for (int col = minCol; col < cols; col++) {
			int index = random.nextInt(ColumnType.values().length);
			ColumnType ct = ColumnType.values()[index];
			String name = "random_" + col;
			String pattern = null;
			String[] range = null;
			if (random.nextFloat() > optionalProbability) {
				columns.add(new Column(name, ct, false, 0, pattern, range));
			} else {
				float sparsity = random.nextFloat();
				columns.add(new Column(name, ct, true, sparsity, pattern, range));

			}
		}
		return new Schema(columns);
	}

	public Schema build(File file) throws IOException {
		if (file.getName().toLowerCase().endsWith(".csv") || file.getName().toLowerCase().endsWith(".txt")) {
			return buildFromCSV(file);
		} else if (file.getName().toLowerCase().endsWith(".json")) {
			return buildFromJSON(file);
		} else {
			throw new IOException("Unsupported schema type.");
		}
	}

	private Schema buildFromJSON(File file) throws IOException {

		String content = FileUtils.readFileToStringBuffer(file).toString();
		JSONArray arr = new JSONArray(content);
		List<Column> columns = new ArrayList<Column>();
		for (int index = 0; index < arr.length(); index++) {
			JSONObject columnJSON = arr.getJSONObject(index);
			String type = columnJSON.getString("type");
			String name = columnJSON.getString("name");
			String pattern = columnJSON.optString("pattern");
			JSONArray rangeArray = columnJSON.optJSONArray("range");
			String[] range = null;
			if (rangeArray != null) {
				range = new String[rangeArray.length()];
				for (int idx = 0; idx < rangeArray.length(); idx++) {
					range[idx] = rangeArray.getString(idx);
				}
			}

			boolean optional = !columnJSON.getString("mode").equalsIgnoreCase("REQUIRED");
			float sparsity = optional ? 0.5f : 1.0f;
			try {
				String sparsityStr = columnJSON.getString("sparsity");
				sparsity = Float.parseFloat(sparsityStr);
			} catch (Exception e) {
			}
			Column column = buildColumn(type, name, optional, sparsity, pattern, range);
			columns.add(column);
		}
		return new Schema(columns);
	}

	private Schema buildFromCSV(File file) throws IOException {
		List<Column> columns = new ArrayList<Column>();
		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);
		String line = br.readLine();
		while (line != null) {
			if (line.trim().equals("")) {
				line = br.readLine();
				continue;
			}
			String[] splits = line.trim().split(",");
			String name = splits[0].toLowerCase();
			String type = splits[1].toLowerCase();
			String pattern = "";
			boolean optional = false;
			float sparsity = 0.0f;
			if (splits.length > 2) {
				optional = splits[2].equalsIgnoreCase("optional");
				if (splits.length > 3) {
					if (optional) {
						sparsity = Float.parseFloat(splits[3]);
					}
				}

				if (splits.length > 4) {
					pattern = splits[4];
				}

			}

			Column column = buildColumn(type, name, optional, sparsity, pattern, null);
			if (column == null) {
				throw new NullPointerException("Unknown col type '" + type + "', please implement.");
			}
			columns.add(column);

			line = br.readLine();
		}
		fr.close();
		return new Schema(columns);

	}

	public Column buildColumn(String type, String name, boolean optional, float sparsity, String pattern,
			String[] range) {

		ColumnType columnType = ColumnType.STRING;
		if (type.equals("int") || type.equals("integer")) {
			columnType = ColumnType.INTEGER;
		} else if (type.equalsIgnoreCase("uint") || type.equals("unsigned_integer")) {
			columnType = ColumnType.UNSIGNED_INTEGER;
		} else if (type.equalsIgnoreCase("uuid")) {
			columnType = ColumnType.UUID;
		} else if (type.equalsIgnoreCase("count")) {
			columnType = ColumnType.COUNT;
		} else if (type.equalsIgnoreCase("float") || type.equalsIgnoreCase("float64")) {
			columnType = ColumnType.FLOAT;
		} else if (type.equalsIgnoreCase("double") || type.equalsIgnoreCase("NUMERIC")) {
			columnType = ColumnType.DOUBLE;
		} else if (type.equalsIgnoreCase("boolean") || type.equalsIgnoreCase("bool")) {
			columnType = ColumnType.BOOLEAN;
		} else if (type.equalsIgnoreCase("string")) {
			columnType = ColumnType.STRING;
		} else if (type.equalsIgnoreCase("dob")) {
			columnType = ColumnType.DOB;
		} else if (type.equalsIgnoreCase("epoch")) {
			columnType = ColumnType.EPOCH;
		} else if (type.equalsIgnoreCase("date")) {
			columnType = ColumnType.DATE;
		} else if (type.equalsIgnoreCase("time")) {
			columnType = ColumnType.TIME;
		} else if (type.equalsIgnoreCase("datetime") || type.equalsIgnoreCase("TIMESTAMP")) {
			columnType = ColumnType.DATETIME;
		} else if (type.equalsIgnoreCase("datetime_nanos")) {
			columnType = ColumnType.DATETIME_NANOS;
		} else if (type.equalsIgnoreCase("address")) {
			columnType = ColumnType.ADDRESS;
		} else if (type.equalsIgnoreCase("email")) {
			columnType = ColumnType.EMAIL;
		} else if (type.equalsIgnoreCase("path_cloud_storage_gcs")) {
			columnType = ColumnType.PATH_CLOUD_STORAGE_GCS;
		} else if (type.equalsIgnoreCase("ip_address")) {
			columnType = ColumnType.IP_ADDRESS;
		} else if (type.equalsIgnoreCase("port_number")) {
			columnType = ColumnType.PORT_NUMBER;
		} else if (type.equalsIgnoreCase("path_cloud_storage_azure")) {
			columnType = ColumnType.PATH_CLOUD_STORAGE_AZURE;
		} else if (type.equalsIgnoreCase("path_cloud_storage_s3")) {
			columnType = ColumnType.PATH_CLOUD_STORAGE_S3;
		} else if (type.equalsIgnoreCase("path_cloud_storage")) {
			columnType = ColumnType.PATH_CLOUD_STORAGE;
		} else if (type.equalsIgnoreCase("path_http_ftp")) {
			columnType = ColumnType.PATH_FILE;
		} else if (type.equalsIgnoreCase("path_file")) {
			columnType = ColumnType.PATH_HTTP_FTP;
		} else if (type.equalsIgnoreCase("path_http")) {
			columnType = ColumnType.PATH_HTTP;
		} else if (type.equalsIgnoreCase("path_https")) {
			columnType = ColumnType.PATH_HTTPS;
		} else if (type.equalsIgnoreCase("path_http_https")) {
			columnType = ColumnType.PATH_HTTP_HTTPS;
//		} else if (type.equalsIgnoreCase("path_cloud_stoage_gcs")) {
//			return new Column(name, ColumnType.PATH_CLOUD_STOAGE_GCS, optional, sparsity, pattern);
//		} else if (type.equalsIgnoreCase("path_cloud_stoage_gcs")) {
//			return new Column(name, ColumnType.PATH_CLOUD_STOAGE_GCS, optional, sparsity, pattern);
//		} else if (type.equalsIgnoreCase("path_cloud_stoage_gcs")) {
//			return new Column(name, ColumnType.PATH_CLOUD_STOAGE_GCS, optional, sparsity, pattern);
		} else {
			CLI.out(">>>>>>>>>>>");
			CLI.out(">>>>>>>>>>> SchemaFactory.buildColumn: UNKNOWN type=" + type + ", name=" + name + ", optional="
					+ optional + ", sparsity=" + sparsity);
			CLI.out(">>>>>>>>>>>");
			return null;
		}
		return new Column(name, columnType, optional, sparsity, pattern, range);
	}

}
