package conbot.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class StringValidator {
	
	// YYYY-MM-DDTHH:MM:SS.000Z"
	final String REGEX_DATETIME = "((....)-(..)-(..)T(..):(..):(..)\\.(......))(...)Z";

	Pattern dateTimePattern = null;
	
	public StringValidator() {
		dateTimePattern = Pattern.compile(REGEX_DATETIME);
	}
	
	public String[] splitNanos(String candidate) {
		
		Matcher m = dateTimePattern.matcher(candidate);
		if (!m.matches()) {
			return null;
		}
		
		String group1 = m.group(1);
		String group9 = m.group(9);
		
		String[] results = new String[2];
		results[0] = group1;		// date-time
		results[1] = group9;		// nanos
		return results;

	}

	public boolean isDateTime(String candidate) {
		if (candidate == null || candidate.trim().equals("")) {
			return false;
		}
		Matcher m = dateTimePattern.matcher(candidate);
		if (!m.matches() ) {
			return false;
		}

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		try {
			sdf.parse(m.group(1));
			return true;
		} catch (ParseException pe) {
			System.out.println("Date is invalid.");
			return false;
		}
	}
 
	 
}

