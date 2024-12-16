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

import dev.denwav.hypo.model.data.ClassData;
import dev.denwav.hypo.model.data.FieldData;
import dev.denwav.hypo.model.data.LazyFieldData;
import dev.denwav.hypo.model.data.Visibility;
import dev.denwav.hypo.types.desc.TypeDescriptor;
import dev.denwav.hypo.types.sig.TypeSignature;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FieldNode;

/**
 * Implementation of {@link FieldData} based on {@code asm}'s {@link FieldNode}.
 */
public class AsmFieldData extends LazyFieldData implements FieldData {

    private final @NotNull ClassData parentClass;
    private final @NotNull FieldNode node;

    /**
     * Construct a new instance of {@link AsmFieldData} using the given {@link FieldNode}.
     *
     * @param parentClass The {@link ClassData} which declares this field.
     * @param node The {@link FieldNode} to use for this {@link AsmFieldData}.
     */
    public AsmFieldData(final @NotNull ClassData parentClass, final @NotNull FieldNode node) {
        this.parentClass = parentClass;
        this.node = node;
    }

    /**
     * Returns the {@link FieldNode} which backs this {@link AsmFieldData}.
     * @return The {@link FieldNode} which backs this {@link AsmFieldData}.
     */
    public @NotNull FieldNode getNode() {
        return this.node;
    }

    @Override
    public @NotNull Visibility visibility() {
        return HypoAsmUtil.accessToVisibility(this.node.access);
    }

    @Override
    public boolean isStatic() {
        return (this.node.access & Opcodes.ACC_STATIC) != 0;
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
    public @NotNull String name() {
        return this.node.name;
    }

    @Override
    public @NotNull ClassData parentClass() {
        return this.parentClass;
    }

    @Override
    public @NotNull TypeDescriptor computeDescriptor() {
        return TypeDescriptor.parse(this.node.desc);
    }

    @Override
    public @Nullable TypeSignature computeSignature() {
        final String sig = this.node.signature;
        if (sig != null) {
            return TypeSignature.parse(sig);
        } else {
            return null;
        }
    }
}
