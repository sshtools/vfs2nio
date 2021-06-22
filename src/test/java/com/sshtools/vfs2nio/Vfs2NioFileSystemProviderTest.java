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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import junit.framework.Assert;

public class Vfs2NioFileSystemProviderTest {
	File rootFile = new File(File.separator);

	@Test
	public void testCreateFolder() throws Exception {
		try (FileSystem rootFs = createRootVFS()) {
			String randpath = System.getProperty("java.io.tmpdir") + "/"
					+ String.valueOf((long) (Math.abs(Math.random() * 100000)));
			File randfile = new File(randpath);
			Assert.assertFalse(randfile.exists());
			Files.createDirectory(rootFs.getPath(randpath));
			Assert.assertTrue(randfile.exists());
		}
	}

	@Test
	public void testFileDelete() throws Exception {
		try (FileSystem rootFs = createRootVFS()) {
			File file = File.createTempFile("vfs", "tmp");
			writeTestFile(file);
			Files.delete(rootFs.getPath(file.getPath()));
			Assert.assertFalse(file.exists());
		}
	}

	@Test
	public void testFileRead() throws Exception {
		try (FileSystem rootFs = createRootVFS()) {
			File file = File.createTempFile("vfs", "tmp");
			writeTestFile(file);
			try (InputStream in = Files.newInputStream(rootFs.getPath(file.getPath()))) {
				try (InputStream origIn = new FileInputStream(file)) {
					compareStreams(origIn, in);
				}
			}
		}
	}

	@Test
	public void testFileReadUri() throws Exception {
		File file = File.createTempFile("vfs", "tmp");
		writeTestFile(file);
		Path path = Paths.get(new URI("vfs:" + file.toURI().toString()));
		try (InputStream in = Files.newInputStream(path)) {
			try (InputStream origIn = new FileInputStream(file)) {
				compareStreams(origIn, in);
			}
		}
	}

	@Test
	public void testFileWrite() throws Exception {
		try (FileSystem rootFs = createRootVFS()) {
			File file = File.createTempFile("vfs", "tmp");
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			writeTestBytes(bytes);
			// Write via vfs
			try (OutputStream out = Files.newOutputStream(rootFs.getPath(file.getPath()))) {
				out.write(bytes.toByteArray());
				out.flush();
			}
			// Compare reading directly with byte array
			try (InputStream in = new FileInputStream(file)) {
				try (InputStream origIn = new ByteArrayInputStream(bytes.toByteArray())) {
					compareStreams(origIn, in);
				}
			}
		}
	}

	@Test
	public void testRootList() throws Exception {
		try (FileSystem fs = createRootVFS()) {
			/* Get names from VFS */
			List<String> names = new ArrayList<>();
			for (Path p : fs.getRootDirectories()) {
				try (DirectoryStream<Path> d = Files.newDirectoryStream(p)) {
					for (Path dp : d)
						names.add(dp.getFileName().toString());
				}
			}
			Collections.sort(names);
			/* Get names directly */
			List<String> directNames = new ArrayList<>(Arrays.asList(rootFile.list()));
			Collections.sort(directNames);
			/* Compare them */
			Assert.assertEquals(names, directNames);
		}
	}

	private void compareStreams(InputStream expected, InputStream actual) throws IOException {
		int r1, r2;
		while (true) {
			r1 = expected.read();
			r2 = actual.read();
			if (r1 == -1 && r2 == -1)
				return;
			else
				Assert.assertEquals(r1, r2);
		}
	}

	private FileSystem createRootVFS() throws IOException {
		URI uri = URI.create("vfs:" + rootFile.toURI().toString());
		FileSystem fs = FileSystems.newFileSystem(uri, null);
		return fs;
	}

	private void writeTestBytes(OutputStream out) throws IOException {
		for (int i = 0; i < 1024; i++)
			out.write((int) (Math.random() * 256));
		out.flush();
	}

	private void writeTestFile(File file) throws IOException, FileNotFoundException {
		try (FileOutputStream fos = new FileOutputStream(file)) {
			writeTestBytes(fos);
		}
	}
}
