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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessDeniedException;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.ProviderMismatchException;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.apache.commons.vfs2.AllFileSelector;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.RandomAccessContent;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.util.RandomAccessMode;

public class Vfs2NioFileSystemProvider extends FileSystemProvider {
	public final static String FILE_SYSTEM_OPTIONS = "com.sshtools.vfs2nio.fileSystemOptions";
	public final static String VFS_MANAGER = "com.sshtools.vfs2nio.vfsManager";

	// Checks that the given file is a UnixPath
	static final Vfs2NioPath toVFSPath(Path path) {
		if (path == null)
			throw new NullPointerException();
		if (!(path instanceof Vfs2NioPath))
			throw new ProviderMismatchException();
		return (Vfs2NioPath) path;
	}

	private final Map<URI, Vfs2NioFileSystem> filesystems = Collections.synchronizedMap(new HashMap<>());

	public Vfs2NioFileSystemProvider() {
	}

	@Override
	public void checkAccess(Path path, AccessMode... modes) throws IOException {
		Vfs2NioPath p = toVFSPath(path);
		FileObject fo = p.toFileObject();

		if (modes.length == 0) {
			modes = new AccessMode[] { AccessMode.READ };
		}

		for (AccessMode m : modes) {
			switch (m) {
			case EXECUTE:
				if (!fo.isExecutable())
					throw new AccessDeniedException(String.format("No %s access to %s", m, path));
				break;
			case READ:
				if (!fo.isReadable())
					throw new AccessDeniedException(String.format("No %s access to %s", m, path));
				break;
			case WRITE:
				if (!fo.isWriteable())
					throw new AccessDeniedException(String.format("No %s access to %s", m, path));
				break;
			default:
				break;
			}
		}
	}

	@Override
	public void copy(Path src, Path target, CopyOption... options) throws IOException {
		/*
		 * TODO: Support REPLACE_EXISTING, COPY_ATTRIBUTES, ATOMIC_MOVE if
		 * possible
		 */
		toVFSPath(target).toFileObject().copyFrom(toVFSPath(src).toFileObject(), new AllFileSelector());
	}

	@Override
	public void createDirectory(Path path, FileAttribute<?>... attrs) throws IOException {
		/* TODO: Support attributes */
		Vfs2NioPath p = toVFSPath(path);
		checkAccess(p, AccessMode.WRITE);
		FileObject fo = p.toFileObject();
		fo.createFolder();
	}

	@Override
	public final void delete(Path path) throws IOException {
		Vfs2NioPath p = toVFSPath(path);
		checkAccess(p, AccessMode.WRITE);
		FileObject fo = p.toFileObject();
		fo.deleteAll();
	}

