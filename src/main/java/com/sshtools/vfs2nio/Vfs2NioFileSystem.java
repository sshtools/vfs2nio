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
import java.net.URI;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.FileStore;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.nio.BaseFileSystem;
import org.apache.nio.ImmutableList;

public class Vfs2NioFileSystem extends BaseFileSystem<Vfs2NioPath, Vfs2NioFileSystemProvider> {
	private static final Set<String> supportedFileAttributeViews = Collections
			.unmodifiableSet(new HashSet<String>(Arrays.asList("basic", "vfs")));
	private boolean open = true;
	private FileObject root;

	private URI uri;

	public Vfs2NioFileSystem(Vfs2NioFileSystemProvider provider, FileObject root, URI uri) throws FileSystemException {
		super(provider);
		this.root = root;
		this.uri = uri;
	}

	@Override
	public void close() throws IOException {
		if (!open)
			throw new IOException("Not open");
		open = false;
		provider().removeFileSystem(uri);
	}

	public Vfs2NioFileAttributes getFileAttributes(Vfs2NioPath path) {
		return new Vfs2NioFileAttributes(pathToFileObject(path));
	}

	public URI getUri() {
		return uri;
	}

	public FileObject getRoot() {
		return root;
	}

	public long getTotalSpace() {
		// TODO from FileSystem attributes?
		return 0;
	}

	public long getUnallocatedSpace() {
		// TODO from FileSystem attributes?
		return 0;
	}

	public long getUsableSpace() {
		// TODO from FileSystem attributes?
		return 0;
	}

	@Override
	public boolean isOpen() {
		return open;
	}

	@Override
	public boolean isReadOnly() {
		try {
			return !root.isWriteable();
		} catch (FileSystemException e) {
			return true;
		}
	}


	public static String[] getPathSegments(Path path) {
		int n = path.getNameCount();

		String[] result = new String[n];

		// The iterator is expected to yield n items
		Iterator<Path> it = path.iterator();
		for (int i = 0; i < n; ++i) {
			String segment = it.next().toString();
			result[i] = segment;
		}
		return result;
	}


	public Iterator<Path> iterator(Path path, Filter<? super Path> filter) throws IOException {
		FileObject obj = pathToFileObject(Vfs2NioFileSystemProvider.toVFSPath(path));
		FileObject[] children = obj.getChildren();

		String[] baseNames = getPathSegments(path);
		int childNameIdx = baseNames.length;

		return new Iterator<Path>() {
			int index;

			@Override
			public boolean hasNext() {
				return index < children.length;
			}

			@Override
			public Path next() {
				Path croot = path.getRoot();

				String[] childNames = Arrays.copyOf(baseNames, childNameIdx + 1);
				childNames[childNameIdx] = children[index].getName().getBaseName().toString();
				++index;

				return new Vfs2NioPath(Vfs2NioFileSystem.this, croot.toString(), childNames);
			}
		};
	}

	public void setTimes(Vfs2NioPath path, FileTime mtime, FileTime atime, FileTime ctime) {
		if (atime != null || ctime != null)
			throw new UnsupportedOperationException();
		FileObject object = pathToFileObject(path);
		try {
			object.getContent().setLastModifiedTime(mtime.toMillis());
		} catch (FileSystemException e) {
			throw new Vfs2NioException("Failed to set last modified.", e);
		}
	}

	@Override
	public Set<String> supportedFileAttributeViews() {
		return supportedFileAttributeViews;
	}

	@Override
	protected Vfs2NioPath create(String root, ImmutableList<String> names) {
		return new Vfs2NioPath(this, root, names);
	}

	boolean exists(Vfs2NioPath path) {
		try {
			return pathToFileObject(path).exists();
		} catch (Exception e) {
			return false;
		}
	}

	FileStore getFileStore(Vfs2NioPath path) {
		return new Vfs2NioFileStore(path);
	}

	FileObject pathToFileObject(Vfs2NioPath path) {
		try {
			return root.resolveFile(path.toString());
		} catch (FileSystemException e) {
			throw new Vfs2NioException("Failed to resolve.", e);
		}
	}
}
