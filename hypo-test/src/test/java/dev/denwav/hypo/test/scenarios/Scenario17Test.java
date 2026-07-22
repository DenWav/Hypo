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

package dev.denwav.hypo.test.scenarios;

import dev.denwav.hypo.asm.AsmClassDataProvider;
import dev.denwav.hypo.asm.hydrate.SuperConstructorHydrator;
import dev.denwav.hypo.core.HypoConfig;
import dev.denwav.hypo.core.HypoContext;
import dev.denwav.hypo.hydrate.HydrationManager;
import dev.denwav.hypo.hydrate.HydrationProvider;
import dev.denwav.hypo.hydrate.generic.HypoHydration;
import dev.denwav.hypo.model.ClassProviderRoot;
import dev.denwav.hypo.model.data.ClassData;
import dev.denwav.hypo.model.data.MethodData;
import dev.denwav.hypo.test.framework.TestScenarioBase;
import java.io.IOException;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SuppressWarnings("SameParameterValue")
@DisplayName("[integration] Scenario 17 - SuperConstructorHydrator Error Handling")
public class Scenario17Test extends TestScenarioBase {

    @Override
    public @NotNull Env env() {
        return new Env() {
            @Override
            public @NotNull String forContext() {
                return "scenario-17";
            }

            @Override
            public @NotNull Iterable<HydrationProvider<?>> hydration() {
                return List.of(SuperConstructorHydrator.create());
            }

            @Override
            public boolean includeJdk() {
                return true;
            }
        };
    }

    // -------------------------------------------------------------------------
    // Bytecode patching helpers
    // -------------------------------------------------------------------------

    private static byte[] readClassFromScenarioJar(final String className) throws IOException {
        final String jarPath = System.getProperty("scenario-17");
        assertNotNull(jarPath, "System property 'scenario-17' must be set by the Gradle test task");
        try (final JarFile jar = new JarFile(jarPath)) {
            final JarEntry entry = jar.getJarEntry(className + ".class");
            assertNotNull(entry, "Expected '" + className + ".class' inside scenario-17 jar");
            return jar.getInputStream(entry).readAllBytes();
        }
    }

    private static byte[] patchSuperCallOwner(final byte[] classBytes, final String newOwner) {
        final ClassReader reader = new ClassReader(classBytes);
        final ClassWriter writer = new ClassWriter(0);
        reader.accept(new ClassVisitor(Opcodes.ASM9, writer) {
            @Override
            public MethodVisitor visitMethod(final int access, final String name, final String descriptor,
                                             final String signature, final String[] exceptions) {
                final MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
                if ("<init>".equals(name)) {
                    return new MethodVisitor(Opcodes.ASM9, mv) {
                        @Override
                        public void visitMethodInsn(final int opcode, final String owner, final String name,
                                                    final String descriptor, final boolean isInterface) {
                            if (opcode == Opcodes.INVOKESPECIAL && "<init>".equals(name)) {
                                super.visitMethodInsn(opcode, newOwner, name, descriptor, isInterface);
                            } else {
                                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
                            }
                        }
                    };
                }
                return mv;
            }
        }, 0);
        return writer.toByteArray();
    }

    private static byte[] removeSuperCall(final byte[] classBytes) {
        final ClassReader reader = new ClassReader(classBytes);
        final ClassWriter writer = new ClassWriter(0);
        reader.accept(new ClassVisitor(Opcodes.ASM9, writer) {
            @Override
            public MethodVisitor visitMethod(final int access, final String name, final String descriptor,
                                             final String signature, final String[] exceptions) {
                final MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
                if ("<init>".equals(name)) {
                    return new MethodVisitor(Opcodes.ASM9, mv) {
                        @Override
                        public void visitMethodInsn(final int opcode, final String owner, final String name,
                                                    final String descriptor, final boolean isInterface) {
                            //noinspection StatementWithEmptyBody
                            if (opcode == Opcodes.INVOKESPECIAL && "<init>".equals(name)) {
                                // Skip: do not emit this instruction
                            } else {
                                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
                            }
                        }
                    };
                }
                return mv;
            }
        }, 0);
        return writer.toByteArray();
    }

