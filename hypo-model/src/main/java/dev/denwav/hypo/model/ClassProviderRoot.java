/*
 * Hypo, an extensible and pluggable Java bytecode analytical model.
 *
 * Copyright (C) 2023  Kyle Wood (DenWav)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Lesser GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License only.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package dev.denwav.hypo.model;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Source of raw {@code byte[]} data for class files based on the class name when normalized with
 * {@link HypoModelUtil#normalizedClassName(String)}. This is used by the default {@link ClassDataProvider} abstract
 * implementation {@link AbstractClassDataProvider}, but is not a requirement for other implementations of
 * {@link ClassDataProvider} to use.
 *
 * <p>Unlike {@link ClassDataProvider}, this class has no expectation of caching. Implementations should return a new
 * {@code byte[]} whenever a class name is requested, it is up to the provider to cache objects.
 *
 * <p>The purpose of separating the roots from the providers is to more generally handle the 2 separate problems of
 * parsing Java class files independently, the 2 problems being:
 *
 * <ol>
 *     <li>The location and format the class files are stored in</li>
 *     <li>The format of the actual class files themselves and the method the class files are to be parsed</li>
 * </ol>
 *
 * <p>Roots handle the first concern, while providers handle the second. For example, Java classes are typically going
 * to either be in a zip archive such as a jar, or in a directory. Separate root implementations are needed to properly
 * handle these different sources of class files, but the process of actually parsing {@code .class} files is
 * independent of their source location.
 *
 * <p>By default 3 distinct types of roots are provided:
 *
 * <ol>
 *     <li>{@link #fromDir(Path) Directory-based roots}</li>
 *     <li>{@link #fromJar(Path) Jar-based roots}</li>
 *     <li>{@link #ofJdk() The system JDK root}</li>
 * </ol>
 *
 * <p>The system JDK root will use the currently running JVM to provide class file data for core JVM classes. Specific
 * implementations compatible with and tested against Java 8 through Java 17 will be returned based on the version of
 * the currently running JVM. Later versions of Java may also be supported, but this cannot be guaranteed.
 *
 * <p>Roots are {@link AutoCloseable} as they operate over file system resources. Roots passed into an
 * {@link AbstractClassDataProvider} will also be closed when the provider is closed.
 */
public interface ClassProviderRoot extends AutoCloseable {

    /**
     * Retrieve the raw binary data corresponding to the class file of the given name. Returns {@code null} if the class
     * file cannot be found.
     *
     * @param fileName The class file name to find.
     * @return The raw binary data corresponding to the given name.
     * @throws IOException If an IO error occurs while trying to read the file.
     */
    byte @Nullable [] getClassData(final @NotNull String fileName) throws IOException;

    /**
     * Find all of the classes available to this root and return {@link FileDataReference references} to them, without
     * actually loading and returning the whole file itself. This allows providers to enumerate the full list of all
     * class files while still being able to lazily load the class data itself.
     *
     * @return A list of {@link FileDataReference} corresponding to every class file available to this root.
     * @throws IOException If an IO error occurs while enumerating the file list.
     */
    @NotNull List<? extends FileDataReference> getAllClasses() throws IOException;

    /**
     * Get a {@link Stream} which walks the full file tree of this root, including all non-class files. This is an
     * experimental API which is not guaranteed to be implemented. If not implemented, it will simply return an empty
     * stream.
     *
     * <p>The returned {@link Stream} must be closed, as it will contain references to open file handles.
     *
     * @return A {@link Stream} which walks the full file tree of this root, include all non-class files.
     * @throws IOException If an IO error occurs creating the stream.
     */
    @ApiStatus.Experimental
    default @NotNull Stream<? extends FileDataReference> walkAllFiles() throws IOException {
        return Stream.of();
    }

    /**
     * Return the system root, which allows reading class data for the currently running JVM. This method will return
     * different implementations depending on the version of the JVM currently running, it is compatible with and has
     * been tested against at least Java 8 through Java 17. Later version of Java may also be compatible as well, but
     * that cannot be guaranteed.
     *
     * @return The root corresponding to the currently running JVM's system class files.
     * @throws IOException If an IO error occurs while reading system classes.
     */
    static @NotNull ClassProviderRoot ofJdk() throws IOException {
        return SystemClassProviderRoot.newInstance();
    }

