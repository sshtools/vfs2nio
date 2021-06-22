/**
 * Copyright © 2018 - 2021 SSHTOOLS Limited (support@sshtools.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sshtools.vfs2nio;

import java.io.IOException;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileTime;
import java.util.LinkedHashMap;
import java.util.Map;

public class Vfs2NioFileAttributeView implements BasicFileAttributeView {
	private static enum Attribute {
		attributes, certificates, contentEncoding, contentType, creationTime, fileKey, isDirectory, isOther, isRegularFile, isSymbolicLink, lastAccessTime, lastModifiedTime, size
	};

	@SuppressWarnings("unchecked")
	static <V extends FileAttributeView> V get(Vfs2NioPath path, Class<V> type) {
		if (type == null)
			throw new NullPointerException();
		if (type == BasicFileAttributeView.class)
			return (V) new Vfs2NioFileAttributeView(path, false);
		if (type == Vfs2NioFileAttributeView.class)
			return (V) new Vfs2NioFileAttributeView(path, true);
		return null;
	}
	static Vfs2NioFileAttributeView get(Vfs2NioPath path, String type) {
		if (type == null)
			throw new NullPointerException();
		if (type.equals("basic"))
			return new Vfs2NioFileAttributeView(path, false);
		if (type.equals("vfs"))
			return new Vfs2NioFileAttributeView(path, true);
		return null;
	}

	private final Vfs2NioPath path;

	private final boolean vfs;

	private Vfs2NioFileAttributeView(Vfs2NioPath path, boolean isZipView) {
		this.path = path;
		this.vfs = isZipView;
	}

	@Override
	public String name() {
		return vfs ? "vfs" : "basic";
	}

	@Override
	public Vfs2NioFileAttributes readAttributes() throws IOException {
		return path.getAttributes();
	}

	@Override
	public void setTimes(FileTime lastModifiedTime, FileTime lastAccessTime, FileTime createTime) throws IOException {
		path.setTimes(lastModifiedTime, lastAccessTime, createTime);
	}

	Object attribute(Attribute id, Vfs2NioFileAttributes attributes) {
		switch (id) {
		case size:
			return attributes.size();
		case creationTime:
			return attributes.creationTime();
		case lastAccessTime:
			return attributes.lastAccessTime();
		case lastModifiedTime:
			return attributes.lastModifiedTime();
		case isDirectory:
			return attributes.isDirectory();
		case isRegularFile:
			return attributes.isRegularFile();
		case isSymbolicLink:
			return attributes.isSymbolicLink();
		case isOther:
			return attributes.isOther();
		case fileKey:
			return attributes.fileKey();
		default:
			if (vfs) {
				switch (id) {
				case contentType:
					return attributes.contentType();
				case contentEncoding:
					return attributes.contentEncoding();
				case certificates:
					return attributes.certificates();
				case attributes:
					return attributes.attributes();
				default:
					break;
				}
			}
		}
		return null;
	}

	Map<String, Object> readAttributes(String attributes) throws IOException {
		Vfs2NioFileAttributes zfas = readAttributes();
		LinkedHashMap<String, Object> map = new LinkedHashMap<>();
		if ("*".equals(attributes)) {
			for (Attribute id : Attribute.values()) {
				try {
					map.put(id.name(), attribute(id, zfas));
				} catch (IllegalArgumentException x) {
				}
			}
		} else {
			String[] as = attributes.split(",");
			for (String a : as) {
				try {
					map.put(a, attribute(Attribute.valueOf(a), zfas));
				} catch (IllegalArgumentException x) {
				}
			}
		}
		return map;
	}

	void setAttribute(String attribute, Object value) throws IOException {
		try {
			if (Attribute.valueOf(attribute) == Attribute.lastModifiedTime)
				setTimes((FileTime) value, null, null);
			return;
		} catch (IllegalArgumentException x) {
		}
		throw new UnsupportedOperationException("'" + attribute + "' is unknown or read-only attribute");
	}
}