	@Override
	public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options) {
		return Vfs2NioFileAttributeView.get(toVFSPath(path), type);
	}

	@Override
	public FileStore getFileStore(Path path) throws IOException {
		return toVFSPath(path).getFileStore();
	}

	@Override
	public FileSystem getFileSystem(URI uri) {
		synchronized (filesystems) {
			Vfs2NioFileSystem vfs = null;
			URI path = toFsUri(uri);
			vfs = filesystems.get(path);
			if (vfs == null)
				throw new FileSystemNotFoundException(String.format("Cannot find file system for %s", uri));
			return vfs;
		}
	}

	@Override
	public Path getPath(URI uri) {
		FileSystem fileSystem;
		try {
			fileSystem = getFileSystem(uri);
		} catch (FileSystemNotFoundException fsnfe) {
			try {
				fileSystem = newFileSystem(uri, new HashMap<>());
			} catch (IOException e) {
				throw new Vfs2NioException("Failed to create new file system.", e);
			}
		}
		return fileSystem.getPath(toFsUri(uri).getSchemeSpecificPart());
	}

	@Override
	public String getScheme() {
		return "vfs";
	}

	@Override
	public boolean isHidden(Path path) {
		try {
			return toVFSPath(path).toFileObject().isHidden();
		} catch (FileSystemException e) {
			return false;
		}
	}

	@Override
	public boolean isSameFile(Path path, Path other) throws IOException {
		return toVFSPath(path).toFileObject().equals(toVFSPath(other).toFileObject());
	}

	@Override
	public void move(Path src, Path target, CopyOption... options) throws IOException {
		toVFSPath(src).toFileObject().moveTo(toVFSPath(target).toFileObject());
	}

	@Override
	public AsynchronousFileChannel newAsynchronousFileChannel(Path path, Set<? extends OpenOption> options, ExecutorService exec,
			FileAttribute<?>... attrs) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs)
			throws IOException {
		// TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public DirectoryStream<Path> newDirectoryStream(Path path, Filter<? super Path> filter) throws IOException {
		return new Vfs2NioDirectoryStream(toVFSPath(path), filter);
	}

	@Override
	public FileChannel newFileChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
		RandomAccessMode accessMode = options.contains(StandardOpenOption.WRITE)
				? RandomAccessMode.READWRITE
				: RandomAccessMode.READ;

		Vfs2NioPath vfsPath = toVFSPath(path);
		FileObject fileObject = vfsPath.toFileObject();
		RandomAccessContent content = fileObject.getContent().getRandomAccessContent(accessMode);

		return new FileChannelFromSeekableByteChannelImpl(new Vfs2NioSeekableByteChannel(content));
	}

	@Override
	public FileSystem newFileSystem(Path path, Map<String, ?> env) throws IOException {
		// TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public FileSystem newFileSystem(URI uri, Map<String, ?> env) throws IOException {
		URI path = toFsUri(uri);
		if (filesystems.containsKey(path))
			throw new FileSystemAlreadyExistsException();
		synchronized (filesystems) {
			FileSystemManager mgr = env == null ? null : (FileSystemManager) env.get(VFS_MANAGER);
			if (mgr == null)
				mgr = VFS.getManager();
			FileSystemOptions opts = env == null ? null : (FileSystemOptions) env.get(FILE_SYSTEM_OPTIONS);
			Vfs2NioFileSystem vfs = new Vfs2NioFileSystem(this,
					opts == null ? mgr.resolveFile(path) : mgr.resolveFile(path.toString(), opts), path);
			filesystems.put(path, vfs);
			return vfs;
		}
	}

	@Override
	public InputStream newInputStream(Path path, OpenOption... options) throws IOException {
		List<OpenOption> optlist = Arrays.asList(options);
		if (optlist.contains(StandardOpenOption.WRITE))
			throw new IllegalArgumentException(String.format("%s is not supported by this method.", StandardOpenOption.WRITE));
		checkAccess(path, AccessMode.READ);
		return toVFSPath(path).toFileObject().getContent().getInputStream();
	}

	@Override
	public OutputStream newOutputStream(Path path, OpenOption... options) throws IOException {
		List<OpenOption> optlist = Arrays.asList(options);
		if (optlist.contains(StandardOpenOption.READ))
			throw new IllegalArgumentException(String.format("%s is not supported by this method.", StandardOpenOption.READ));
		FileObject fo = toVFSPath(path).toFileObject();
		if (optlist.contains(StandardOpenOption.CREATE_NEW) && fo.exists())
			throw new IOException(
					String.format("%s already exists, and the option %s was specified.", fo, StandardOpenOption.CREATE_NEW));
		checkAccess(path, AccessMode.WRITE);
		return fo.getContent().getOutputStream(optlist.contains(StandardOpenOption.APPEND));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options) throws IOException {
		if (type == BasicFileAttributes.class || type == Vfs2NioFileAttributes.class)
			return (A) toVFSPath(path).getAttributes();
		return null;
	}

	@Override
	public Map<String, Object> readAttributes(Path path, String attribute, LinkOption... options) throws IOException {
		return toVFSPath(path).readAttributes(attribute, options);
	}

	@Override
	public Path readSymbolicLink(Path link) throws IOException {
		throw new UnsupportedOperationException("Not supported.");
	}

	@Override
	public void setAttribute(Path path, String attribute, Object value, LinkOption... options) throws IOException {
		toVFSPath(path).setAttribute(attribute, value, options);
	}

	protected URI toFsUri(URI uri) {
		String scheme = uri.getScheme();
		if ((scheme == null) || !scheme.equalsIgnoreCase(getScheme())) {
			throw new IllegalArgumentException(String.format("URI scheme must be %s", getScheme()));
		}
		try {
			String spec = uri.getSchemeSpecificPart();
			int sep = spec.indexOf("!/");
			if (sep != -1)
				spec = spec.substring(0, sep);
			URI u = new URI(spec);
			return u;
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(e.getMessage(), e);
		}
	}

	protected Path XXuriToPath(URI uri) {
		String scheme = uri.getScheme();
		if ((scheme == null) || !scheme.equalsIgnoreCase(getScheme())) {
			throw new IllegalArgumentException("URI scheme is not '" + getScheme() + "'");
		}
		try {
			// only support legacy JAR URL syntax vfs:{uri}!/{entry} for now
			String spec = uri.getSchemeSpecificPart();
			int sep = spec.indexOf("!/");
			if (sep != -1)
				spec = spec.substring(0, sep);
			URI u = new URI(spec);
			return Paths.get(u).toAbsolutePath();
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(e.getMessage(), e);
		}
	}

	void removeFileSystem(URI path) throws IOException {
		filesystems.remove(path);
	}
}
