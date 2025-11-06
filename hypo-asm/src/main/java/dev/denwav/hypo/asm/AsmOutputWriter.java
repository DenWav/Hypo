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

package dev.denwav.hypo.asm;

import dev.denwav.hypo.core.HypoContext;
import dev.denwav.hypo.core.HypoOutputWriter;
import dev.denwav.hypo.model.ClassProviderRoot;
import dev.denwav.hypo.model.JarClassProviderRoot;
import dev.denwav.hypo.model.data.ClassData;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.ToIntFunction;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

/**
 * Implementation of {@link HypoOutputWriter} for {@link AsmClassDataProvider ASM providers}.
 */
@SuppressWarnings("ClassCanBeRecord")
public final class AsmOutputWriter implements HypoOutputWriter {

    private final @NotNull Path outputFile;
    private final @NotNull ToIntFunction<ClassNode> writerFlags;

    private AsmOutputWriter(final @NotNull Path outputFile, final @NotNull ToIntFunction<ClassNode> writerFlags) {
        this.outputFile = outputFile;
        this.writerFlags = writerFlags;
    }

    /**
     * Create a new instance of a {@link AsmOutputWriter} which writes to the given file as a jar file.
     *
     * @param output The jar file to write to. Must not already exist.
     * @param writerFlags Function which provides writer flags for a given {@link ClassNode}.
     * @return The new {@link AsmOutputWriter}.
     */
    public static @NotNull AsmOutputWriter to(final @NotNull Path output, final @NotNull ToIntFunction<ClassNode> writerFlags) {
        if (Files.exists(output)) {
            throw new IllegalArgumentException("Cannot write to jar file, as it already exists: " + output);
        }
        return new AsmOutputWriter(output, writerFlags);
    }

    @Override
    public void write(final @NotNull HypoContext context) throws IOException {
        createParentDirectories(this.outputFile);

        try (
            final OutputStream out = Files.newOutputStream(this.outputFile);
            final BufferedOutputStream bos = new BufferedOutputStream(out);
            final ZipOutputStream zos = new ZipOutputStream(bos)
        ) {
            for (final ClassProviderRoot root : context.getProvider().roots()) {
                if (root instanceof final JarClassProviderRoot jarRoot) {
                    final Path jarFile = jarRoot.getJarFile();
                    try (
                        final InputStream in = Files.newInputStream(jarFile);
                        final BufferedInputStream bis = new BufferedInputStream(in);
                        final ZipInputStream zis = new ZipInputStream(bis)
                    ) {
                        ZipEntry entry;
                        while ((entry = zis.getNextEntry()) != null) {
                            final ZipEntry outEntry = new ZipEntry(entry);
                            zos.putNextEntry(outEntry);
                            try {
                                this.writeFile(context, outEntry, zis, zos);
                            } finally {
                                zos.closeEntry();
                            }
                        }
                    }
                }
            }
        }
    }

    private void writeFile(
        final @NotNull HypoContext context,
        final @NotNull ZipEntry entry,
        final @NotNull ZipInputStream in,
        final @NotNull ZipOutputStream out
    ) throws IOException {
        if (entry.isDirectory()) {
            return;
        }
        if (!entry.getName().endsWith(".class")) {
            // simple copy
            copy(in, out);
            return;
        }

        final ClassData data = context.getProvider().findClass(removeClassSuffix(entry.getName()));
        if (data == null) {
            throw new IllegalStateException("Can no longer find " + entry.getName());
        }
        if (!(data instanceof AsmClassData)) {
            throw new IllegalStateException("Cannot handle ClassData objects which are not " +
                AsmClassData.class.getName() + " - found: " + data.getClass().getName());
        }

        final ClassNode node = ((AsmClassData) data).getNode();
        final ClassWriter writer = new ClassWriter(this.writerFlags.applyAsInt(node));
        node.accept(writer);

        final byte[] classBytes = writer.toByteArray();
        out.write(classBytes);
    }

    private static void copy(final InputStream in, final OutputStream out) throws IOException {
        final byte[] data = new byte[8096];
        int read;
        while ((read = in.read(data)) > 0) {
            out.write(data, 0, read);
        }
    }

    private static @NotNull String removeClassSuffix(final @NotNull String text) {
        // .class is 6 characters
        return text.substring(0, text.length() - 6);
    }

    private static void createParentDirectories(final @NotNull Path path) throws IOException {
        final Path parent = path.getParent();
        if (parent == null) {
            return;
        }
        Files.createDirectories(parent);
    }
}