    /**
     * Create a new root from the given directory.
     *
     * @param path The {@link Path path} to use as the root directory.
     * @return A new root from the given directory.
     */
    static @NotNull ClassProviderRoot fromDir(final @NotNull Path path) {
        return new DirClassProviderRoot(path);
    }

    /**
     * Create multiple roots from multiple root directories. This method is simply a convenience method for creating
     * multiple roots individually.
     *
     * @param paths An array of {@link Path paths} to use as root directories for multiple roots.
     * @return A new list of roots corresponding to the array of directories.
     */
    @SuppressWarnings("resource")
    static @NotNull List<@NotNull ClassProviderRoot> fromDirs(final @NotNull Path @NotNull ... paths) {
        final ClassProviderRoot[] roots = new ClassProviderRoot[paths.length];
        for (int i = 0; i < paths.length; i++) {
            roots[i] = new DirClassProviderRoot(paths[i]);
        }
        return Arrays.asList(roots);
    }

    /**
     * Create a new root from the given jar file. This should generally work for any zip file, not just files with the
     * {@code .jar} extension.
     *
     * @param path The {@link Path path} to use as the jar file.
     * @return A new root for the given jar file.
     * @throws IOException If an IO error occurs while trying to open the jar file.
     */
    static @NotNull ClassProviderRoot fromJar(final @NotNull Path path) throws IOException {
        return new JarClassProviderRoot(path);
    }

    /**
     * Create multiple roots from multiple jars. This method is simply a convenience method for creating multiple roots
     * individually.
     *
     * @param paths An array of {@link Path paths} to use as jars for multiple roots.
     * @return A new list of roots corresponding to the array of jars.
     * @throws IOException If an IO error occurs while trying to read one of the jar files.
     */
    @SuppressWarnings("resource")
    static @NotNull List<@NotNull ClassProviderRoot> fromJars(final @NotNull Path @NotNull ... paths) throws IOException {
        final ClassProviderRoot[] roots = new ClassProviderRoot[paths.length];
        for (int i = 0; i < paths.length; i++) {
            roots[i] = new JarClassProviderRoot(paths[i]);
        }
        return Arrays.asList(roots);
    }

    /**
     * A reference to a class file, with the ability to later load the data for the referenced class via the
     * {@link #readData()} method.
     *
     * @see #getAllClasses()
     */
    interface FileDataReference {

        /**
         * The name of the class, in the internal JVM format.
         *
         * @return The name of the class, in the internal JVM format.
         */
        @NotNull String name();

        /**
         * Load the data for the reference class file. The actual reading of the class file does not occur until this
         * method is called. This method can return {@code null} in the unlikely case that the file no longer exists
         * by the time this method is called.
         *
         * @return The raw binary data for the class file this is referencing, or {@code null} if the class file cannot
         * be found anymore.
         * @throws IOException If an IO error occurs while reading the file.
         */
        byte @Nullable [] readData() throws IOException;
    }
}

/**
 * Generic implementation of {@link ClassProviderRoot.FileDataReference} for {@link Path} objects.
 */
final class PathFileDataReference implements ClassProviderRoot.FileDataReference {

    private final @NotNull String name;
    private final @NotNull Path path;

    /**
     * Constructor for {@link PathFileDataReference}.
     *
     * @param name The name of the class this is a reference to.
     * @param path The {@link Path} object referring to the class file.
     */
    PathFileDataReference(final @NotNull String name, final @NotNull Path path) {
        this.name = name;
        this.path = path;
    }

    @Override
    public @NotNull String name() {
        return this.name;
    }

    @Override
    public byte @Nullable [] readData() throws IOException {
        if (Files.notExists(this.path)) {
            return null;
        }
        return Files.readAllBytes(this.path);
    }
}

/**
 * {@link ClassProviderRoot} implementation for directories. Create instances of this class with
 * {@link ClassProviderRoot#fromDir(Path)} or {@link ClassProviderRoot#fromDirs(Path...)}.
 */
