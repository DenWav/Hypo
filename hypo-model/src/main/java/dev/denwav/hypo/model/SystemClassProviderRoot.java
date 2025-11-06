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
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReader;
import java.lang.module.ModuleReference;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * {@link ClassProviderRoot} implementation for system classes for Java 9+. Tested on JDKs up to Java 23.
 */
/* package */ final class SystemClassProviderRoot implements ClassProviderRoot {

    private final @NotNull List<ModuleReader> readers;

    /**
     * Constructor for {@link SystemClassProviderRoot}. Use {@link ClassProviderRoot#ofJdk()} instead.
     *
     * @throws IOException If an IO error occurs while opening JDK modules.
     */
    SystemClassProviderRoot() throws IOException {
        final Set<ModuleReference> refs = ModuleFinder.ofSystem().findAll();
        final ModuleReader[] readers = new ModuleReader[refs.size()];
        int index = 0;
        for (final ModuleReference ref : refs) {
            @SuppressWarnings("resource") final ModuleReader openedReader = ref.open();
            readers[index++] = openedReader;
        }
        this.readers = Arrays.asList(readers);
    }

    @Override
    public byte @Nullable [] getClassData(@NotNull String fileName) throws IOException {
        for (final ModuleReader reader : this.readers) {
            final ByteBuffer resource = reader.read(fileName).orElse(null);
            if (resource == null) {
                continue;
            }
            try {
                final byte[] data = new byte[resource.remaining()];
                resource.get(data);
                return data;
            } finally {
                reader.release(resource);
            }
        }
        return null;
    }

    @Override
    public @NotNull List<? extends ClassProviderRoot.FileDataReference> getAllClasses() throws IOException {
        List<ClassProviderRoot.FileDataReference> refs = null;

        for (final ModuleReader reader : this.readers) {
            final List<ClassProviderRoot.FileDataReference> list = reader.list()
                .filter(n -> n.endsWith(".class"))
                .map(n -> new SystemFileDataReference(n, reader))
                .collect(Collectors.toList());

            if (refs == null) {
                refs = list;
            } else {
                refs.addAll(list);
            }
        }
        return refs == null ? Collections.emptyList() : refs;
    }

    @Override
    public void close() throws IOException {
        IOException thrown = null;
        for (final ModuleReader reader : this.readers) {
            try {
                reader.close();
            } catch (final IOException e) {
                thrown = HypoModelUtil.addSuppressed(thrown, e);
            }
        }
        if (thrown != null) {
            throw thrown;
        }
    }

    /**
     * Implementation of {@link ClassProviderRoot.FileDataReference} for {@link SystemClassProviderRoot}.
     */
    static final class SystemFileDataReference implements ClassProviderRoot.FileDataReference {

        private final @NotNull String name;
        private final @NotNull ModuleReader reader;

        /**
         * Construct a new instance of {@link SystemFileDataReference}.
         *
         * @param name The file name.
         * @param reader The {@link ModuleReader} to read the class from.
         */
        SystemFileDataReference(final @NotNull String name, final @NotNull ModuleReader reader) {
            this.name = name;
            this.reader = reader;
        }

        @Override
        public @NotNull String name() {
            return this.name;
        }

        @Override
        public byte @Nullable [] readData() throws IOException {
            final ByteBuffer resource = this.reader.read(this.name).orElse(null);
            if (resource == null) {
                return null;
            }
            try {
                final byte[] data = new byte[resource.remaining()];
                resource.get(data);
                return data;
            } finally {
                this.reader.release(resource);
            }
        }
    }
}
