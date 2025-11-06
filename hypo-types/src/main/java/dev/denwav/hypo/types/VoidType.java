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

import dev.denwav.hypo.types.desc.TypeDescriptor;
import dev.denwav.hypo.types.sig.TypeSignature;
import org.jetbrains.annotations.NotNull;

/**
 * The {@code void} type in Java. This class is an implementation for both {@link TypeDescriptor} and
 * {@link TypeSignature}. {@code void} is only void when used as the return type in a method descriptor or signature, it
 * cannot be used as the type for any field or variable.
 *
 * <p>The wrapper type for {@code void} is {@link java.lang.Void java/lang/Void}. The internal name for {@code void} is
 * {@code V}.
 */
public enum VoidType implements TypeDescriptor, TypeSignature {
    /**
     * The singleton instance of this type object.
     */
    INSTANCE,
    ;

    @Override
    public void asReadable(final @NotNull StringBuilder sb) {
        sb.append("void");
    }

    @Override
    public void asInternal(final @NotNull StringBuilder sb) {
        this.asInternal(sb, false);
    }

    @Override
    public void asInternal(final @NotNull StringBuilder sb, final boolean withBindKey) {
        sb.append('V');
    }

    @Override
    public @NotNull VoidType asSignature() {
        return this;
    }

    @Override
    public @NotNull VoidType asDescriptor() {
        return this;
    }

    @Override
    public @NotNull VoidType bind(final @NotNull TypeVariableBinder binder) {
        return this;
    }

    @Override
    public boolean isUnbound() {
        return false;
    }

    @Override
    public String toString() {
        return this.asReadable();
    }
}
