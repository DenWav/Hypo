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
import com.demonwav.hypo.model.data.ConstructorData;
import com.demonwav.hypo.model.data.LazyConstructorData;
import com.demonwav.hypo.model.data.MethodDescriptor;
import com.demonwav.hypo.model.data.Visibility;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

/**
 * Implementation of {@link ConstructorData} based on {@code asm}'s {@link MethodNode}.
 *
 * @see AsmMethodData
 */
public class AsmConstructorData extends LazyConstructorData implements ConstructorData {

    private final @NotNull ClassData parentClass;
    private final @NotNull MethodNode node;

    /**
     * Construct a new instance of {@link AsmConstructorData} using the given {@link MethodNode}. The given {@code node}
     * is understood to be a constructor, passing a {@link MethodNode} which isn't a constructor is undefined. Regular
     * methods should use {@link AsmMethodData}.
     *
     * @param parentClass The {@link ClassData} which declares this constructor.
     * @param node The {@link MethodNode} to use for this {@link AsmConstructorData}.
     */
    public AsmConstructorData(final @NotNull ClassData parentClass, final @NotNull MethodNode node) {
        this.parentClass = parentClass;
        this.node = node;
    }

    /**
     * Returns the {@link MethodNode} which backs this {@link AsmConstructorData}.
     * @return The {@link MethodNode} which backs this {@link AsmConstructorData}.
     */
    public @NotNull MethodNode getNode() {
        return this.node;
    }

    @Override
    public @NotNull Visibility visibility() {
        return HypoAsmUtil.accessToVisibility(this.node.access);
    }

    @Override
    public boolean isSynthetic() {
        return (this.node.access & Opcodes.ACC_SYNTHETIC) != 0;
    }

    @Override
    public @NotNull ClassData parentClass() {
        return this.parentClass;
    }

    @Override
    public @NotNull MethodDescriptor computeDescriptor() {
        return MethodDescriptor.parseDescriptor(this.node.desc);
    }
}
