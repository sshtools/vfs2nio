/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.nio;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.ProviderMismatchException;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.spi.FileSystemProvider;
import java.util.AbstractList;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.sshtools.vfs2nio.Vfs2NioFileSystem;

public abstract class BasePath<T extends BasePath<T, FS, P>, FS extends BaseFileSystem<T, P>, P extends FileSystemProvider> implements Path {

    protected final ImmutableList<String> names;
    protected final String root;
    private final FS fileSystem;


    public BasePath(FS fileSystem, String root, ImmutableList<String> names) {
        this.fileSystem = fileSystem;
        this.root = root;
        this.names = names;
    }

    public BasePath(FS fileSystem, String root, String... names) {
    	this(fileSystem, root, new ImmutableList<>(names));
    }

    @Override
    public int compareTo(Path paramPath) {
        T p1 = asT();
        T p2 = checkPath(paramPath);
        int c = compare(p1.root, p2.root);
        if (c != 0) {
            return c;
        }
        for (int i = 0; i < Math.min(p1.names.size(), p2.names.size()); i++) {
            String n1 = p1.names.get(i);
            String n2 = p2.names.get(i);
            c = compare(n1, n2);
            if (c != 0) {
                return c;
            }
        }
        return p1.names.size() - p2.names.size();
    }

    @Override
    public boolean endsWith(Path other) {
        T p1 = asT();
        T p2 = checkPath(other);
        if (p2.isAbsolute()) {
            return p1.compareTo(p2) == 0;
        }
        return endsWith(p1.names, p2.names);
    }

    @Override
    public boolean endsWith(String other) {
        return endsWith(getFileSystem().getPath(other));
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Path
                && compareTo((Path) obj) == 0;
    }

    @Override
    public T getFileName() {
        if (!names.isEmpty()) {
            return create(null, names.get(names.size() - 1));
        }
        return null;
    }

    @Override
    public FS getFileSystem() {
        return fileSystem;
    }

    @Override
    public T getName(int index) {
        int maxIndex = getNameCount();
        if ((index < 0) || (index >= maxIndex)) {
            throw new IllegalArgumentException("Invalid name index " + index + " - not in range [0-" + maxIndex + "]");
        }
        return create(null, names.subList(index, index + 1));
    }

    @Override
    public int getNameCount() {
        return names.size();
    }

    @Override
    public T getParent() {
        if (names.isEmpty() || ((names.size() == 1) && (root == null))) {
            return null;
        }
        return create(root, names.subList(0, names.size() - 1));
    }

    @Override
    public T getRoot() {
        if (isAbsolute()) {
            return create(root);
        }
        return null;
    }

    @Override
    public int hashCode() {
        int hash = Objects.hashCode(getFileSystem());
        // use hash codes from toString() form of names
        hash = 31 * hash + Objects.hashCode(root);
        for (String name : names) {
            hash = 31 * hash + Objects.hashCode(name);
        }
        return hash;
    }

    @Override
    public boolean isAbsolute() {
        return root != null;
    }

    @Override
    public Iterator<Path> iterator() {
        return new AbstractList<Path>() {
            @Override
            public Path get(int index) {
                return getName(index);
            }

            @Override
            public int size() {
                return getNameCount();
            }
        }.iterator();
    }

    @Override
    public T normalize() {
        if (isNormal()) {
            return asT();
        }

        Deque<String> newNames = new ArrayDeque<>();
        for (String name : names) {
            if (name.equals("..")) {
                String lastName = newNames.peekLast();
                if (lastName != null && !lastName.equals("..")) {
                    newNames.removeLast();
                } else if (!isAbsolute()) {
                    // if there's a root and we have an extra ".." that would go up above the root, ignore it
                    newNames.add(name);
                }
            } else if (!name.equals(".")) {
                newNames.add(name);
            }
        }

        return newNames.equals(names) ? asT() : create(root, newNames);
    }

    @Override
    public WatchKey register(WatchService watcher, WatchEvent.Kind<?>... events) throws IOException {
        return register(watcher, events, (WatchEvent.Modifier[]) null);
    }

    @Override
    public WatchKey register(WatchService watcher, WatchEvent.Kind<?>[] events, WatchEvent.Modifier... modifiers) throws IOException {
        throw new UnsupportedOperationException("Register to watch " + toAbsolutePath() + " N/A");
    }

    @Override
    public T relativize(Path other) {
        T p1 = asT();
        T p2 = checkPath(other);
        if (!Objects.equals(p1.getRoot(), p2.getRoot())) {
            throw new IllegalArgumentException("Paths have different roots: " + this + ", " + other);
        }
        if (p2.equals(p1)) {
            return create(null);
        }
        if (p1.root == null && p1.names.isEmpty()) {
            return p2;
        }
        // Common subsequence
        int sharedSubsequenceLength = 0;
        for (int i = 0; i < Math.min(p1.names.size(), p2.names.size()); i++) {
            if (p1.names.get(i).equals(p2.names.get(i))) {
                sharedSubsequenceLength++;
            } else {
                break;
            }
        }
        int extraNamesInThis = Math.max(0, p1.names.size() - sharedSubsequenceLength);
        List<String> extraNamesInOther = (p2.names.size() <= sharedSubsequenceLength)
                ? Collections.<String>emptyList()
                : p2.names.subList(sharedSubsequenceLength, p2.names.size());
        List<String> parts = new ArrayList<>(extraNamesInThis + extraNamesInOther.size());
        // add .. for each extra name in this path
        parts.addAll(Collections.nCopies(extraNamesInThis, ".."));
        // add each extra name in the other path
        parts.addAll(extraNamesInOther);
        return create(null, parts);
    }

