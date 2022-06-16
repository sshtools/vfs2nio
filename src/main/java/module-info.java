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
import java.nio.file.spi.FileSystemProvider;

import com.sshtools.vfs2nio.Vfs2NioFileSystemProvider;

module com.sshtools.vfs2nio {
    requires transitive commons.vfs2;
    /* requires static jsch; */
    requires transitive com.sshtools.sshapi.commonsvfs;
    requires transitive com.sshtools.sshapi.core;
    requires static org.apache.commons.compress;
    provides FileSystemProvider with Vfs2NioFileSystemProvider;
    exports com.sshtools.vfs2nio;
    exports org.apache.nio;
    
}