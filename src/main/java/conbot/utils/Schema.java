package conbot.utils;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

public class Schema {
	
	Field[] fields;

	enum Mode { REQUIRED, NULLABLE};
	enum FieldType { NUMERIC, STRING, TIMESTAMP, FLOAT64 };
	
	public class Field {
		Mode mode;
		FieldType fieldType;
		String name;
		String description;
		public Field(JSONObject json) {
			this.mode = Mode.valueOf(json.getString("mode"));
			this.fieldType = FieldType.valueOf(json.getString("type"));
			this.name = json.getString("name");
			this.description = json.getString("description");
		}
		
		public boolean validate(String candidate) {
			if ( mode == Mode.NULLABLE ) {
				// a nullable that is empty is true regardless of type
				if (candidate.trim().equals("")) {
					return true;
				}
				
				// a nullable that is not empty must still pass type validation
				switch (fieldType) {
				case FLOAT64:
					try {
						new Float(candidate);
					} catch (NumberFormatException e) {
						return false;
					}
					return true;
				case NUMERIC:
					try {
						Float f = new Float(candidate);
					} catch (NumberFormatException e) {
						return false;
					}
					return true;
				case STRING:
					return true;
				case TIMESTAMP:
					return false;
				default:
					return false;
				}
				
			}

			// else a non nullable must pass type validation
			// an empty non-nullable is false
			// a nullable that is not empty must still pass type validation
			switch (fieldType) {
			case FLOAT64:
				try {
					new Float(candidate);
				} catch (NumberFormatException e) {
					return false;
				}
				return true;
			case NUMERIC:
				try {
					new Float(candidate);
				} catch (NumberFormatException e) {
					return false;
				}
				return true;
			case STRING:
				return true;
			case TIMESTAMP:
				return candidate.length() == "2019-09-28T09:28:08.478089333Z".length();
				
			default:
				return false;
			}
			
		}
	}
	
	public Schema(File file) throws IOException {
		InputStream is = new FileInputStream(file);
		JSONTokener tokener = new JSONTokener(is);
		JSONArray arr = new JSONArray(tokener);
		Field[] fields = new Field[arr.length()];
		for (int index=0; index<arr.length(); index++) {
			JSONObject obj = arr.getJSONObject(index);
			Field field = new Field(obj);
			fields[index] = field;
		}
		this.fields = fields;
	}
	
	public boolean validate(int row, String[] columns) {
//		if (columns.length > fields.length) {
//			System.out.println("Validate: row invalid, data bigger than schema [" + columns.length + " != " + fields.length + "]");
//			return false;
//		}
		for (int index=0; index<columns.length; index++) {
			Field field = fields[index];
			if (field.validate(columns[index]) != true ) {
				System.out.println("Validate[" + row + "] : column '" + field.name + "' invalid, " + field.fieldType + "/" + field.mode + " != '" + columns[index] + "'");
				return false;
			}
		}
		return true;
	}
	
}