    @Override
    public T resolve(Path other) {
        T p1 = asT();
        T p2 = checkPath(other);
        if (p2.isAbsolute()) {
            return p2;
        }
        if (p2.names.isEmpty()) {
            return p1;
        }
        String[] names = new String[p1.names.size() + p2.names.size()];
        int index = 0;
        for (String p : p1.names) {
            names[index++] = p;
        }
        for (String p : p2.names) {
            names[index++] = p;
        }
        return create(p1.root, names);
    }

    @Override
    public T resolve(String other) {
        return resolve(getFileSystem().getPath(other));
    }

    @Override
    public Path resolveSibling(Path other) {
        T parent = getParent();
        return parent == null ? other : parent.resolve(other);
    }

    @Override
    public Path resolveSibling(String other) {
        return resolveSibling(getFileSystem().getPath(other));
    }

    @Override
    public boolean startsWith(Path other) {
        T p1 = asT();
        T p2 = checkPath(other);
        return Objects.equals(p1.getFileSystem(), p2.getFileSystem())
                && Objects.equals(p1.root, p2.root)
                && startsWith(p1.names, p2.names);
    }

    @Override
    public boolean startsWith(String other) {
        return startsWith(getFileSystem().getPath(other));
    }

    @Override
    public T subpath(int beginIndex, int endIndex) {
        int maxIndex = getNameCount();
        if ((beginIndex < 0) || (beginIndex >= maxIndex) || (endIndex > maxIndex) || (beginIndex >= endIndex)) {
            throw new IllegalArgumentException("subpath(" + beginIndex + "," + endIndex + ") bad index range - allowed [0-" + maxIndex + "]");
        }
        return create(null, names.subList(beginIndex, endIndex));
    }

    @Override
    public T toAbsolutePath() {
        if (isAbsolute()) {
            return asT();
        }
        return fileSystem.getDefaultDir().resolve(this);
    }

    @Override
    public File toFile() {
        throw new UnsupportedOperationException("To file " + toAbsolutePath() + " N/A");
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (root != null) {
            sb.append(root);
        }
        
        String separator = getFileSystem().getSeparator();
        for (String name : names) {
            if ((sb.length() > 0) && (sb.charAt(sb.length() - 1) != '/')) {
                sb.append(separator);
            }
            sb.append(name);
        }
        return sb.toString();
    }

    @Override
    public URI toUri() {
        URI fsUri = ((Vfs2NioFileSystem)getFileSystem()).getUri();

        String str = fsUri + "/" + names.stream().collect(Collectors.joining("/"));
        try {
            return new URI(str);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    protected T asT() {
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    protected T checkPath(Path paramPath) {
        if (paramPath.getClass() != getClass()) {
            throw new ProviderMismatchException("Path is not of this class: " + paramPath + "[" + paramPath.getClass().getSimpleName() + "]");
        }
        T t = (T) paramPath;

        FileSystem fs = t.getFileSystem();
        if (fs.provider() != this.fileSystem.provider()) {
            throw new ProviderMismatchException("Mismatched providers for " + t);
        }
        return t;
    }

    protected int compare(String s1, String s2) {
        if (s1 == null) {
            return s2 == null ? 0 : -1;
        } else {
            return s2 == null ? +1 : s1.compareTo(s2);
        }
    }

    protected T create(String root, Collection<String> names) {
        return create(root, new ImmutableList<>(names.toArray(new String[names.size()])));
    }

    protected T create(String root, ImmutableList<String> names) {
        return fileSystem.create(root, names);
    }

    protected T create(String root, String... names) {
        return create(root, new ImmutableList<>(names));
    }

    protected boolean endsWith(List<?> list, List<?> other) {
        return other.size() <= list.size() && list.subList(list.size() - other.size(), list.size()).equals(other);
    }

    protected boolean isNormal() {
        int count = getNameCount();
        if ((count == 0) || ((count == 1) && !isAbsolute())) {
            return true;
        }
        boolean foundNonParentName = isAbsolute(); // if there's a root, the path doesn't start with ..
        boolean normal = true;
        for (String name : names) {
            if (name.equals("..")) {
                if (foundNonParentName) {
                    normal = false;
                    break;
                }
            } else {
                if (name.equals(".")) {
                    normal = false;
                    break;
                }
                foundNonParentName = true;
            }
        }
        return normal;
    }

    protected boolean startsWith(List<?> list, List<?> other) {
        return list.size() >= other.size() && list.subList(0, other.size()).equals(other);
    }

}
