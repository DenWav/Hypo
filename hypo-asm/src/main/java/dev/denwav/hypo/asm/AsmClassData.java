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

import dev.denwav.hypo.core.HypoException;
import dev.denwav.hypo.model.ClassDataProvider;
import dev.denwav.hypo.model.HypoModelUtil;
import dev.denwav.hypo.model.data.ClassData;
import dev.denwav.hypo.model.data.ClassKind;
import dev.denwav.hypo.model.data.FieldData;
import dev.denwav.hypo.model.data.LazyClassData;
import dev.denwav.hypo.model.data.MethodData;
import dev.denwav.hypo.model.data.MethodDescriptor;
import dev.denwav.hypo.model.data.Visibility;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InnerClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.RecordComponentNode;

import static dev.denwav.hypo.asm.HypoAsmUtil.toJvmType;
import static dev.denwav.hypo.model.data.MethodDescriptor.parseDescriptor;
import static org.objectweb.asm.Type.getType;

/**
 * Implementation of {@link ClassData} based on {@code asm}'s {@link ClassNode}.
 */
@SuppressWarnings("resource")
public class AsmClassData extends LazyClassData {

    private final @NotNull ClassNode node;

    /**
     * Construct a new instance of {@link AsmClassData} using the given {@link ClassNode}.
     *
     * @param node The {@link ClassNode} to use for this {@link AsmClassData}.
     */
    public AsmClassData(final @NotNull ClassNode node) {
        this.node = node;
    }

    /**
     * Returns the {@link ClassNode} which backs this {@link AsmClassData}.
     *
     * @return The {@link ClassNode} which backs this {@link AsmClassData}.
     */
    public @NotNull ClassNode getNode() {
        return this.node;
    }

    @Override
    public @NotNull String computeName() {
        return this.name();
    }
    @Override
    public @NotNull String name() {
        return this.node.name;
    }

    @Override
    public @Nullable ClassData computeOuterClass() throws IOException {
        // Simple case (anonymous classes & local classes)
        if (this.node.outerClass != null) {
            return this.prov().findClass(this.node.outerClass);
        }
        // Standard nested classes are more annoying
        final String thisName = this.name();
        for (int i = 0; i < this.node.innerClasses.size(); i++) {
            final InnerClassNode innerClassNode = this.node.innerClasses.get(i);
            if (Objects.equals(thisName, innerClassNode.name) && innerClassNode.outerName != null) {
                return this.prov().findClass(innerClassNode.outerName);
            }
        }
        // Not able to find outer class
        return null;
    }

    @Override
    public boolean computeStaticInnerClass() {
        if (this.node.outerClass != null) {
            if ((this.node.access & (Opcodes.ACC_STATIC | Opcodes.ACC_ENUM | Opcodes.ACC_RECORD)) != 0) {
                return true;
            }
            if (this.node.outerMethod != null) {
                final ClassData outerClass;
                try {
                    outerClass = this.outerClass();
                } catch (final IOException e) {
                    throw HypoModelUtil.rethrow(e);
                }
                if (outerClass != null) {
                    final MethodData outerMethod = outerClass.method(this.node.outerMethod, parseDescriptor(this.node.outerMethodDesc));
                    if (outerMethod != null) {
                        return outerMethod.isStatic();
                    }
                }
            }
            // All we know is it's a class inside a method, which is usually not static. We can't find the method though,
            // so we have to guess.
            return false;
        }
        final String thisName = this.name();
        for (int i = 0; i < this.node.innerClasses.size(); i++) {
            final InnerClassNode innerClassNode = this.node.innerClasses.get(i);
            if (Objects.equals(thisName, innerClassNode.name) && innerClassNode.outerName != null) {
                return (innerClassNode.access & (Opcodes.ACC_STATIC | Opcodes.ACC_ENUM | Opcodes.ACC_RECORD)) != 0;
            }
        }
        return false;
    }

    @Override
    public boolean computeIsFinal() {
        return this.isFinal();
    }
    @Override
    public boolean isFinal() {
        return (this.node.access & Opcodes.ACC_FINAL) != 0;
    }

    @Override
    public boolean computeIsSynthetic() {
        return this.isSynthetic();
    }
    @Override
    public boolean isSynthetic() {
        return (this.node.access & Opcodes.ACC_SYNTHETIC) != 0;
    }

    @Override
    public boolean computeIsSealed() {
        return this.isSealed();
    }
    @Override
    public boolean isSealed() {
        return this.node.permittedSubclasses != null;
    }

