# vfs2nio

Provides a bridge between the [JSR 203](https://jcp.org/en/jsr/detail?id=203) ([The Java NIO.2 File System in JDK 7](http://www.oracle.com/technetwork/articles/javase/nio-139333.html)) and [Apache's Commons VFS 2](https://commons.apache.org/proper/commons-vfs/). This allows you to use the standard
Java API with all [file systems](https://commons.apache.org/proper/commons-vfs/filesystems.html) in Commons VFS, and any 3rd party
add-ons that implement the Commons VFS API. This includes :-

### Default File Systems
 * [Bzip2](https://commons.apache.org/proper/commons-vfs/filesystems.html#gzip_and_bzip2)
 * [File](https://commons.apache.org/proper/commons-vfs/filesystems.html#Local_Files)
 * [FTP](https://commons.apache.org/proper/commons-vfs/filesystems.html#FTP)
 * [FTPS](https://commons.apache.org/proper/commons-vfs/filesystems.html#FTPS)
 * [GZip](https://commons.apache.org/proper/commons-vfs/filesystems.html#gzip_and_bzip2)
 * [HDFS](https://commons.apache.org/proper/commons-vfs/filesystems.html#HDFS)
 * [HTTP](https://commons.apache.org/proper/commons-vfs/filesystems.html#HTTP_and_HTTPS)
 * [Jar](https://commons.apache.org/proper/commons-vfs/filesystems.html#Zip_Jar_and_Tar)
 * [RAM](https://commons.apache.org/proper/commons-vfs/filesystems.html#ram)
 * [Res](https://commons.apache.org/proper/commons-vfs/filesystems.html#res)
 * [SFTP](https://commons.apache.org/proper/commons-vfs/filesystems.html#SFTP)
 * [Tar](https://commons.apache.org/proper/commons-vfs/filesystems.html#Zip_Jar_and_Tar)
 * [Temp](https://commons.apache.org/proper/commons-vfs/filesystems.html#Temporary_Fils)
 * [WebDAV](https://commons.apache.org/proper/commons-vfs/WebDAV)
 * [Zip](https://commons.apache.org/proper/commons-vfs/filesystems.html#Zip_Jar_and_Tar)

### Require Commons VFS Sandbox Library
 * [CIFS](https://commons.apache.org/proper/commons-vfs/filesystems.html#CIFS)
 * [mime](https://commons.apache.org/proper/commons-vfs/filesystems.html#mime)
 
### SSHTools Provided
 * [Azure](https://github.com/sshtools/vfs)
 * [AFP](https://github.com/sshtools/vfs) (Prototype)
 * [Dropbox](https://github.com/sshtools/vfs)
 * [GCS](https://github.com/sshtools/vfs)
 * [GoogleDrive](https://github.com/sshtools/vfs)
 * [NFS](https://github.com/sshtools/vfs) (Prototype)
 * [RFBFTP](https://github.com/sshtools/vfs)
 * [S3](https://github.com/sshtools/vfs)
 * [SFTP](https://www.sshtools.com/en/products/java-ssh-client/) (Maverick 1.6 provider)
 * [SFTP](https://github.com/sshtools/sshapi) (SSHAPI version with multiple SSH providers)
 * [WebDAV](https://github.com/sshtools/vfs)
 
Different providers may have different requirements, but in general, it is just a case of including an additional library into your project configuration. See below for more information.


## Configuring Your Project

This library is provided on Maven Central, so if you are using Maven, all you need to do is add this to your POM.

```
	...
		<dependency>
			<groupId>com.sshtools</groupId>
			<artifactId>vfs2nio</artifactId>
			<version>0.9.0-SNAPSHOT-SNAPSHOT</version>
		</dependency>
	...
```

Adjust for this use with other project systems such as Ivy, Gradle etc, or download directly from the [SSHTools](http://mvnrepository.com/artifact/com.sshtools) group.

This will get you support for the built-in file systems provided by Commons VFS that do not require addtional libraries.
This includes file systems such as *file*, *ram*, *temp*, *jar, *res*.

_For any other file systems you will always need this library, plus all of the others you would have to use if you were using Commons VFS directly._

### Additional Official File Systems

To get access to the officially supported Commons VFS providers, you may need to add additional libraries.

```
	...
		<dependency>
			<groupId>com.sshtools</groupId>
			<artifactId>vfs2nio</artifactId>
           <version>0.9.0-SNAPSHOT</version>
		</dependency>
		<dependency>
    		<groupId>org.apache.commons</groupId>
    		<artifactId>commons-compress</artifactId>
    		<version>1.18</version>
		</dependency>
	...

```

Will add support for *gzip*, *bzip*, *tar*, *zip*. See Commons VFS documentation for exactly what libraries are needed for what file systems. 

### Third Party Libraries

For some filesystems, you may need additional 3rd party libraries. For example, for Dropbox support, you would need to add 
the SSHTools VFS libraries :-

```
		<dependency>
			<groupId>com.sshtools</groupId>
			<artifactId>vfs2nio</artifactId>
			<version>0.9.0-SNAPSHOT</version>
		</dependency>
		<dependency>
			<groupId>com.sshtools</groupId>
			<artifactId>vfs-dropbox</artifactId>
			<version>3.0.0</version>
		</dependency>
```

With this added, you would get support for a URI in the format `vfs:dropbox://`.

Another slightly more complex example would be using the 'Maverick' library, accessing it's SFTP support via SSHAPI's commons VFS library. You need to exclude the _jSch_ library to prevent Commons VFS seeing this and using it over the SSHAPI provider, so it
is probably a good idea to add an exclusion (note, this isn't strictly required at the moment but may be in the future).

```
		<dependency>
			<groupId>com.sshtools</groupId>
			<artifactId>vfs2nio</artifactId>
           <version>0.9.0-SNAPSHOT</version>
			<exclusions>
		        <exclusion>
				    <groupId>com.jcraft</groupId>
				    <artifactId>jsch</artifactId>
		        </exclusion>
		    </exclusions>
		</dependency>
		<dependency>
			<groupId>com.sshtools</groupId>
			<artifactId>sshapi-commons-vfs</artifactId>
			<version>1.1.2</version>
		</dependency>
		<dependency>
			<groupId>com.sshtools</groupId>
			<artifactId>sshapi-maverick-16</artifactId>
			<version>1.1.2</version>
		</dependency>
```

With this added, you would get support for a URI in the format `vfs:sftp://user:password@host/path`. 

 
##Example 1 - Create A Directory

Create a new directory at the root of the file system.

```
        try (var fs = FileSystems.newFileSystem(URI.create(vfsUri), opts)) {
            Files.createDirectory(fs.getPath("mydir1"));
        }
```
 
##Example 2 - List Directory

The following would list all of the root directories on an SFTP server (if you have SFTP provider installed).
 
```
try (var fs = FileSystems.newFileSystem(URI.create("vfs:sftp://myuser:mypassword@myserver/"), new HashMap<>())) {
    for (var root : fs.getRootDirectories()) {
        System.out.println(root);
        try (var dir = Files.newDirectoryStream(root)) {
            for (var path : dir) {
                System.out.println("  " + path);
            }
        }
    }
}
```

##Example 3 - Writing Files

The following will create a file in the root of the VFS URI and fill it with some content.
 
```
try (var fs = FileSystems.newFileSystem(URI.create("vfs:sftp://myuser:mypassword@myserver/"), new HashMap<>())) {
    try (var out = Files.newBufferedWriter(fs.getPath("myfile.txt"))) {
        var wrt = new PrintWriter(out, true);
        wrt.println("My test content");
        wrt.println("Another line");
        wrt.println("The end");
    }
}
```

##Example 4 - Reading Files

The following will reads the file created in the previous example and displays it back to `sysout`.
 
```
try (var fs = FileSystems.newFileSystem(URI.create("vfs:sftp://myuser:mypassword@myserver/"), new HashMap<>())) {
    try (var in = Files.newBufferedReader(fs.getPath("myfile.txt"))) {
        String line;
        while( ( line = in.readLine() ) != null) {
            System.out.println(line);
        }
    }
}
```

##Example 5 - Copying Files

The following will copy the file created in Example 2 to another file.
 
```
try (var fs = FileSystems.newFileSystem(URI.create("vfs:sftp://myuser:mypassword@myserver/"), new HashMap<>())) {
    Files.copy(fs.getPath("myfile.txt"), fs.getPath("myfilecopy.txt"));
}
```

##TODO Example 6 - File Attributes

##TODO Example 7 - File Attributes

##TODO Example 8- Deleting Files

##TODO Example 9 - Renaming Files

## Authentication

There are 3 different techniques that may be used if the virtual file system requires authentication.

If the username is not supplied, the system property `user.name` will be queried for the default. If the password is not supplied, is will be interactively asked for in the `Console`, if the `Console` is available. 

### 1. Encoding The User Information And Password In The URI

The username and password are provided as URL encoded text into the URI itself when you create the file system. The syntax is ..

```
vfs:<vfsScheme>://<username>[:<password>]/.......
```

Both the username and password must be [URL encoded](https://en.wikipedia.org/wiki/Percent-encoding). 

The `<username>` may further encode the *Domain Name* (if the underlying VFS requires it). In this case, the syntax of `<username>` is ..

```
    <username>[@<domain>]
```

.. or ..

```
    [<domain>\]<username>
```

### 2. Providing User Information As File System Options 



```
    var opts = new HashMap<String, Object>();
    opts.put(Vfs2NioFileSystemProvider.DOMAIN, "mycompany"); // Optional
    opts.put(Vfs2NioFileSystemProvider.USERNAME, "myuser"); // Defaults to user.name
    opts.put(Vfs2NioFileSystemProvider.PASSWORD, "mypassword"); // May be a String or char[] 
    var fs = FileSystems.newFileSystem(URI.create("vfs:sftp://myserver/"), opts);
```


### 3. Providing A org.apache.commons.vfs2.UserAuthenticator

Commons VFS's native authentication mechanism is the `UserAuthenticator`. You may provide a class that implements this interface, and pass that to a NIO.2 file system via the `HashMap` argument of `FileSystems.newFileSystem()`. The option should have a key of `com.sshtools.vfs2nio.vfsAuthenticator` (or the constant  `Vfs2NioFileSystemProvider.AUTHENTICATOR`) and the value should be an instance of a `UserAuthenticator`. See the [Commons VFS documentation](https://commons.apache.org/proper/commons-vfs/api.html) for further information.

In this case, the root URI's user information is ignored, as are the other file system options described in Example 2 above. 

Use this technique if you want to interactively provide authentication details, e.g. in a GUI.

```
    var opts = new HashMap<String, Object>();
    opts.put(Vfs2NioFileSystemProvider.AUTHENTICATOR, myAuthenticatorInstance); 
    var fs = FileSystems.newFileSystem(URI.create("vfs:sftp://myserver/"), opts);
```

## FileSystemOptions

Commons VFS uses `FileSystemOptions` to configure scheme specific options for the various file systems.
Pass an instance of this class as a file system option with the key `com.sshtools.vfs2nio.fileSystemOptions` (or use the constant `Vfs2NioFileSystemProvider.FILE_SYSTEM_OPTIONS`).

For example, to configure use of private key for authentication with an SFTP file system.

```
    var opts = new HashMap<String, Object>();
    var fsOpts = new FileSystemOptions();
    SftpFileSystemConfigBuilder.getInstance().setIdentities(fsOptions, new File[] { new File("/path/to/private/key"); });
    opts.put(Vfs2NioFileSystemProvider.FILE_SYSTEM_OPTIONS, fsOpts)
    var fs = FileSystems.newFileSystem(URI.create("vfs:sftp://myserver/"), opts);
```

## FileSystemManager

It is also possible to extend Commons VFS's `FileSystemManager` for your needs, and pass this as a file system option. Pass an instance of this class as a file system option with the key `com.sshtools.vfs2nio.vfsManager` (or use the constant `Vfs2NioFileSystemProvider.VFS_MANAGER`).