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

import dev.denwav.hypo.model.data.Visibility;
import dev.denwav.hypo.types.desc.MethodDescriptor;
import dev.denwav.hypo.types.desc.TypeDescriptor;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * Utility class for {@code asm} and Hypo compatibility.
 */
public final class HypoAsmUtil {

    private HypoAsmUtil() {}

    /**
     * For convenience, an empty {@code int[]} array.
     */
    public static final int[] EMPTY_INT_ARRAY = new int[0];

    /**
     * Returns the {@link Visibility visibility level} from the given access modifiers.
     *
     * @param access The access modifiers to determine visibility from.
     * @return The {@link Visibility visibility level} from the given access modifiers.
     */
    public static @NotNull Visibility accessToVisibility(final int access) {
        if ((access & Opcodes.ACC_PUBLIC) != 0) {
            return Visibility.PUBLIC;
        } else if ((access & Opcodes.ACC_PROTECTED) != 0) {
            return Visibility.PROTECTED;
        } else if ((access & Opcodes.ACC_PRIVATE) != 0) {
            return Visibility.PRIVATE;
        } else {
            return Visibility.PACKAGE;
        }
    }

    /**
     * Map an {@code asm} {@link Type} object into a Hypo {@link TypeDescriptor}.
     *
     * @param type The {@code asm} {@link Type} to convert to a Hypo {@link TypeDescriptor}.
     * @return A {@link TypeDescriptor} which matches the given {@link Type}.
     */
    @SuppressWarnings("ReferenceEquality")
    public static @NotNull TypeDescriptor toJvmType(final @NotNull Type type) {
        if (type.getSort() == Type.METHOD) {
            throw new IllegalArgumentException("Given type is a method descriptor: " + type);
        }

        return TypeDescriptor.parse(type.getDescriptor());
    }

    /**
     * Map an asm {@link Type} into a Hypo {@link MethodDescriptor}. The {@link Type} must represnet a method descriptor.
     * @param type The asm {@link Type} to convert into a Hypo {@link MethodDescriptor}.
     * @return A {@link MethodDescriptor} which matches the given {@link Type}
     */
    public static @NotNull MethodDescriptor toDescriptor(final @NotNull Type type) {
        if (type.getSort() != Type.METHOD) {
            throw new IllegalArgumentException("Given type is not a method descriptor: " + type);
        }

        return MethodDescriptor.parse(type.getDescriptor());
    }
}
