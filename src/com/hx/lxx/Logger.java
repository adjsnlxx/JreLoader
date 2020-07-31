package com.hx.lxx;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {

	private String name;
	private final SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	private static PrintWriter out;

	private static Level level = Level.DEBUG;

	public Logger(String name, File fileName) {
		this.name = name;

		try {
			out = new PrintWriter(fileName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.err.println("*** JRELOADER will not produce any logging output ***");
		}

		info("Reload load log path = " + fileName);
	}

	public static void setLevel(Level level) {
		Logger.level = level;
	}

	public void debug(String message) {
		print(Level.DEBUG, message);
	}

	public void info(String message) {
		print(Level.INFO, message);
	}

	public void warn(String message) {
		print(Level.WARN, message);
	}

	public void error(String message) {
		print(Level.ERROR, message);
	}

	public void error(String message, Throwable t) {
		print(Level.ERROR, message, t);
	}

	private void print(Level level, String message) {
		print(level, message, null);
	}

	private void print(Level level, String message, Throwable t) {
		print0(out, level, message, t);
	}

	private void print0(PrintWriter out, Level level, String message, Throwable t) {
		if (out != null && level.ordinal() >= Logger.level.ordinal()) {
			Date now = new Date();
			out.format("%s %-5s [%s] - %s", dateFmt.format(now), level.name(), name, message);
			out.println();
			if (t != null) {
				t.printStackTrace(out);
			}
			out.flush();
		}
	}

}
