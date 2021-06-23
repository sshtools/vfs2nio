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
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;

public abstract class FileChannelFromSeekableByteChannelBase
    extends FileChannel
{
    protected abstract SeekableByteChannel getSeekableByteChannel();
    protected abstract int getBlockSize();

    @Override
    public int read(ByteBuffer dst) throws IOException {
        return getSeekableByteChannel().read(dst);
    }

    @Override
    public long read(ByteBuffer[] dsts, int offset, int length) throws IOException {
        return ChannelUtils.readScattered(getSeekableByteChannel(), dsts, offset, length);
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        return getSeekableByteChannel().write(src);
    }

    @Override
    public long write(ByteBuffer[] srcs, int offset, int length) throws IOException {
        return ChannelUtils.writeScattered(getSeekableByteChannel(), srcs, offset, length);
    }

    @Override
    public long position() throws IOException {
        return getSeekableByteChannel().position();
    }

    @Override
    public FileChannel position(long newPosition) throws IOException {
        getSeekableByteChannel().position(newPosition);
        return this;
    }

    @Override
    public long size() throws IOException {
        return getSeekableByteChannel().size();
    }

    @Override
    public FileChannel truncate(long size) throws IOException {
        getSeekableByteChannel().truncate(size);
        return this;
    }

    @Override
    public void force(boolean metaData) throws IOException {
        // Silently ignored
        // throw new UnsupportedOperationException();
    }

    @Override
    public long transferTo(long position, long count, WritableByteChannel target) throws IOException {
        return ChannelUtils.transferTo(getSeekableByteChannel(), position, count, target, getBlockSize());
    }

    @Override
    public long transferFrom(ReadableByteChannel src, long position, long count) throws IOException {
        return ChannelUtils.transferFrom(getSeekableByteChannel(), src, position, count, getBlockSize());
    }

    @Override
    public int read(ByteBuffer dst, long position) throws IOException {
        return ChannelUtils.read(getSeekableByteChannel(), dst, position);
    }

    @Override
    public int write(ByteBuffer src, long position) throws IOException {
        return ChannelUtils.write(getSeekableByteChannel(), src, position);
    }

    @Override
    public MappedByteBuffer map(MapMode mode, long position, long size) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public FileLock lock(long position, long size, boolean shared) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public FileLock tryLock(long position, long size, boolean shared) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void implCloseChannel() throws IOException {
        getSeekableByteChannel().close();
    }
}
