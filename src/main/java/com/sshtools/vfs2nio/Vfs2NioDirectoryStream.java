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
import java.nio.file.ClosedDirectoryStreamException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class Vfs2NioDirectoryStream implements DirectoryStream<Path> {
	private final DirectoryStream.Filter<? super Path> filter;
	private final Vfs2NioFileSystem fs;
	private volatile Iterator<Path> iterator;
	private volatile boolean open = true;
	private final Path path;

	Vfs2NioDirectoryStream(Vfs2NioPath zipPath, DirectoryStream.Filter<? super Path> filter) throws IOException {
		this.fs = zipPath.getFileSystem();
		this.path = zipPath.normalize();
		this.filter = filter;
		if (!Files.isDirectory(path))
			throw new NotDirectoryException(zipPath.toString());
	}

	@Override
	public synchronized void close() throws IOException {
		open = false;
	}

	@Override
	public synchronized Iterator<Path> iterator() {
		if (!open)
			throw new ClosedDirectoryStreamException();
		if (iterator != null)
			throw new IllegalStateException();
		try {
			iterator = fs.iterator(path, filter);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		return new Iterator<Path>() {
			@Override
			public boolean hasNext() {
				if (!open)
					return false;
				return iterator.hasNext();
			}

			@Override
			public synchronized Path next() {
				if (!open)
					throw new NoSuchElementException();
				return iterator.next();
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
}
