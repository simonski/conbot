package conbot.converter.csv;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import conbot.converter.Consumer;
import conbot.converter.Record;

public class ConsumerCSV extends Consumer {

	BufferedWriter bwriter = null;

	public static final String COMMA = ",";
	public static final String NEWLINE = "\n";

	public ConsumerCSV() {
		super();
	}

	public void buildWriter() throws IOException {
		FileWriter writer = new FileWriter(this.file, StandardCharsets.UTF_8);
		bwriter = new BufferedWriter(writer, super.writeBuffer);
	}

	public void write(Record record) throws IOException {
		bwriter.write("" + record.rowNumber);
		bwriter.write(COMMA);
		String[] splits = record.line.split(COMMA, -1);
		bwriter.write(splits[0]);
		for (int index=1; index<splits.length; index++) {
			bwriter.write(COMMA);
			bwriter.write(splits[index]);
		}
		// for (int index=0; index<splits.length; index++) {
		// 	bwriter.write(splits[index]);
		// 	if (index+1 < splits.length) {
		// 		bwriter.write(COMMA);
		// 	}
		// }
		bwriter.write(NEWLINE);
	}

	public void close() throws IOException {
		bwriter.close();
	}

	@Override
	public String getOutputFileSuffix() {
		return "csv";
	}
}
