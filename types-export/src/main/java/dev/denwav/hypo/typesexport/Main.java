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

package dev.denwav.hypo.typesexport;

import dev.denwav.hypo.asm.AsmClassData;
import dev.denwav.hypo.asm.AsmClassDataProvider;
import dev.denwav.hypo.core.HypoContext;
import dev.denwav.hypo.model.ClassProviderRoot;
import dev.denwav.hypo.model.data.ClassData;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import javanet.staxutils.IndentingXMLStreamWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;

public final class Main {

    public static void main(final String[] args) throws IOException, XMLStreamException {
        if (args.length < 2) {
            throw new RuntimeException("No output file given");
        }

        final String mode = args[0];
        final Path outputFile = Path.of(args[1]).toAbsolutePath();
        Files.createDirectories(outputFile.getParent());

        final var root = switch (mode) {
            case "jvm" -> List.of(ClassProviderRoot.ofJdk());
            case "jar" -> ClassProviderRoot.fromJars(Stream.of(System.getProperty("hypo.jar.path").split(":")).map(Path::of).toArray(Path[]::new));
            default -> throw new IllegalStateException("Unknown mode: " + mode);
        };

        try (
            final BufferedWriter writer = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8)
        ) {
            final var factory = XMLOutputFactory.newFactory();
            final var streamWriter = new IndentingXMLStreamWriter(factory.createXMLStreamWriter(writer));

            streamWriter.writeStartDocument();
            streamWriter.writeStartElement("classes");

            try (final HypoContext ctx = HypoContext.builder()
                .withProvider(AsmClassDataProvider.of(root))
                .build()) {

                final Iterator<ClassData> it = ctx.getProvider().stream()
                    .sorted(Comparator.comparing(ClassData::name))
                    .iterator();

                while (it.hasNext()) {
                    final AsmClassData clazz = (AsmClassData) it.next();
                    final ClassNode node = clazz.getNode();
                    if (node.fields.isEmpty() && node.methods.isEmpty()) {
                        continue;
                    }

                    streamWriter.writeStartElement("class");

                    streamWriter.writeAttribute("name", node.name);
                    if (node.signature != null) {
                        streamWriter.writeAttribute("signature", node.signature);
                    }

                    if (!node.fields.isEmpty()) {
                        streamWriter.writeStartElement("fields");
                        for (final FieldNode field : node.fields) {
                            streamWriter.writeEmptyElement("field");
                            streamWriter.writeAttribute("name", field.name);
                            streamWriter.writeAttribute("descriptor", field.desc);
                            if (field.signature != null) {
                                streamWriter.writeAttribute("signature", field.signature);
                            }
                        }
                        streamWriter.writeEndElement(); // fields
                    }

                    if (!node.methods.isEmpty()) {
                        streamWriter.writeStartElement("methods");
                        for (final MethodNode method : node.methods) {
                            if (method.localVariables != null && !method.localVariables.isEmpty()) {
                                streamWriter.writeStartElement("method");
                            } else {
                                streamWriter.writeEmptyElement("method");
                            }
                            streamWriter.writeAttribute("name", method.name);
                            streamWriter.writeAttribute("descriptor", method.desc);
                            if (method.signature != null) {
                                streamWriter.writeAttribute("signature", method.signature);
                            }

                            if (method.localVariables != null) {
                                streamWriter.writeStartElement("locals");
                                for (final LocalVariableNode local : method.localVariables) {
                                    streamWriter.writeEmptyElement("local");
                                    streamWriter.writeAttribute("name", local.name);
                                    streamWriter.writeAttribute("descriptor", local.desc);
                                    if (local.signature != null) {
                                        streamWriter.writeAttribute("signature", local.signature);
                                    }
                                }
                                streamWriter.writeEndElement(); // locals
                            }
                            if (method.localVariables != null && !method.localVariables.isEmpty()) {
                                streamWriter.writeEndElement(); // method
                            }
                        }
                        streamWriter.writeEndElement(); // methods
                    }

                    streamWriter.writeEndElement(); // class
                }
            }

            streamWriter.writeEndElement(); // classes

            streamWriter.writeEndDocument();
            streamWriter.close();
        }
    }
}
