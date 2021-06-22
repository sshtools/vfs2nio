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
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileStoreAttributeView;

public class Vfs2NioFileStore extends FileStore {
	private static class CVFS2NioFSFileStoreAttributes {
		Vfs2NioFileStore fstore;

		public CVFS2NioFSFileStoreAttributes(Vfs2NioFileStore fstore) throws IOException {
			this.fstore = fstore;
		}

		public long totalSpace() {
			return fstore.fs.getTotalSpace();
		}

		public long unallocatedSpace() throws IOException {
			if (!fstore.isReadOnly())
				return fstore.fs.getUnallocatedSpace();
			return 0;
		}

		public long usableSpace() throws IOException {
			if (!fstore.isReadOnly())
				return fstore.fs.getUsableSpace();
			return 0;
		}
	}

	private final Vfs2NioFileSystem fs;

	Vfs2NioFileStore(Vfs2NioPath zpath) {
		this.fs = zpath.getFileSystem();
	}

	@Override
	public Object getAttribute(String attribute) throws IOException {
		if (attribute.equals("totalSpace"))
			return getTotalSpace();
		if (attribute.equals("usableSpace"))
			return getUsableSpace();
		if (attribute.equals("unallocatedSpace"))
			return getUnallocatedSpace();
		throw new UnsupportedOperationException(String.format("Attribute %s is not supported.", attribute));
	}

	@Override
	public <V extends FileStoreAttributeView> V getFileStoreAttributeView(Class<V> type) {
		if (type == null)
			throw new NullPointerException();
		return null;
	}

	@Override
	public long getTotalSpace() throws IOException {
		return new CVFS2NioFSFileStoreAttributes(this).totalSpace();
	}

	@Override
	public long getUnallocatedSpace() throws IOException {
		return new CVFS2NioFSFileStoreAttributes(this).unallocatedSpace();
	}

	@Override
	public long getUsableSpace() throws IOException {
		return new CVFS2NioFSFileStoreAttributes(this).usableSpace();
	}

	@Override
	public boolean isReadOnly() {
		return fs.isReadOnly();
	}

	@Override
	public String name() {
		return fs.toString() + "/";
	}

	@Override
	public boolean supportsFileAttributeView(Class<? extends FileAttributeView> type) {
		return (type == BasicFileAttributeView.class || type == Vfs2NioFileAttributeView.class);
	}

	@Override
	public boolean supportsFileAttributeView(String name) {
		return name.equals("basic") || name.equals("vfs");
	}

	@Override
	public String type() {
		return "vfs";
	}
}
