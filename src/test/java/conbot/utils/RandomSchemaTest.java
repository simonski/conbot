package conbot.utils;

import conbot.datacreator.Schema;
import conbot.datacreator.SchemaCreator;
import conbot.datacreator.SchemaFactory;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Random;

public class RandomSchemaTest {

	@Test
	@Ignore
	public void test() throws Exception {
		SchemaCreator sc = new SchemaCreator();

		int seed = 500000;
		while(true) {
			String line = "-seed " + seed + " -cols 80 -sparsity 0.99 -optional 0.99";
			seed += 1;

			String[] args = line.split(" ", -1);
			CLI cli = new CLI(args);

			Schema schema = sc.create(cli);
			Schema.Column col1 = schema.getColumns().get(0);
			Schema.Column col2 = schema.getColumns().get(1);
			CLI.out("Seed is " + seed + ", col1=" + col1.type + ", col2=" + col2.type);
			if (col1.type == Schema.ColumnType.UUID) {
				if (col2.type == Schema.ColumnType.TIME ) {
					CLI.out("Seed is " + seed);
					break;
				}
			}
		}
	}

}
