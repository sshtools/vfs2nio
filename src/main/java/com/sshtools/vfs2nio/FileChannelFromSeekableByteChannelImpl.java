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

import java.nio.channels.SeekableByteChannel;

public class FileChannelFromSeekableByteChannelImpl
	extends FileChannelFromSeekableByteChannelBase
{
	public static final int DEFAULT_BLOCK_SIZE = 4096;
	
	protected SeekableByteChannel delegate;
	protected int blockSize;

	public FileChannelFromSeekableByteChannelImpl(SeekableByteChannel delegate) {
		this(delegate, DEFAULT_BLOCK_SIZE);
	}

	public FileChannelFromSeekableByteChannelImpl(SeekableByteChannel delegate, int blockSize) {
		super();
		this.delegate = delegate;
		this.blockSize = blockSize;
	}

	@Override
	protected SeekableByteChannel getSeekableByteChannel() {
		return delegate;
	}

	@Override
	protected int getBlockSize() {
		return blockSize;
	}
}

