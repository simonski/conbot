package conbot.converter;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import conbot.utils.Report;

/**
 * Intended to hold shared state between Consumers
 *
 */
public class Context {

	public ConcurrentHashMap<String, AtomicLong> map;
	public Report report;
	
	public File sourceFile;

	public Context() {
		map = new ConcurrentHashMap<String, AtomicLong>();
	}

}