final class DirClassProviderRoot implements ClassProviderRoot {

    private final @NotNull Path root;

    /**
     * Constructor for {@link DirClassProviderRoot}. Use {@link ClassProviderRoot#fromDir(Path)} instead.
     *
     * @param root The directory for this root.
     */
    DirClassProviderRoot(final @NotNull Path root) {
        this.root = root;
    }

    @Override
    public byte @Nullable [] getClassData(final @NotNull String fileName) throws IOException {
        final Path file = this.root.resolve(fileName);
        if (Files.exists(file)) {
            return Files.readAllBytes(file);
        }
        return null;
    }

    @Override
    public @NotNull List<? extends FileDataReference> getAllClasses() throws IOException {
        final PathMatcher pathMatcher = this.root.getFileSystem().getPathMatcher("glob:*.class");
        final List<FileDataReference> result;
        try (final Stream<Path> stream = Files.walk(this.root)) {
            result = stream.filter(Files::isRegularFile)
                .filter(p -> pathMatcher.matches(p.getFileName()))
                .map(p -> new PathFileDataReference(this.root.relativize(p).toString(), p))
                .collect(Collectors.toList());
        }
        return result;
    }

    @SuppressWarnings({"resource", "StreamResourceLeak"})
    @Override
    public @NotNull Stream<? extends FileDataReference> walkAllFiles() throws IOException {
        return Files.walk(this.root)
            .filter(Files::isRegularFile)
            .map(p -> new PathFileDataReference(this.root.relativize(p).toString(), p));
    }

    @Override
    public void close() {
    }
}

/**
 * {@link ClassProviderRoot} implementation for jar files. Create instances of this class with
 * {@link ClassProviderRoot#fromJar(Path)} or {@link ClassProviderRoot#fromJars(Path...)}.
 */
final class JarClassProviderRoot implements ClassProviderRoot {

    private final @NotNull FileSystem fileSystem;
    private final @NotNull List<Path> roots;

    /**
     * Constructor for {@link JarClassProviderRoot}. Use {@link ClassProviderRoot#fromJar(Path)} instead.
     *
     * @param jarFile The jar file for this root.
     * @throws IOException If an IO error occurs while opening the jar file.
     */
    JarClassProviderRoot(final @NotNull Path jarFile) throws IOException {
        this.fileSystem = FileSystems.newFileSystem(jarFile, (ClassLoader) null);
        this.roots = new ArrayList<>();
        for (final Path root : this.fileSystem.getRootDirectories()) {
            this.roots.add(root);
        }
    }

    @Override
    public byte @Nullable [] getClassData(@NotNull String fileName) throws IOException {
        for (final Path root : this.roots) {
            final Path file = root.resolve(fileName);
            if (Files.exists(file)) {
                return Files.readAllBytes(file);
            }
        }
        return null;
    }

    @Override
    public @NotNull List<? extends FileDataReference> getAllClasses() throws IOException {
        final PathMatcher pathMatcher = this.fileSystem.getPathMatcher("glob:*.class");
        List<FileDataReference> result = null;
        for (final Path root : this.roots) {
            try (final Stream<Path> stream = Files.walk(root)) {
                final List<FileDataReference> r = stream
                    .filter(Files::isRegularFile)
                    .filter(p -> pathMatcher.matches(p.getFileName()))
                    .map(p -> new PathFileDataReference(root.relativize(p).toString(), p))
                    .collect(Collectors.toList());

                if (result == null) {
                    result = r;
                } else {
                    result.addAll(r);
                }
            }
        }
        return result == null ? Collections.emptyList() : result;
    }

    @SuppressWarnings({"resource", "StreamResourceLeak", "RedundantThrows"})
    @Override
    public @NotNull Stream<? extends FileDataReference> walkAllFiles() throws IOException {
        return this.roots.stream()
            .flatMap(HypoModelUtil.wrapFunction(root ->
                Files.walk(root)
                    .filter(Files::isRegularFile)
                    .map(p -> new PathFileDataReference(root.relativize(p).toString(), p))));
    }

    @Override
    public void close() throws Exception {
        this.fileSystem.close();
    }
}
