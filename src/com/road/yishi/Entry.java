package com.road.yishi;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Entry {

	Entry parent;
	List<Entry> children;
	String name;
	File file;
	long lastModified;

	void addChild(Entry e) {
		children = (children == null) ? new ArrayList<Entry>() : children;
		children.add(e);
		e.parent = this;
	}

	boolean isDirty() {
		return file.lastModified() > lastModified;
	}

	void clearDirty() {
		lastModified = file.lastModified();
		if (children != null) {
			for (Entry e : children) {
				e.lastModified = e.file.lastModified();
			}
		}
	}

	public void forceDirty() {
		if (parent == null) {
			lastModified = 0;
			if (children != null) {
				for (Entry e : children) {
					e.lastModified = 0;
				}
			}
		} else {
			parent.forceDirty();
		}
	}
}