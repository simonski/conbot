package conbot.datacreator;

import java.text.DecimalFormat;
import java.util.List;

public class Schema {

	public enum ColumnType {
		UUID, 
		UNSIGNED_INTEGER, 
		INTEGER, 
		FLOAT, 
		DOUBLE, 
		BOOLEAN,
		STRING, 
		COUNT, 
		DOB, 
		EPOCH,
		DATE,
		TIME,
		DATETIME,
		DATETIME_NANOS,
		ADDRESS,
		EMAIL,
		PATH_FILE,
		PATH_HTTP,
		PATH_HTTPS,
		PATH_HTTP_HTTPS,
		PATH_HTTP_FTP,
		IP_ADDRESS,
		PORT_NUMBER,
		PATH_CLOUD_STORAGE,
		PATH_CLOUD_STORAGE_GCS,
		PATH_CLOUD_STORAGE_S3,
		PATH_CLOUD_STORAGE_AZURE,
	}

	public static class Column {
		public ColumnType type;
		public String name;
		public boolean optional;
		public String pattern;		// if present, AAAAA.9999  (see RandomData)
		public String[] range;
		public float sparsity = 0.0f;

		public Column(String name, ColumnType type, boolean optional, float sparsity, String pattern, String[] range) {
			this.name = name;
			this.type = type;
			this.optional = optional;
			this.sparsity = sparsity;
			this.pattern = pattern;
			this.range = range;
		}

	}

	private List<Column> columns;

	public Schema(List<Column> columns) {
		this.columns = columns;
	}

	public List<Column> getColumns() {
		return columns;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		
		for (Column c : columns) {
			sb.append(c.name + "," + c.type + "," + (c.optional ? "optional" : "required") + "," + df.format(c.sparsity) + "\n");
		}
		return sb.toString();
	}

//	/**
//	 * creates a nullable string type avro schema for a list of column names
//	 * 
//	 * @param columns
//	 * @param recordname
//	 * @param namespace
//	 * @return
//	 */
//	public org.apache.avro.Schema toAvroSchema(String namespace, String recordname) throws IOException {
//
//		JSONObject json = new JSONObject();
//		json.put("type", "record");
//		json.put("name", recordname);
//		json.put("namespace", namespace);
//		JSONArray columns_array = new JSONArray();
//		JSONArray type_array;
//		for (Column column : getColumns()) {
//			JSONObject column_json = new JSONObject();
//			column_json.put("name", column.name);
//			switch (column.type) {
//			case BOOLEAN:
//				if ( column.optional) {
//					type_array = new JSONArray();
//					type_array.put("boolean");
//					type_array.put("null");
//					column_json.put("type", type_array);
//				} else {
//					column_json.put("type", "boolean");
//				}
//				break;
//				
//			case COUNT:
//				if (column.optional) {
//					type_array = new JSONArray();
//					type_array.put("long");
//					type_array.put("null");
//					column_json.put("type", type_array);
//				} else {
//					column_json.put("type", "long");
//				}
//				break;
//				
//			case DOB:
//				if (column.optional) {
//					type_array = new JSONArray();
//					type_array.put("string");
//					type_array.put("null");
//					column_json.put("type", type_array);
//				} else {
//					column_json.put("type", "string");
//				}
//				break;
//				
//			case DOUBLE:
//				if (column.optional) {
//					type_array = new JSONArray();
//					type_array.put("double");
//					type_array.put("null");
//					column_json.put("type", type_array);
//				} else {
//					column_json.put("type", "double");
//				}
//				break;
//				
//			case EPOCH:
//				if (column.optional) {
//					type_array = new JSONArray();
//					type_array.put("long");
//					type_array.put("null");
//					column_json.put("type", type_array);
//				} else {
//					column_json.put("type", "long");
//				}
//				break;
//				
//			case FLOAT:
//				if (column.optional) {
//					type_array = new JSONArray();
//					type_array.put("float");
//					type_array.put("null");
//					column_json.put("type", type_array);
//				} else {
//					column_json.put("type", "float");
//				}
//				break;
//				
//			case INTEGER:
//				if (column.optional) {
//					type_array = new JSONArray();
//					type_array.put("int");
//					type_array.put("null");
//					column_json.put("type", type_array);
//				} else {
//					column_json.put("type", "int");
//				}
//				break;
//				
//			case STRING:
//				if (column.optional) {
//					type_array = new JSONArray();
//					type_array.put("string");
//					type_array.put("null");
//					column_json.put("type", type_array);
//				} else {
//					column_json.put("type", "string");
//				}
//				break;
//				
//			case UNSIGNED_INTEGER:
//				if (column.optional) {
//					type_array = new JSONArray();
//					type_array.put("int");
//					type_array.put("null");
//					column_json.put("type", type_array);
//				} else {
//					column_json.put("type", "int");
//				}
//				break;
//				
//			case UUID:
//				if (column.optional) {
//					type_array = new JSONArray();
//					type_array.put("string");
//					type_array.put("null");
//					column_json.put("type", type_array);
//				} else {
//					column_json.put("type", "string");
//				}
//				break;
//			case ADDRESS:
//				if (column.optional) {
//					type_array = new JSONArray();
//					type_array.put("string");
//					type_array.put("null");
//					column_json.put("type", type_array);
//				} else {
//					column_json.put("type", "string");
//				}
//			case DATE:
//				if (column.optional) {
//					type_array = new JSONArray();
//					type_array.put("string");
//					type_array.put("null");
//					column_json.put("type", type_array);
//				} else {
//					column_json.put("type", "string");
//				}
//			case DATETIME:
//				if (column.optional) {
//					type_array = new JSONArray();
//					type_array.put("string");
//					type_array.put("null");
//					column_json.put("type", type_array);
//				} else {
//					column_json.put("type", "string");
//				}
//			case DATETIME_NANOS:
//				if (column.optional) {
//					type_array = new JSONArray();
//					type_array.put("string");
//					type_array.put("null");
//					column_json.put("type", type_array);
//				} else {
//					column_json.put("type", "string");
//				}
//			case EMAIL:
//				if (column.optional) {
//					type_array = new JSONArray();
//					type_array.put("string");
//					type_array.put("null");
//					column_json.put("type", type_array);
//				} else {
//					column_json.put("type", "string");
//				}
//			case TIME:
//				if (column.optional) {
//					type_array = new JSONArray();
//					type_array.put("string");
//					type_array.put("null");
//					column_json.put("type", type_array);
//				} else {
//					column_json.put("type", "string");
//				}
//
//			default:
//				break;
//			}
//			columns_array.put(column_json);
//		}
//		json.put("fields", columns_array);
//		String schema_string = json.toString();
//
//		Parser parser = new Parser();
//		// System.out.println(json.toString(4));
//		org.apache.avro.Schema schema = parser.parse(schema_string);
//		return schema;
//
//	}

}
