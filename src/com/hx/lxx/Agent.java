package com.hx.lxx;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

public class Agent {

	static ReloadThread t;
	static Instrumentation inst;

	/**
	 * Premain method called before the target application is initialized.
	 * for example:
	 * java -noverify -javaagent:jreloader.jar=loadlib MainClass
	 * @param args command line argument passed via <code>-javaagent</code>
	 * @param inst instance of the instrumentation service
	 */
	public static void premain(String args, Instrumentation inst) throws ClassNotFoundException, UnmodifiableClassException {
		System.out.println("+---------------------------------------------------+");
		System.out.println("| JReloader Agent " + String.format("%-34s", Version.VERSION));
		System.out.println("+---------------------------------------------------+");
		if (args == null) {
			System.exit(0);
			return;
		}

		Agent.inst = inst;

		String loaddir = null;
		String logDir = null;
		String[] params = args.split("\\,");
		loaddir = params[0];
		logDir = params[1];

		try {
			File relaodDir = FileUtil.createDir(loaddir);

			File reloadLogDir = FileUtil.createDir(logDir);
			File reloadLogFile = FileUtil.createFile(reloadLogDir.getAbsolutePath() + "//jreloader.log");
			ReloadThread thread = new ReloadThread(relaodDir, reloadLogFile, inst);
			thread.start();
		} catch (Exception e) {
			System.err.println("Args = " + args);
			e.printStackTrace();
			System.exit(0);
		}

		System.out.println(args);
	}

}
