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
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * {@link ClassProviderRoot} implementation for system classes for Java 8.
 */
@SuppressWarnings("unused") // Loaded dynamically
class SystemClassProviderRootJdk8 implements ClassProviderRoot {

    private final @NotNull ClassProviderRoot delegate;

    /**
     * Constructor for {@link SystemClassProviderRootJdk8}. Use {@link ClassProviderRoot#ofJdk()} instead.
     *
     * @throws IOException If an IO error occurs while opening JDK modules.
     */
    SystemClassProviderRootJdk8() throws IOException {
        final Class<String> clazz = String.class;
        final String classFileName = '/' + clazz.getName().replace('.', '/') + ".class";
        final URL classFileUrl = clazz.getResource(classFileName);
        if (classFileUrl == null) {
            throw new IllegalStateException("Could not find location of " + classFileName + " file");
        }

        final String jarProtocol = "jar:";
        final String expectedProtocol = jarProtocol + "file:";
        final String classFileUrlString = classFileUrl.toString();
        if (!classFileUrlString.startsWith(expectedProtocol)) {
            throw new IllegalStateException("Unknown protocol: " + classFileUrlString);
        }

        final int index = classFileUrlString.indexOf('!');
        if (index == -1) {
            throw new IllegalStateException("Could not determine where " + classFileUrlString + " refers to");
        }

        final URI rtJarUri = URI.create(classFileUrlString.substring(jarProtocol.length(), index));
        final Path rtJarPath = Paths.get(rtJarUri);
        if (Files.notExists(rtJarPath)) {
            throw new IllegalStateException("JDK jar path is not a file: " + rtJarPath);
        }

        this.delegate = ClassProviderRoot.fromJar(rtJarPath);
    }

    @Override
    public byte @Nullable [] getClassData(@NotNull String fileName) throws IOException {
        return this.delegate.getClassData(fileName);
    }

    @Override
    public @NotNull List<? extends ClassDataReference> getAllClasses() throws IOException {
        return this.delegate.getAllClasses();
    }

    @Override
    public void close() throws Exception {
        this.delegate.close();
    }
}
