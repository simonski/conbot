package conbot.converter.empty;

import java.io.IOException;

import conbot.converter.Consumer;
import conbot.converter.Record;

public class ConsumerEmpty extends Consumer {

	public ConsumerEmpty() {
		super();

	}

	public void buildWriter() throws IOException {
	}

	public void write(Record record) {
		String[] splits = record.line.split(",", -1);
	}

	public void close() throws IOException {
	}

	@Override
	public String getOutputFileSuffix() {
		return "";
	}
}
