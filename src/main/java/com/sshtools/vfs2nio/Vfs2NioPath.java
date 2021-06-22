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
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.nio.file.spi.FileSystemProvider;
import java.util.Map;

import org.apache.commons.vfs2.FileObject;
import org.apache.nio.BasePath;
import org.apache.nio.ImmutableList;

public class Vfs2NioPath extends BasePath<Vfs2NioPath, Vfs2NioFileSystem, Vfs2NioFileSystemProvider> {
	public Vfs2NioPath(Vfs2NioFileSystem fileSystem, String root, ImmutableList<String> names) {
		super(fileSystem, root, names);
	}

	public Vfs2NioPath(Vfs2NioFileSystem fileSystem, String root, String... names) {
		super(fileSystem, root, names);
	}

	public FileObject toFileObject() {
		return getFileSystem().pathToFileObject(this);
	}

	@Override
	public Path toRealPath(LinkOption... options) throws IOException {
		// TODO: handle links
		Vfs2NioPath absolute = toAbsolutePath();
		FileSystem fs = getFileSystem();
		FileSystemProvider provider = fs.provider();
		provider.checkAccess(absolute);
		return absolute;
	}

	boolean exists() {
		return getFileSystem().exists(normalize());
	}

	Vfs2NioFileAttributes getAttributes() throws IOException {
		Vfs2NioFileAttributes zfas = getFileSystem().getFileAttributes(normalize());
		if (zfas == null)
			throw new NoSuchFileException(toString());
		return zfas;
	}

	FileStore getFileStore() throws IOException {
		// each ZipFileSystem only has one root (as requested for now)
		if (exists())
			return getFileSystem().getFileStore(this);
		throw new NoSuchFileException(normalize().toString());
	}

	Map<String, Object> readAttributes(String attributes, LinkOption... options) throws IOException {
		String view = null;
		String attrs = null;
		int colonPos = attributes.indexOf(':');
		if (colonPos == -1) {
			view = "basic";
			attrs = attributes;
		} else {
			view = attributes.substring(0, colonPos++);
			attrs = attributes.substring(colonPos);
		}
		Vfs2NioFileAttributeView zfv = Vfs2NioFileAttributeView.get(this, view);
		if (zfv == null) {
			throw new UnsupportedOperationException("view not supported");
		}
		return zfv.readAttributes(attrs);
	}

	void setAttribute(String attribute, Object value, LinkOption... options) throws IOException {
		String type = null;
		String attr = null;
		int colonPos = attribute.indexOf(':');
		if (colonPos == -1) {
			type = "basic";
			attr = attribute;
		} else {
			type = attribute.substring(0, colonPos++);
			attr = attribute.substring(colonPos);
		}
		Vfs2NioFileAttributeView view = Vfs2NioFileAttributeView.get(this, type);
		if (view == null)
			throw new UnsupportedOperationException("view <" + view + "> is not supported");
		view.setAttribute(attr, value);
	}

	void setTimes(FileTime mtime, FileTime atime, FileTime ctime) throws IOException {
		getFileSystem().setTimes(normalize(), mtime, atime, ctime);
	}
}
