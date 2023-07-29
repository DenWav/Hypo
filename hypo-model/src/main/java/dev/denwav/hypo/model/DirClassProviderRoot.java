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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * {@link ClassProviderRoot} implementation for directories. Create instances of this class with
 * {@link ClassProviderRoot#fromDir(Path)} or {@link ClassProviderRoot#fromDirs(Path...)}.
 */
public final class DirClassProviderRoot implements ClassProviderRoot {

    private final @NotNull Path root;

    /**
     * Constructor for {@link DirClassProviderRoot}. Use {@link ClassProviderRoot#fromDir(Path)} instead.
     *
     * @param root The directory for this root.
     */
    DirClassProviderRoot(final @NotNull Path root) {
        this.root = root;
    }

    /**
     * Returns the directory used by this directory class provider root.
     * @return The directory used by this directory class provider root.
     */
    public @NotNull Path getDir() {
        return this.root;
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
