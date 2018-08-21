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
			<version>0.0.1-SNAPSHOT</version>
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
			<version>0.0.1-SNAPSHOT</version>
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
			<version>0.0.1-SNAPSHOT</version>
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
			<version>0.0.1-SNAPSHOT</version>
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

With this added, you would get support for a URI in the format `vfs:sftp://user@host/path`. 


 
##Example 1 - List Directory

The following would list all of the root directories on an SFTP server (if you have SFTP provider installed) :-
 
```
/* The actual Commons VFS URI to access */  
String vfsUri = "sftp://myuser@myserver/";

for(Path p : FileSystems.newFileSystem(
			URI.create("vfs:" + vfsUri), 
			new HashMap<String, ?>()).getRootDirectories()) {
	System.out.println(p);
	try(DirectoryStream<Path> d = Files.newDirectoryStream(p)) {
		for(Path dp : d) {
			System.out.println("  " + dp);	
		}
	}
}
```

##TODO Example 2 - Reading Files

##TODO Example 3 - Deleting Files

##TODO Example 4 - Renaming Files

##TODO Example 5 - Copying Files

##TODO Example 6 - File Attributes

##TODO Authentication

##TODO Passing FileSystemOptions

##TODO Custom FileManager