    @Override
    public @Nullable List<ClassData> computePermittedClasses() throws IOException {
        final List<String> permitted = this.node.permittedSubclasses;
        if (permitted == null) {
            return null;
        }
        final ArrayList<ClassData> result = new ArrayList<>(permitted.size());
        for (final String name : permitted) {
            result.add(this.prov().findClass(name));
        }
        return result;
    }

    @Override
    public @Nullable List<@NotNull FieldData> computeRecordComponents() {
        final List<RecordComponentNode> components = this.node.recordComponents;
        if (components == null) {
            return null;
        }

        final ArrayList<@NotNull FieldData> result = new ArrayList<>();
        for (final RecordComponentNode componentNode : components) {
            final FieldData field = this.field(componentNode.name, toJvmType(getType(componentNode.descriptor)));
            if (field != null) {
                result.add(field);
            }
        }

        return result;
    }

    @Override
    public @NotNull EnumSet<ClassKind> computeClassKinds() {
        final EnumSet<ClassKind> kinds = EnumSet.noneOf(ClassKind.class);
        if ((this.node.access & Opcodes.ACC_ANNOTATION) != 0) {
            kinds.add(ClassKind.ANNOTATION);
        }
        if ((this.node.access & Opcodes.ACC_INTERFACE) != 0) {
            kinds.add(ClassKind.INTERFACE);
        }
        if ((this.node.access & Opcodes.ACC_ABSTRACT) != 0) {
            kinds.add(ClassKind.ABSTRACT_CLASS);
        }
        if ((this.node.access & Opcodes.ACC_ENUM) != 0) {
            kinds.add(ClassKind.ENUM);
        }
        if ((this.node.access & Opcodes.ACC_RECORD) != 0) {
            kinds.add(ClassKind.RECORD);
        }
        if (kinds.isEmpty()) {
            kinds.add(ClassKind.CLASS);
        }
        return kinds;
    }

    @Override
    public @NotNull EnumSet<ClassKind> kinds() {
        return EnumSet.copyOf(super.kinds());
    }

    @Override
    public @NotNull Visibility computeVisibility() {
        return this.visibility();
    }
    @Override
    public @NotNull Visibility visibility() {
        return HypoAsmUtil.accessToVisibility(this.node.access);
    }

    @Override
    public @Nullable ClassData computeSuperClass() throws IOException {
        final String superName = this.node.superName;
        if (superName == null) {
            return null;
        }
        final ClassData superClassData = this.prov().findClass(superName);
        if (superClassData == null && this.isRequireFullClasspath()) {
            throw new HypoException("Unable to resolve class data binding for '" + superName +
                "' which is listed as the super class for '" + this.name() + "'");
        }
        return superClassData;
    }

    @Override
    public @NotNull List<ClassData> computeInterfaces() throws IOException {
        final ClassDataProvider prov = this.prov();
        final ArrayList<ClassData> res = new ArrayList<>();
        for (final String inter : this.node.interfaces) {
            final ClassData interfaceData = prov.findClass(inter);
            if (interfaceData != null) {
                res.add(interfaceData);
                continue;
            }
            if (this.isRequireFullClasspath()) {
                throw new HypoException("Unable to resolve class data binding for '" + inter +
                    "' which is listed as an interface for '" + this.name() + "'");
            }
        }
        return res;
    }

    @Override
    public @NotNull List<FieldData> computeFields() {
        final ArrayList<FieldData> res = new ArrayList<>();
        for (final FieldNode field : this.node.fields) {
            res.add(new AsmFieldData(this, field));
        }
        return res;
    }

    @Override
    public @NotNull List<MethodData> computeMethods() {
        final ArrayList<MethodData> res = new ArrayList<>();
        for (final MethodNode method : this.node.methods) {
            if (method.name.equals("<init>")) {
                res.add(new AsmConstructorData(this, method));
            } else {
                res.add(new AsmMethodData(this, method));
            }
        }
        return res;
    }

    /**
     * Create a new {@link ClassData} by parsing the given binary data of a Java class file.
     *
     * @param classData The Java class file data to parse into a {@link AsmClassData} object.
     * @return The new {@link AsmClassData} object representing the given Java class.
     */
    @Contract("_ -> new")
    public static @NotNull AsmClassData readFile(final byte @NotNull [] classData) {
        final ClassNode node = new ClassNode(Opcodes.ASM9);
        new ClassReader(classData).accept(node, 0);

        return new AsmClassData(node);
    }
}
