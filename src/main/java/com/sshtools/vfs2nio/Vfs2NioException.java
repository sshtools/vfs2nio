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

@SuppressWarnings("serial")
public class Vfs2NioException extends RuntimeException {
	public Vfs2NioException() {
	}

	public Vfs2NioException(String message) {
		super(message);
	}

	public Vfs2NioException(String message, Throwable cause) {
		super(message, cause);
	}

	public Vfs2NioException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public Vfs2NioException(Throwable cause) {
		super(cause);
	}
}
