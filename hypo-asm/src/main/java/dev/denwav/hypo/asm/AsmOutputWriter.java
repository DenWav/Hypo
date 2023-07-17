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
import dev.denwav.hypo.model.HypoModelUtil;
import dev.denwav.hypo.model.data.ClassData;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

/**
 * Implementation of {@link HypoOutputWriter} for {@link AsmClassDataProvider ASM providers}.
 */
public final class AsmOutputWriter implements HypoOutputWriter {

    private final @NotNull Path outputFile;

    private AsmOutputWriter(final @NotNull Path outputFile) {
        this.outputFile = outputFile;
    }

    /**
     * Create a new instance of a {@link AsmOutputWriter} which writes to the given file as a jar file.
     *
     * @param output The jar file to write to. Must not already exist.
     * @return The new {@link AsmOutputWriter}.
     */
    public static @NotNull AsmOutputWriter to(final @NotNull Path output) {
        if (Files.exists(output)) {
            throw new IllegalArgumentException("Cannot write to jar file, as it already exists: " + output);
        }
        return new AsmOutputWriter(output);
    }

    @Override
    public void write(final @NotNull HypoContext context) throws IOException {
        createParentDirectories(this.outputFile);

        final HashMap<String, Object> options = new HashMap<>();
        options.put("create", true);
        try (final FileSystem fs = FileSystems.newFileSystem(URI.create("jar:" + this.outputFile.toUri()), options)) {
            final Path outputRoot = fs.getPath("/");

            for (final ClassProviderRoot root : context.getProvider().roots()) {
                try (final Stream<? extends ClassProviderRoot.FileDataReference> stream = root.walkAllFiles()) {
                    stream.forEach(HypoModelUtil.wrapConsumer(f -> {
                        this.writeFile(context, outputRoot, f);
                    }));
                }
            }
        }
    }

    private void writeFile(
        final @NotNull HypoContext context,
        final @NotNull Path outputRoot,
        final @NotNull ClassProviderRoot.FileDataReference ref
    ) throws IOException {
        final Path output = outputRoot.resolve(ref.name());
        createParentDirectories(output);

        if (!ref.name().endsWith(".class")) {
            // simple copy
            final byte[] data = ref.readData();
            if (data == null) {
                throw new IllegalStateException("Can no longer find " + ref.name());
            }
            Files.write(output, data);
            return;
        }

        final ClassData data = context.getProvider().findClass(removeClassSuffix(ref.name()));
        if (data == null) {
            throw new IllegalStateException("Can no longer find " + ref.name());
        }
        if (!(data instanceof AsmClassData)) {
            throw new IllegalStateException("Cannot handle ClassData objects which are not " +
                AsmClassData.class.getName() + " - found: " + data.getClass().getName());
        }

        final ClassNode node = ((AsmClassData) data).getNode();
        final ClassWriter writer = new ClassWriter(0);
        node.accept(writer);

        final byte[] classBytes = writer.toByteArray();
        Files.write(output, classBytes);
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
