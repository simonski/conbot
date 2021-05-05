package conbot.utils;

import java.util.Random;

/**
 *
 */
public class RandomData {

	public String create(String pattern, Random random) {
		String value = "";
		for (int index=0; index<pattern.length(); index++) {
			String c = pattern.substring(index, index+1).toUpperCase();
			if (c.equals("9")) {
				value += "" + random.nextInt(10);
			} else if ( c.equals("A")) {
				// random single character
				char ch = (char) (random.nextInt(26) + 65);
				value += ch;
			} else {
				// retain whatever it is {
				value += c;
			}
			
		}
		return value;
	}


}