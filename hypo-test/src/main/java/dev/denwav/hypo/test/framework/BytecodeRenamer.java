/*
 * Hypo, an extensible and pluggable Java bytecode analytical model.
 *
 * Copyright (C) 2021  Kyle Wood (DenWav)
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

package dev.denwav.hypo.test.framework;

import dev.denwav.hypo.model.ClassProviderRoot;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;

/**
 * A primitive method renamer for the given {@link #delegate}.
 *
 * @param map The map of renames to run over any class retrieved from the {@link #delegate}.
 * @param delegate The {@link ClassProviderRoot} to use as the source for class data.
 */
@SuppressWarnings("InvalidParam") // errorprone doesn't know about record javadoc yet
public record BytecodeRenamer(
    @NotNull Map<String, Map<String, String>> map,
    @NotNull ClassProviderRoot delegate
) implements ClassProviderRoot {

    /**
     * Run the given rename map against the given class {@code data} using {@link SimpleRemapper}.
     *
     * @param data The class data to rename.
     * @param map The map of renames to apply.
     * @return The new class data with the renames applied.
     */
    static byte @NotNull [] rename(final byte @NotNull [] data, final @NotNull Map<String, Map<String, String>> map) {
        final var reader = new ClassReader(data);
        final var writer = new ClassWriter(0);
        final var remapper = new ClassRemapper(writer, new SimpleRemapper(map));
        reader.accept(remapper, 0);

        return writer.toByteArray();
    }

    @Override
    public byte @Nullable [] getClassData(@NotNull String fileName) throws IOException {
        final byte[] data = this.delegate.getClassData(fileName);
        if (data == null) {
            return null;
        }
        return rename(data, this.map);
    }

    @Override
    public @NotNull List<? extends ClassDataReference> getAllClasses() throws IOException {
        return this.delegate.getAllClasses().stream()
            .map(r -> new WrappedClassDataReference(r, this.map))
            .collect(Collectors.toList());
    }

    @Override
    public void close() throws Exception {
        this.delegate.close();
    }

    /**
     * {@link ClassDataReference} implementation for {@link BytecodeRenamer}. This will call
     * {@link #rename(byte[], Map)} on the class data before returning it.
     */
    record WrappedClassDataReference(
        @NotNull ClassProviderRoot.ClassDataReference delegate,
        @NotNull Map<String, Map<String, String>> map
    ) implements ClassDataReference {

        @Override
        public @NotNull String name() {
            return this.delegate.name();
        }

        @Override
        public byte @Nullable [] readData() throws IOException {
            final byte[] data = this.delegate.readData();
            if (data == null) {
                return null;
            }
            return rename(data, this.map);
        }
    }

    /**
     * Primitive remapper used by {@link BytecodeRenamer}. Uses the provided map of renames to rename methods in
     * {@link #mapMethodName(String, String, String)}.
     */
    static final class SimpleRemapper extends Remapper {
        private final @NotNull Map<String, Map<String, String>> map;

        /**
         * Construct a new instance of {@link SimpleRemapper} with the given rename map.
         * @param map The set of renames to apply.
         */
        public SimpleRemapper(final @NotNull Map<String, Map<String, String>> map) {
            this.map = map;
        }

        @Override
        public String mapMethodName(final String owner, final String name, final String descriptor) {
            return this.map.getOrDefault(owner, Map.of()).getOrDefault(name + descriptor, name);
        }
    }
}
