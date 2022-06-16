/*
 * Copyright © 2018 - 2022 SSHTOOLS Limited (support@sshtools.com)
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

	@Override
    public Iterable<Path> getRootDirectories() {
	    if(uri.getPath() == null)
	        return super.getRootDirectories();
	    else {
            var uriPath = uri.getPath();
            var fsRoot = getPath(uriPath);
            return Collections.<Path>singleton(fsRoot);
	    }
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

	public Iterator<Path> iterator(Path path, Filter<? super Path> filter) throws IOException {
		var obj = pathToFileObject(Vfs2NioFileSystemProvider.toVFSPath(path));
		var children = obj.getChildren();
		return new Iterator<Path>() {
			int index;

			@Override
			public boolean hasNext() {
				return index < children.length;
			}

			@Override
			public Path next() {
//				var croot = path.getRoot();
//				var f = path.getFileName();
//				return new Vfs2NioPath(Vfs2NioFileSystem.this, croot.toString(),
//						(f == null ? "" : f.toString() + "/") + children[index++].getName().getBaseName().toString());
			    
			    return new Vfs2NioPath(Vfs2NioFileSystem.this, null, children[index++].getName().getPath());
			}
		};
	}

	public void setTimes(Vfs2NioPath path, FileTime mtime, FileTime atime, FileTime ctime) {
		if (atime != null || ctime != null)
			throw new UnsupportedOperationException();
		var object = pathToFileObject(path);
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
