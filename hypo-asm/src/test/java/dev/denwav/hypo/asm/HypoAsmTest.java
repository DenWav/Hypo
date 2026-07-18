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

import dev.denwav.hypo.core.HypoConfig;
import dev.denwav.hypo.core.HypoContext;
import dev.denwav.hypo.model.data.ClassData;
import dev.denwav.hypo.types.desc.ClassTypeDescriptor;
import dev.denwav.hypo.types.desc.TypeDescriptor;
import java.io.IOException;
import java.util.stream.Stream;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("[asm] HypoAsm Tests")
public class HypoAsmTest {

    private static Path testJar;

    private static class CustomTestException extends Exception {
        public CustomTestException(final String message) {
            super(message);
        }
    }

    @BeforeAll
    public static void setupSpec() {
        final String jarPath = System.getProperty("scenario-01");
        Assertions.assertNotNull(jarPath, "System property 'scenario-01' is not set. Make sure to run this via Gradle.");
        testJar = Paths.get(jarPath);
        Assertions.assertTrue(testJar.toFile().exists(), "Test jar does not exist at " + testJar);
    }

    @Test
    @DisplayName("context(Path) should create a valid context with JVM classes")
    public void testContextPath() throws IOException {
        try (final HypoContext context = HypoAsm.context(testJar)) {
            Assertions.assertNotNull(context);
            // Check that the jar class is available in target provider
            final ClassData classData = context.getProvider().findClass("scenario01.TestClass");
            Assertions.assertNotNull(classData);
            // Check that JVM classes are available in context provider (since withJvm is true by default)
            final ClassData jvmClass = context.getContextProvider().findClass("java.lang.Object");
            Assertions.assertNotNull(jvmClass);
        }
    }

    @Test
    @DisplayName("context(Collection) should create a valid context with JVM classes")
    public void testContextCollection() throws IOException {
        try (final HypoContext context = HypoAsm.context(List.of(testJar))) {
            Assertions.assertNotNull(context);
            final ClassData classData = context.getProvider().findClass("scenario01.TestClass");
            Assertions.assertNotNull(classData);
            final ClassData jvmClass = context.getContextProvider().findClass("java.lang.Object");
            Assertions.assertNotNull(jvmClass);
        }
    }

    @Test
    @DisplayName("context(Path, boolean, HypoConfig) should respect withJvm and config parameters")
    public void testContextPathWithParams() throws IOException {
        // Test withJvm = true
        try (final HypoContext context = HypoAsm.context(testJar, true, null)) {
            final ClassData jvmClass = context.getContextProvider().findClass("java.lang.Object");
            Assertions.assertNotNull(jvmClass);
        }

        // Test withJvm = false
        try (final HypoContext context = HypoAsm.context(testJar, false, null)) {
            final ClassData jvmClass = context.getContextProvider().findClass("java.lang.Object");
            Assertions.assertNull(jvmClass);
        }

        // Test config is respected
        final HypoConfig config = HypoConfig.builder().setRequireFullClasspath(true).build();
        try (final HypoContext context = HypoAsm.context(testJar, true, config)) {
            Assertions.assertEquals(config, context.getConfig());
        }
    }

