package com.hx.lxx;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReloadThread extends Thread {

    private Logger log;

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
        log.info("reload thread started");

        while (true) {
            try {
                sleep(5000);

                checkReload();
            } catch (Exception e) {
                this.log.error("reload run error", e);
            }
        }
    }

    private void checkReload() throws Exception {
        String absPath = reloadDir.getAbsolutePath() + File.separatorChar;
        List<String> files = FileUtil.listDirAllFiles(absPath, filter);
        if ((files == null) || (files.isEmpty())) {
            return;
        }

        boolean isChange = false;
        for (String temp : files) {
            File file = new File(absPath + temp).getAbsoluteFile();

            Entry e = new Entry();
            e.name = temp.substring(0, temp.lastIndexOf(".")).replace(File.separatorChar, '/');
            e.file = file;

            if (entries.containsKey(e.name) && (entries.get(e.name).lastModified == file.lastModified())) {
                continue;
            }

            entries.put(e.name, e);
            isChange = true;
        }

        if (!isChange) {
            return;
        }

        findGroups();

        File dir = new File(absPath);
        reload(dir, entries.values());
    }

    @SuppressWarnings({"deprecation", "unchecked"})
    private void reload(File dir, Collection<Entry> aux) throws IOException, ClassNotFoundException, UnmodifiableClassException {
        List<ClassDefinition> definitionList = new ArrayList<ClassDefinition>();
        for (Entry e : aux) {
            if (e.isDirty() && e.parent == null) {
                try {
                    String className = e.name.replaceAll("\\/", ".");
                    className = className.replaceAll("\\\\", ".");

                    URL[] urls = new URL[]{dir.toURL()};
                    URLClassLoader urlcl = new URLClassLoader(urls);
                    Class clazz = urlcl.loadClass(className);

                    log.info("reload className = " + className + ", FileName = " + e.file);

                    byte[] bytes = FileUtil.loadClassData(e.file);
                    ClassDefinition definition = new ClassDefinition(clazz, bytes);
                    definitionList.add(definition);
                } catch (Exception t) {
                    log.error("Could not reload " + e.name, t);
                }

                e.clearDirty();
            } else {
                log.error("reload isDiry = " + (e.isDirty()) + ", fileName = " + e.file.getAbsolutePath());
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
