package com.road.yishi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {

	public static byte[] loadClassData(File file) throws IOException {
		FileInputStream input = null;
		try {
			input = new FileInputStream(file);
			long length = file.length();
			byte[] bt = new byte[(int) length];
			int rl = input.read(bt);
			if (rl != length) {
				throw new IOException("Date load failed, fileName = " + file.getAbsolutePath());
			}

			return bt;
		} finally {
			if (input != null) {
				input.close();
			}
		}
	}

	public static byte[] loadClassData(String name) throws IOException {
		File file = new File(name);
		if (!file.exists()) {
			return null;
		}
		FileInputStream input = null;
		try {
			input = new FileInputStream(file);
			long length = file.length();
			byte[] bt = new byte[(int) length];
			int rl = input.read(bt);
			if (rl != length) {
				throw new IOException("���ܶ�ȡ��������");
			}
			return bt;
		} finally {
			if (input != null)
				input.close();
		}
	}

	public static long getFileLastModifyTime(String name) {
		File file = new File(name);
		return file.lastModified();
	}

	public static void writeFileLastModifyTime(String name, long time) {
		File file = new File(name);
		file.setLastModified(time);
	}

	public static String getClassPackage(String classPath) throws IOException {
		InputStream in = null;
		BufferedReader reader = null;
		try {
			in = new FileInputStream(classPath);
			reader = new BufferedReader(new InputStreamReader(in));
			String packageName = "";

			String line = "";
			while ((line = reader.readLine()) != null) {
				if (line.indexOf("package") != -1) {
					packageName = line.substring(line.indexOf(" "), line.length() - 1).trim();
					break;
				}
			}
			return packageName;
		} finally {
			if (reader != null)
				reader.close();
			if (in != null)
				in.close();
		}
	}

	public static String readStringFileUTF8(String fileName) throws Exception {
		return readStringFile(fileName, "UTF-8");
	}

	public static String readStringFile(String fileName, String encoding) throws Exception {
		FileInputStream fis = null;
		BufferedReader reader = null;
		try {
			fis = new FileInputStream(fileName);
			reader = new BufferedReader(new InputStreamReader(fis, encoding));
			StringBuffer sb = new StringBuffer();
			while (reader.ready()) {
				String line = reader.readLine();
				sb.append(line);
				sb.append("\r\n");
			}
			return sb.toString();
		} finally {
			if (reader != null)
				reader.close();
			if (fis != null)
				fis.close();
		}
	}

	public static void delete(File file) {
		if (file.isFile()) {
			file.delete();
		} else if (file.isDirectory()) {
			File[] _files = file.listFiles();
			for (File _f : _files) {
				delete(_f);
			}
			file.delete();
		}
	}

	public static List<String> listDirAllFiles(String root, FileFilter filter) {
		File dir = new File(root);
		root = dir.getAbsolutePath();

		List<String> fileNames = new ArrayList<String>();

		File[] files = dir.listFiles(filter);
		for (File file : files) {
			listAllFiles(root, file, fileNames, filter);
		}

		return fileNames;
	}

	private static void listAllFiles(String root, File dir, List<String> fileNames, FileFilter filter) {
		if (dir.isDirectory()) {
			File[] children = dir.listFiles(filter);
			for (File file : children)
				listAllFiles(root, file, fileNames, filter);
		} else {
			String path = dir.getAbsolutePath();
			String fileName = path.substring(root.length() + 1);
			fileNames.add(fileName);
		}
	}

	public static File createDir(String dir) {
		File file = new File(dir);
		if (file.exists()) {
			FileUtil.delete(file);
		}

		file.mkdir();
		return file;

	}

	public static File createFile(String dir) throws IOException {
		File file = new File(dir);
		if (file.exists()) {
			file.delete();
		}

		file.createNewFile();
		return file;
	}

	public static void deleteAllFiles(File baseDir) {
		File[] files = baseDir.listFiles();

		for (File file : files) {
			delete(file);
		}
	}

}