    @SuppressWarnings("resource")
    @Test
    @DisplayName("context(Collection, boolean, HypoConfig) should respect parameters and throw on empty collection")
    public void testContextCollectionWithParams() throws IOException {
        // Test withJvm = true
        try (final HypoContext context = HypoAsm.context(List.of(testJar), true, null)) {
            final ClassData jvmClass = context.getContextProvider().findClass("java.lang.Object");
            Assertions.assertNotNull(jvmClass);
        }

        // Test withJvm = false
        try (final HypoContext context = HypoAsm.context(List.of(testJar), false, null)) {
            final ClassData jvmClass = context.getContextProvider().findClass("java.lang.Object");
            Assertions.assertNull(jvmClass);
        }

        // Test config is respected
        final HypoConfig config = HypoConfig.builder().setRequireFullClasspath(true).build();
        try (final HypoContext context = HypoAsm.context(List.of(testJar), true, config)) {
            Assertions.assertEquals(config, context.getConfig());
        }

        // Test empty jars collection throws IllegalArgumentException
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            HypoAsm.context(Collections.emptyList(), true, null);
        });
    }

    @Test
    @DisplayName("run(Path, ThrowingConsumer) should execute consumer, pass context, close it, and propagate exception")
    public void testRunPath() {
        final AtomicBoolean executed = new AtomicBoolean(false);
        HypoAsm.run(testJar, context -> {
            Assertions.assertNotNull(context);
            final ClassData classData = context.getProvider().findClass("scenario01.TestClass");
            Assertions.assertNotNull(classData);
            executed.set(true);
        });
        Assertions.assertTrue(executed.get());

        // Test custom checked exception propagation
        Assertions.assertThrows(CustomTestException.class, () -> {
            HypoAsm.run(testJar, context -> {
                throw new CustomTestException("test exception");
            });
        });

        // Test IOException is wrapped in UncheckedIOException
        Assertions.assertThrows(UncheckedIOException.class, () -> {
            HypoAsm.run(testJar, context -> {
                throw new IOException("test exception");
            });
        });
    }

    @Test
    @DisplayName("run(Path, boolean, HypoConfig, ThrowingConsumer) should respect params and propagate exception")
    public void testRunPathWithParams() {
        final AtomicBoolean executed = new AtomicBoolean(false);
        final HypoConfig config = HypoConfig.builder().build();
        HypoAsm.run(testJar, false, config, context -> {
            Assertions.assertNotNull(context);
            Assertions.assertEquals(config, context.getConfig());
            // JVM classpath should not be included
            final ClassData jvmClass = context.getContextProvider().findClass("java.lang.Object");
            Assertions.assertNull(jvmClass);
            executed.set(true);
        });
        Assertions.assertTrue(executed.get());

        // Test custom checked exception propagation
        Assertions.assertThrows(CustomTestException.class, () -> {
            HypoAsm.run(testJar, false, config, context -> {
                throw new CustomTestException("test exception");
            });
        });

        // Test IOException is wrapped in UncheckedIOException
        Assertions.assertThrows(UncheckedIOException.class, () -> {
            HypoAsm.run(testJar, false, config, context -> {
                throw new IOException("test exception");
            });
        });
    }

    @Test
    @DisplayName("run(Collection, boolean, HypoConfig, ThrowingConsumer) should respect params and propagate exception")
    public void testRunCollectionWithParams() {
        final AtomicBoolean executed = new AtomicBoolean(false);
        final HypoConfig config = HypoConfig.builder().build();
        HypoAsm.run(List.of(testJar), false, config, context -> {
            Assertions.assertNotNull(context);
            Assertions.assertEquals(config, context.getConfig());
            final ClassData jvmClass = context.getContextProvider().findClass("java.lang.Object");
            Assertions.assertNull(jvmClass);
            executed.set(true);
        });
        Assertions.assertTrue(executed.get());

        // Test custom checked exception propagation
        Assertions.assertThrows(CustomTestException.class, () -> {
            HypoAsm.run(List.of(testJar), false, config, context -> {
                throw new CustomTestException("test exception");
            });
        });

        // Test IOException is wrapped in UncheckedIOException
        Assertions.assertThrows(UncheckedIOException.class, () -> {
            HypoAsm.run(List.of(testJar), false, config, context -> {
                throw new IOException("test exception");
            });
        });
    }

    @Test
    @DisplayName("use(Path, ThrowingFunction) should execute function, return result, close context, and propagate exception")
    public void testUsePath() {
        final String result = HypoAsm.use(testJar, context -> {
            Assertions.assertNotNull(context);
            final ClassData classData = context.getProvider().findClass("scenario01.TestClass");
            Assertions.assertNotNull(classData);
            return "success";
        });
        Assertions.assertEquals("success", result);

        // Test custom checked exception propagation
        Assertions.assertThrows(CustomTestException.class, () -> {
            HypoAsm.use(testJar, context -> {
                throw new CustomTestException("test exception");
            });
        });

        // Test IOException is wrapped in UncheckedIOException
        Assertions.assertThrows(UncheckedIOException.class, () -> {
            HypoAsm.use(testJar, context -> {
                throw new IOException("test exception");
            });
        });
    }

    @Test
    @DisplayName("use(Path, boolean, HypoConfig, ThrowingFunction) should respect params, return result, and propagate exception")
    public void testUsePathWithParams() {
        final HypoConfig config = HypoConfig.builder().build();
        final String result = HypoAsm.use(testJar, false, config, context -> {
            Assertions.assertNotNull(context);
            Assertions.assertEquals(config, context.getConfig());
            final ClassData jvmClass = context.getContextProvider().findClass("java.lang.Object");
            Assertions.assertNull(jvmClass);
            return "success";
        });
        Assertions.assertEquals("success", result);

        // Test custom checked exception propagation
        Assertions.assertThrows(CustomTestException.class, () -> {
            HypoAsm.use(testJar, false, config, context -> {
                throw new CustomTestException("test exception");
            });
        });

        // Test IOException is wrapped in UncheckedIOException
        Assertions.assertThrows(UncheckedIOException.class, () -> {
            HypoAsm.use(testJar, false, config, context -> {
                throw new IOException("test exception");
            });
        });
    }

    @Test
    @DisplayName("use(Collection, boolean, HypoConfig, ThrowingFunction) should respect params, return result, and propagate exception")
    public void testUseCollectionWithParams() {
        final HypoConfig config = HypoConfig.builder().build();
        final String result = HypoAsm.use(List.of(testJar), false, config, context -> {
            Assertions.assertNotNull(context);
            Assertions.assertEquals(config, context.getConfig());
            final ClassData jvmClass = context.getContextProvider().findClass("java.lang.Object");
            Assertions.assertNull(jvmClass);
            return "success";
        });
        Assertions.assertEquals("success", result);

        // Test custom checked exception propagation
        Assertions.assertThrows(CustomTestException.class, () -> {
            HypoAsm.use(List.of(testJar), false, config, context -> {
                throw new CustomTestException("test exception");
            });
        });

        // Test IOException is wrapped in UncheckedIOException
        Assertions.assertThrows(UncheckedIOException.class, () -> {
            HypoAsm.use(List.of(testJar), false, config, context -> {
                throw new IOException("test exception");
            });
        });
    }

    @Test
    @DisplayName("Test HypoContext convenience methods (findClass, allClasses, stream)")
    public void testHypoContextConvenienceMethods() throws IOException {
        try (final HypoContext context = HypoAsm.context(testJar)) {
            // findClass(String)
            final ClassData classByStr = context.findClass("scenario01.TestClass");
            Assertions.assertNotNull(classByStr);
            Assertions.assertEquals("scenario01/TestClass", classByStr.name());

            final ClassData jvmClassByStr = context.findClass("java.lang.Object");
            Assertions.assertNotNull(jvmClassByStr);
            Assertions.assertEquals("java/lang/Object", jvmClassByStr.name());

            Assertions.assertNull(context.findClass("nonexistent.Class"));
            Assertions.assertNull(context.findClass((String) null));

            // findClass(TypeDescriptor)
            final TypeDescriptor typeDesc = ClassTypeDescriptor.of("scenario01.TestClass");
            final ClassData classById = context.findClass(typeDesc);
            Assertions.assertNotNull(classById);
            Assertions.assertEquals("scenario01/TestClass", classById.name());

            final TypeDescriptor jvmTypeDesc = ClassTypeDescriptor.of("java.lang.Object");
            final ClassData jvmClassById = context.findClass(jvmTypeDesc);
            Assertions.assertNotNull(jvmClassById);
            Assertions.assertEquals("java/lang/Object", jvmClassById.name());

            Assertions.assertNull(context.findClass(ClassTypeDescriptor.of("nonexistent.Class")));
            Assertions.assertNull(context.findClass((TypeDescriptor) null));

            // allClasses()
            final Iterable<ClassData> iterable = context.allClasses();
            Assertions.assertNotNull(iterable);
            boolean foundInIterable = false;
            for (final ClassData data : iterable) {
                if ("scenario01/TestClass".equals(data.name())) {
                    foundInIterable = true;
                    break;
                }
            }
            Assertions.assertTrue(foundInIterable, "scenario01/TestClass should be present in allClasses()");

            // stream()
            try (final Stream<ClassData> stream = context.stream()) {
                Assertions.assertNotNull(stream);
                final boolean foundInStream = stream.anyMatch(data -> "scenario01/TestClass".equals(data.name()));
                Assertions.assertTrue(foundInStream, "scenario01/TestClass should be present in stream()");
            }
        }
    }
}
