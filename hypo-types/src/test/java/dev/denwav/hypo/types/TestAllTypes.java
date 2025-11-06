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

package dev.denwav.hypo.types;

import dev.denwav.hypo.types.desc.MethodDescriptor;
import dev.denwav.hypo.types.desc.TypeDescriptor;
import dev.denwav.hypo.types.sig.ClassSignature;
import dev.denwav.hypo.types.sig.MethodSignature;
import dev.denwav.hypo.types.sig.TypeSignature;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestAllTypes {

    private static FileSystem fs;
    private static Path root;

    @BeforeAll
    static void setup() throws IOException {
        final Path typesZip = Path.of(System.getProperty("hypo.types.zip"));
        fs = FileSystems.newFileSystem(typesZip);
        root = fs.getPath("/");
    }

    @AfterAll
    static void teardown() throws IOException {
        fs.close();
    }

    @Test
    void testJvmTypes() throws XMLStreamException, IOException {
        this.doTest(root.resolve("jvm.xml"));
    }

    @Test
    void testSpringTypes() throws XMLStreamException, IOException {
        this.doTest(root.resolve("spring.xml"));
    }

    @Test
    void testGuavaTypes() throws XMLStreamException, IOException {
        this.doTest(root.resolve("guava.xml"));
    }

    @Test
    void testEclipseTypes() throws XMLStreamException, IOException {
        this.doTest(root.resolve("eclipse.xml"));
    }


    private void doTest(final Path xmlFile) throws IOException, XMLStreamException {
        try (final BufferedReader reader = Files.newBufferedReader(xmlFile, StandardCharsets.UTF_8)) {
            final XMLInputFactory factory = XMLInputFactory.newFactory();
            final XMLEventReader eventReader = factory.createXMLEventReader(reader);
            this.doTest(eventReader);
        }
    }

    private void doTest(final XMLEventReader reader) throws XMLStreamException {
        ClassAttributes currentClass = null;
        MemberAttributes currentMethod = null;

        long classCounter = 0L;
        long fieldCounter = 0L;
        long methodCounter = 0L;
        long localCounter = 0L;

        while (reader.hasNext()) {
            final XMLEvent event = reader.nextEvent();

            if (event.isStartElement()) {
                final StartElement startElement = event.asStartElement();
                switch (startElement.getName().getLocalPart()) {
                    case "class":
                        currentClass = this.getClassAttributes(startElement);
                        this.testClass(currentClass);
                        classCounter++;
                        break;
                    case "method":
                        currentMethod = this.getMemberAttributes(startElement);
                        this.testMethod(currentClass, currentMethod);
                        methodCounter++;
                        break;
                    case "field":
                        final MemberAttributes field = this.getMemberAttributes(startElement);
                        this.testField(currentClass, field);
                        fieldCounter++;
                        break;
                    case "local":
                        final MemberAttributes local = this.getMemberAttributes(startElement);
                        this.testLocal(currentClass, currentMethod, local);
                        localCounter++;
                        break;
                }
            }
        }

        System.out.printf("Tested classes: %,d%n", classCounter);
        System.out.printf("Tested fields: %,d%n", fieldCounter);
        System.out.printf("Tested methods: %,d%n", methodCounter);
        System.out.printf("Tested locals: %,d%n", localCounter);
    }

    private void testClass(final ClassAttributes attr) {
        if (attr.signature() == null) {
            return;
        }
        final ClassSignature sig = ClassSignature.parse(attr.signature());
        assertEquals(attr.signature(), sig.asInternal(), "Failed for class: " + attr.name());
    }

    private void testField(final ClassAttributes currentClass, final MemberAttributes attr) {
        final Supplier<String> failureMsg = () -> "Failed for field: " + attr.name() + " in class: " + currentClass.name();
        final TypeDescriptor desc = TypeDescriptor.parse(attr.descriptor());
        assertEquals(attr.descriptor(), desc.asInternal(), failureMsg);

        if (attr.signature() == null) {
            return;
        }

        final TypeSignature sig = TypeSignature.parse(attr.signature());
        assertEquals(attr.signature(), sig.asInternal(), failureMsg);
    }

    private void testMethod(final ClassAttributes currentClass, final MemberAttributes attr) {
        final Supplier<String> failureMsg = () -> "Failed for method: " + attr.name() + " in class: " + currentClass.name();
        final MethodDescriptor desc = MethodDescriptor.parse(attr.descriptor());
        assertEquals(attr.descriptor(), desc.asInternal(), failureMsg);

        if (attr.signature() == null) {
            return;
        }

        final MethodSignature sig = MethodSignature.parse(attr.signature());
        assertEquals(attr.signature(), sig.asInternal(), failureMsg);
    }

    private void testLocal(final ClassAttributes currentClass,  final MemberAttributes currentMethod, final MemberAttributes attr) {
        final Supplier<String> failureMsg = () -> "Failed for local: " + attr.name() + " in method: " + currentMethod.name() + " in class: " + currentClass.name();
        final TypeDescriptor desc = TypeDescriptor.parse(attr.descriptor());
        assertEquals(attr.descriptor(), desc.asInternal(), failureMsg);

        if (attr.signature() == null) {
            return;
        }

        final TypeSignature sig = TypeSignature.parse(attr.signature());
        assertEquals(attr.signature(), sig.asInternal(), failureMsg);
    }

    private static final QName nameAttr = new QName("name");
    private static final QName descAttr = new QName("descriptor");
    private static final QName sigAttr = new QName("signature");

    private ClassAttributes getClassAttributes(final StartElement element) {
        final String className = element.getAttributeByName(nameAttr).getValue();
        final Attribute classSigAttr = element.getAttributeByName(sigAttr);
        final String classSig;
        if (classSigAttr != null) {
            classSig = classSigAttr.getValue();
        } else {
            classSig = null;
        }
        return new ClassAttributes(className, classSig);
    }

    private MemberAttributes getMemberAttributes(final StartElement element) {
        final String memberName = element.getAttributeByName(nameAttr).getValue();
        final String memberDesc = element.getAttributeByName(descAttr).getValue();
        final Attribute memberSigAttr = element.getAttributeByName(sigAttr);
        final String memberSig;
        if (memberSigAttr != null) {
            memberSig = memberSigAttr.getValue();
        } else {
            memberSig = null;
        }
        return new MemberAttributes(memberName, memberDesc, memberSig);
    }

    private record ClassAttributes(String name, @Nullable String signature) {}
    private record MemberAttributes(String name, String descriptor, @Nullable String signature) {}
}
