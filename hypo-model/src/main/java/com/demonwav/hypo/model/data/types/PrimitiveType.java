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

package com.demonwav.hypo.model.data.types;

import com.google.errorprone.annotations.Immutable;
import org.jetbrains.annotations.NotNull;

/**
 * Primitive JVM types.
 */
@Immutable
public enum PrimitiveType implements JvmType {
    /**
     * {@link Character#TYPE}
     */
    CHAR("char", "C", "java/lang/Character"),
    /**
     * {@link Byte#TYPE}
     */
    BYTE("byte", "B", "java/lang/Byte"),
    /**
     * {@link Short#TYPE}
     */
    SHORT("short", "S", "java/lang/Short"),
    /**
     * {@link Integer#TYPE}
     */
    INT("int", "I", "java/lang/Integer"),
    /**
     * {@link Long#TYPE}
     */
    LONG("long", "J", "java/lang/Long"),
    /**
     * {@link Float#TYPE}
     */
    FLOAT("float", "F", "java/lang/Float"),
    /**
     * {@link Double#TYPE}
     */
    DOUBLE("double", "D", "java/lang/Double"),
    /**
     * {@link Boolean#TYPE}
     */
    BOOLEAN("boolean", "Z", "java/lang/Boolean"),
    /**
     * {@link Void#TYPE}
     */
    VOID("void", "V", "java/lang/Void");

    private final @NotNull String readableName;
    private final @NotNull String internalName;
    private final @NotNull ClassType wrapperType;

    PrimitiveType(
        final @NotNull String readableName,
        final @NotNull String internalName,
        final @NotNull String wrapperType
    ) {
        this.readableName = readableName;
        this.internalName = internalName;
        this.wrapperType = new ClassType(wrapperType);
    }

    /**
     * Returns the associated wrapper {@link ClassType class type} for this primitive type.
     * @return The associated wrapper {@link ClassType class type} for this primitive type.
     */
    public @NotNull ClassType toWrapperType() {
        return this.wrapperType;
    }

    @Override
    public void asReadableName(final @NotNull StringBuilder sb) {
        sb.append(this.readableName);
    }

    @Override
    public void asInternalName(final @NotNull StringBuilder sb) {
        sb.append(this.internalName);
    }

    /**
     * Get the primitive type associated with the given character. The character must be a valid internal JVM primitive
     * type character.
     *
     * @param c The character to match to a primitive type.
     * @return The primitive type associated with the given chatacter.
     * @throws IllegalStateException If the given character does not match an internal JVM primitive type character.
     * @see #asInternalName()
     */
    public static @NotNull PrimitiveType fromChar(final char c) {
        for (final PrimitiveType v : PrimitiveType.values()) {
            if (v.internalName.charAt(0) == c) {
                return v;
            }
        }
        throw new IllegalStateException("Unknown type: " + c);
    }

    @Override
    public String toString() {
        return this.asReadableName();
    }
}
