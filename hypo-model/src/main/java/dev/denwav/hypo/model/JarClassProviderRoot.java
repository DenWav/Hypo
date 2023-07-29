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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * {@link ClassProviderRoot} implementation for jar files. Create instances of this class with
 * {@link ClassProviderRoot#fromJar(Path)} or {@link ClassProviderRoot#fromJars(Path...)}.
 */
public final class JarClassProviderRoot implements ClassProviderRoot {

    private final @NotNull Path jarFile;
    private final @NotNull FileSystem fileSystem;
    private final @NotNull List<Path> roots;

    /**
     * Constructor for {@link JarClassProviderRoot}. Use {@link ClassProviderRoot#fromJar(Path)} instead.
     *
     * @param jarFile The jar file for this root.
     * @throws IOException If an IO error occurs while opening the jar file.
     */
    JarClassProviderRoot(final @NotNull Path jarFile) throws IOException {
        this.jarFile = jarFile;
        this.fileSystem = FileSystems.newFileSystem(jarFile, (ClassLoader) null);
        this.roots = new ArrayList<>();
        for (final Path root : this.fileSystem.getRootDirectories()) {
            this.roots.add(root);
        }
    }

    /**
     * Returns the jar file used by this class provider root.
     * @return The jar file used by this class provider root.
     */
    public @NotNull Path getJarFile() {
        return this.jarFile;
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
