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

package dev.denwav.hypo.asm;

import dev.denwav.hypo.core.HypoException;
import dev.denwav.hypo.model.ClassDataProvider;
import dev.denwav.hypo.model.data.ClassData;
import dev.denwav.hypo.model.data.ClassKind;
import dev.denwav.hypo.model.data.FieldData;
import dev.denwav.hypo.model.data.LazyClassData;
import dev.denwav.hypo.model.data.MethodData;
import dev.denwav.hypo.model.data.Visibility;
import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InnerClassNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Implementation of {@link ClassData} based on {@code asm}'s {@link ClassNode}.
 */
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
            return (this.node.access & Opcodes.ACC_STATIC) != 0;
        }
        final String thisName = this.name();
        for (int i = 0; i < this.node.innerClasses.size(); i++) {
            final InnerClassNode innerClassNode = this.node.innerClasses.get(i);
            if (Objects.equals(thisName, innerClassNode.name) && innerClassNode.outerName != null) {
                return (innerClassNode.access & Opcodes.ACC_STATIC) != 0;
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
    public @NotNull ClassKind computeClassKind() {
        return this.kind();
    }
    @Override
    public @NotNull ClassKind kind() {
        if ((this.node.access & Opcodes.ACC_ANNOTATION) != 0) {
            return ClassKind.ANNOTATION;
        } else if ((this.node.access & Opcodes.ACC_INTERFACE) != 0) {
            return ClassKind.INTERFACE;
        } else if ((this.node.access & Opcodes.ACC_ABSTRACT) != 0) {
            return ClassKind.ABSTRACT_CLASS;
        } else if ((this.node.access & Opcodes.ACC_ENUM) != 0) {
            return ClassKind.ENUM;
        } else {
            return ClassKind.CLASS;
        }
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
    public @NotNull Set<ClassData> computeInterfaces() throws IOException {
        final ClassDataProvider prov = this.prov();
        final HashSet<ClassData> res = new HashSet<>();
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
    public @NotNull Set<FieldData> computeFields() {
        final Set<FieldData> res = new HashSet<>();
        for (final FieldNode field : this.node.fields) {
            res.add(new AsmFieldData(this, field));
        }
        return res;
    }

    @Override
    public @NotNull Set<MethodData> computeMethods() {
        final Set<MethodData> res = new HashSet<>();
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
