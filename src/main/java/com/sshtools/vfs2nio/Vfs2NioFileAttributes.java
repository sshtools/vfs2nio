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

import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.security.cert.Certificate;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.vfs2.FileContentInfo;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;

public class Vfs2NioFileAttributes implements BasicFileAttributes {
	private final FileObject e;

	Vfs2NioFileAttributes(FileObject e) {
		this.e = e;
	}

	public Map<String, Object> attributes() {
		try {
			return e.getContent().getAttributes();
		} catch (FileSystemException e) {
		}
		return Collections.emptyMap();
	}

	public Certificate[] certificates() {
		try {
			return e.getContent().getCertificates();
		} catch (FileSystemException e) {
		}
		return null;
	}

	public String contentEncoding() {
		try {
			FileContentInfo info = e.getContent().getContentInfo();
			if (info != null)
				return info.getContentEncoding();
		} catch (FileSystemException e) {
		}
		return null;
	}

	public String contentType() {
		try {
			FileContentInfo info = e.getContent().getContentInfo();
			if (info != null)
				return info.getContentType();
		} catch (FileSystemException e) {
		}
		return null;
	}

	@Override
	public FileTime creationTime() {
		/*
		 * TODO - maybe from attributes, but those are specific to the file
		 * system
		 */
		return null;
	}

	@Override
	public Object fileKey() {
		return null;
	}

	@Override
	public boolean isDirectory() {
		try {
			return e.getType() == FileType.FILE_OR_FOLDER || e.getType() == FileType.FOLDER;
		} catch (FileSystemException e) {
			return false;
		}
	}

	@Override
	public boolean isOther() {
		return false;
	}

	@Override
	public boolean isRegularFile() {
		try {
			return e.getType() == FileType.FILE;
		} catch (FileSystemException e) {
			return false;
		}
	}

	@Override
	public boolean isSymbolicLink() {
		return false;
	}

	@Override
	public FileTime lastAccessTime() {
		/*
		 * TODO - maybe from attributes, but those are specific to the file
		 * system
		 */
		return null;
	}

	@Override
	public FileTime lastModifiedTime() {
		try {
			return FileTime.fromMillis(e.getContent().getLastModifiedTime());
		} catch (FileSystemException e) {
			return null;
		}
	}

	@Override
	public long size() {
		try {
			return e.getContent().getSize();
		} catch (FileSystemException e) {
			return 0;
		}
	}
}