    private static HypoContext buildPatchedContext(final String className, final byte[] patchedBytes) throws Exception {
        final ClassProviderRoot patchedRoot = new ClassProviderRoot() {
            @Override
            public byte @Nullable [] getClassData(final @NotNull String fileName) {
                if (fileName.equals(className + ".class")) {
                    return patchedBytes;
                }
                return null;
            }

            @Override
            public @NotNull List<? extends ClassProviderRoot.FileDataReference> getAllClasses() {
                return List.of(new ClassProviderRoot.FileDataReference() {
                    @Override public @NotNull String name() { return className + ".class"; }
                    @Override public byte @Nullable [] readData() { return patchedBytes; }
                });
            }

            @Override
            public void close() {}
        };

        return HypoContext.builder()
            .withProvider(AsmClassDataProvider.of(patchedRoot))
            .withConfig(HypoConfig.builder().setRequireFullClasspath(false).build())
            .build();
    }

    // -------------------------------------------------------------------------
    // Tests
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Hydration completes without exception and SUPER_CALL_TARGET is not set when the super() call owner is an unknown class")
    void testInvalidSuperCallOwnerDegradesSilently() throws Exception {
        final byte[] originalBytes = readClassFromScenarioJar("scenario17/NormalClass");
        final byte[] patchedBytes = patchSuperCallOwner(originalBytes, "invalid/UnknownClass");

        try (final HypoContext ctx = buildPatchedContext("scenario17/NormalClass", patchedBytes)) {
            final HydrationManager manager = HydrationManager.createDefault();
            manager.register(SuperConstructorHydrator.create());
            assertDoesNotThrow(() -> manager.hydrate(ctx),
                "Hydration must not throw when the INVOKESPECIAL owner is an unknown class");

            final ClassData normalClass = ctx.getContextProvider().findClass("scenario17/NormalClass");
            assertNotNull(normalClass, "NormalClass should be present in the context");
            final MethodData ctor = findMethod(normalClass, "<init>", "()V");
            assertNull(ctor.get(HypoHydration.SUPER_CALL_TARGET),
                "SUPER_CALL_TARGET should be null when the super() call owner cannot be resolved");
        }
    }

    @Test
    @DisplayName("Hydration completes without exception and SUPER_CALL_TARGET is not set when the constructor has no super() call")
    void testNoSuperCallConstructorDegradesSilently() throws Exception {
        final byte[] originalBytes = readClassFromScenarioJar("scenario17/NormalClass");
        final byte[] patchedBytes = removeSuperCall(originalBytes);

        try (final HypoContext ctx = buildPatchedContext("scenario17/NormalClass", patchedBytes)) {
            final HydrationManager manager = HydrationManager.createDefault();
            manager.register(SuperConstructorHydrator.create());
            assertDoesNotThrow(() -> manager.hydrate(ctx),
                "Hydration must not throw when the constructor has no INVOKESPECIAL <init> instruction");

            final ClassData normalClass = ctx.getContextProvider().findClass("scenario17/NormalClass");
            assertNotNull(normalClass, "NormalClass should be present in the context");
            final MethodData ctor = findMethod(normalClass, "<init>", "()V");
            assertNull(ctor.get(HypoHydration.SUPER_CALL_TARGET),
                "SUPER_CALL_TARGET should be null when no super() call is present in the constructor");
        }
    }

    @Test
    @DisplayName("Hydration of java.lang.Object completes without exception and SUPER_CALL_TARGET is not set")
    void testObjectSuperclassIsNull() throws Exception {
        @SuppressWarnings("resource") final ClassData objectClass = this.context().getContextProvider()
            .findClass("java/lang/Object");
        assertNotNull(objectClass, "java.lang.Object should be present in the context");

        final MethodData objectCtor = findMethod(objectClass, "<init>", "()V");
        assertNull(objectCtor.get(HypoHydration.SUPER_CALL_TARGET),
            "java.lang.Object.<init> must not have SUPER_CALL_TARGET set - it has no superclass");
    }
}
