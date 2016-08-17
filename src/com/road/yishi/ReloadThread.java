package com.road.yishi;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReloadThread extends Thread {

	private Logger log = null;

	private File reloadDir;
	private ClassFileFilter filter;
	private Instrumentation inst;

	private Map<String, Entry> entries = new LinkedHashMap<String, Entry>();

	public ReloadThread(File reloadDir, File logPath, Instrumentation inst) {
		super("ReloadThread");
		this.inst = inst;
		this.reloadDir = reloadDir;
		this.filter = new ClassFileFilter();
		this.log = new Logger("ReloadThread", logPath);

		setDaemon(true);
		setPriority(MAX_PRIORITY);
	}

	@Override
	public void run() {
		log.info("--------------------start--------------------");

		while (true) {
			try {
				sleep(5000);
			} catch (InterruptedException e) {
			}

			String absPath = reloadDir.getAbsolutePath() + File.separatorChar;
			List<String> dirNames = FileUtil.listDirAllFiles(absPath, filter);
			if ((dirNames == null) || (dirNames.isEmpty())) {
				continue;
			}

			for (String dirName : dirNames) {
				File file = new File(absPath + dirName).getAbsoluteFile();

				Entry e = new Entry();
				e.name = dirName.substring(0, dirName.lastIndexOf(".")).replace(File.separatorChar, '/');
				e.file = file;

				if (entries.containsKey(e.name) && (entries.get(e.name).lastModified == file.lastModified())) {
					continue;
				}

				entries.put(e.name, e);
			}

			findGroups();

			if (entries.isEmpty()) {
				continue;
			}

			log.debug("Checking changes...");
			List<Entry> aux = new ArrayList<Entry>(entries.values());
			for (Entry e : aux) {
				if (e.isDirty()) {
					e.forceDirty();
				}
			}

			File dir = new File(absPath);
			try {
				reload(dir, aux);

				if (dir.exists()) {
					FileUtil.deleteAllFiles(dir);
				}

				entries.clear();
			} catch (Exception e) {
				log.error("Reload Error", e);
			}
		}
	}

	@SuppressWarnings( { "deprecation", "unchecked" })
	private void reload(File dir, List<Entry> aux) throws IOException, ClassNotFoundException, UnmodifiableClassException {
		List<ClassDefinition> definitionList = new ArrayList<ClassDefinition>();
		for (Entry e : aux) {
			if (e.isDirty() && e.parent == null) {
				try {
					String className = e.name.replaceAll("\\/", ".");
					className = className.replaceAll("\\\\", ".");

					URL[] urls = new URL[] { dir.toURL() };
					URLClassLoader urlcl = new URLClassLoader(urls);
					Class clazz = urlcl.loadClass(className);

					log.info("Reload : className = " + className + ", FileName = " + e.file);

					byte[] bytes = FileUtil.loadClassData(e.file);
					ClassDefinition definition = new ClassDefinition(clazz, bytes);
					definitionList.add(definition);
				} catch (Exception t) {
					log.error("Could not reload " + e.name, t);
				}

				e.clearDirty();
			} else {
				log.error("Reload : isDiry = " + (e.isDirty()) + ", fileName = " + e.file.getAbsolutePath());
			}
		}

		if (definitionList.size() > 0) {
			inst.redefineClasses((ClassDefinition[]) definitionList.toArray(new ClassDefinition[definitionList.size()]));
		}
	}

	private void findGroups() {
		for (java.util.Map.Entry<String, Entry> e : entries.entrySet()) {
			String n = e.getValue().name;
			int p = n.indexOf('$');
			if (p != -1) {
				String parentName = n.substring(0, p);
				entries.get(parentName).addChild(e.getValue());
			}
		}
	}

	private class ClassFileFilter implements FileFilter {

		@Override
		public boolean accept(File pathname) {
			return pathname.isDirectory() || pathname.getName().endsWith(".class");
		}

	}
}
