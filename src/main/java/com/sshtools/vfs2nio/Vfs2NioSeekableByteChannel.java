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
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;

import org.apache.commons.vfs2.RandomAccessContent;

import com.fasterxml.jackson.core.io.DataOutputAsStream;

public class Vfs2NioSeekableByteChannel
	implements SeekableByteChannel
{
	protected RandomAccessContent content;
	protected boolean isOpen;

	public Vfs2NioSeekableByteChannel(RandomAccessContent content) {
		this(content, true);
	}
	
	public Vfs2NioSeekableByteChannel(RandomAccessContent content, boolean isOpen) {
		super();
		this.content = content;
		this.isOpen = isOpen;
	}

	@Override
	public boolean isOpen() {
		return isOpen;
	}

	@Override
	public void close() throws IOException {
		content.close();
		isOpen = false;
	}

	public ReadableByteChannel getReadChannel() throws IOException {
		return Channels.newChannel(content.getInputStream());
	}

	public WritableByteChannel getWriteChannel() throws IOException {
		/* DataOutputAsStream comes from jackson */
		return Channels.newChannel(new DataOutputAsStream(content));
	}

	@Override
	public int read(ByteBuffer dst) throws IOException {
		return getReadChannel().read(dst);
	}

	@Override
	public int write(ByteBuffer src) throws IOException {
		return getWriteChannel().write(src);
	}

	@Override
	public long position() throws IOException {
		return content.getFilePointer();
	}

	@Override
	public SeekableByteChannel position(long newPosition) throws IOException {
		content.seek(newPosition);
		return this;
	}

	@Override
	public long size() throws IOException {
		return content.length();
	}

	@Override
	public SeekableByteChannel truncate(long size) throws IOException {
		content.setLength(size);
		return this;
	}

}
