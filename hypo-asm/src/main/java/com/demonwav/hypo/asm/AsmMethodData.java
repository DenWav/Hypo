/*
 * Hypo, an extensible and pluggable Java bytecode analytical model.
 *
 * Copyright (C) 2021  Kyle Wood (DemonWav)
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

package com.demonwav.hypo.asm;

import com.demonwav.hypo.model.data.ClassData;
import com.demonwav.hypo.model.data.LazyMethodData;
import com.demonwav.hypo.model.data.MethodData;
import com.demonwav.hypo.model.data.MethodDescriptor;
import com.demonwav.hypo.model.data.Visibility;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

/**
 * Implementation of {@link MethodData} based on {@code asm}'s {@link MethodNode}.
 *
 * @see AsmConstructorData
 */
public class AsmMethodData extends LazyMethodData implements MethodData {

    private final @NotNull AsmClassData parentClass;
    private final @NotNull MethodNode node;

    /**
     * Construct a new instance of {@link AsmMethodData} using the given {@link MethodNode}. The given {@code node}
     * is understood not to be a constructor, passing a {@link MethodNode} which is a constructor is undefined.
     * Constructors should use {@link AsmConstructorData}.
     *
     * @param parentClass The {@link ClassData} which declares this method.
     * @param node The {@link MethodNode} to use for this {@link AsmMethodData}.
     */
    public AsmMethodData(final @NotNull AsmClassData parentClass, final @NotNull MethodNode node) {
        this.parentClass = parentClass;
        this.node = node;
    }

    /**
     * Returns the {@link MethodNode} which backs this {@link AsmMethodData}.
     * @return The {@link MethodNode} which backs this {@link AsmMethodData}.
     */
    public @NotNull MethodNode getNode() {
        return this.node;
    }

    @Override
    public @NotNull Visibility visibility() {
        return HypoAsmUtil.accessToVisibility(this.node.access);
    }

    @Override
    public boolean isAbstract() {
        return (this.node.access & Opcodes.ACC_ABSTRACT) != 0;
    }

    @Override
    public boolean isFinal() {
        return (this.node.access & Opcodes.ACC_FINAL) != 0;
    }

    @Override
    public boolean isSynthetic() {
        return (this.node.access & Opcodes.ACC_SYNTHETIC) != 0;
    }

    @Override
    public boolean isBridge() {
        return (this.node.access & Opcodes.ACC_BRIDGE) != 0;
    }

    @Override
    public boolean isNative() {
        return (this.node.access & Opcodes.ACC_NATIVE) != 0;
    }

    @Override
    public boolean isStatic() {
        return (this.node.access & Opcodes.ACC_STATIC) != 0;
    }

    @Override
    public @NotNull String name() {
        return this.node.name;
    }

    @Override
    public @NotNull AsmClassData parentClass() {
        return this.parentClass;
    }

    @Override
    public @NotNull MethodDescriptor computeDescriptor() {
        return MethodDescriptor.parseDescriptor(this.node.desc);
    }

    @Override
    public @NotNull String descriptorText() {
        return this.node.desc;
    }
}
