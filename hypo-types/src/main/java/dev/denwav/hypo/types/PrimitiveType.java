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

import dev.denwav.hypo.types.desc.ClassTypeDescriptor;
import dev.denwav.hypo.types.desc.TypeDescriptor;
import dev.denwav.hypo.types.sig.ClassTypeSignature;
import dev.denwav.hypo.types.sig.TypeSignature;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Primitive types in Java, implementation for both {@link TypeDescriptor} and
 * {@link TypeSignature}.
 *
 * <p>There are 8 total primitive types in the JVM:
 * <table>
 *     <caption>
 *         Java Primitive Types
 *     </caption>
 *     <tr>
 *         <th>Name</th>
 *         <th>Wrapper Type</th>
 *         <th>Internal Name</th>
 *     </tr>
 *     <tr>
 *         <td>{@code char}</td>
 *         <td>{@link java.lang.Character java/lang/Character}</td>
 *         <td>{@code C}</td>
 *     </tr>
 *     <tr>
 *         <td>{@code byte}</td>
 *         <td>{@link java.lang.Byte java/lang/Byte}</td>
 *         <td>{@code B}</td>
 *     </tr>
 *     <tr>
 *         <td>{@code short}</td>
 *         <td>{@link java.lang.Short java/lang/Short}</td>
 *         <td>{@code S}</td>
 *     </tr>
 *     <tr>
 *         <td>{@code int}</td>
 *         <td>{@link java.lang.Integer java/lang/Integer}</td>
 *         <td>{@code I}</td>
 *     </tr>
 *     <tr>
 *         <td>{@code long}</td>
 *         <td>{@link java.lang.Long java/lang/Long}</td>
 *         <td>{@code J}</td>
 *     </tr>
 *     <tr>
 *         <td>{@code float}</td>
 *         <td>{@link java.lang.Float java/lang/Float}</td>
 *         <td>{@code F}</td>
 *     </tr>
 *     <tr>
 *         <td>{@code double}</td>
 *         <td>{@link java.lang.Double java/lang/Double}</td>
 *         <td>{@code D}</td>
 *     </tr>
 *     <tr>
 *         <td>{@code boolean}</td>
 *         <td>{@link java.lang.Boolean java/lang/Boolean}</td>
 *         <td>{@code Z}</td>
 *     </tr>
 * </table>
 */
public enum PrimitiveType implements TypeDescriptor, TypeSignature {
    /**
     * {@code char}
     */
    CHAR("char", 'C', "java/lang/Character"),
    /**
     * {@code byte}
     */
    BYTE("byte", 'B', "java/lang/Byte"),
    /**
     * {@code short}
     */
    SHORT("short", 'S', "java/lang/Short"),
    /**
     * {@code int}
     */
    INT("int", 'I', "java/lang/Integer"),
    /**
     * {@code long}
     */
    LONG("long", 'J', "java/lang/Long"),
    /**
     * {@code float}
     */
    FLOAT("float", 'F', "java/lang/Float"),
    /**
     * {@code double}
     */
    DOUBLE("double", 'D', "java/lang/Double"),
    /**
     * {@code boolean}
     */
    BOOLEAN("boolean", 'Z', "java/lang/Boolean"),
    ;

    private final @NotNull String readableName;
    private final char internalName;
    private final @NotNull String wrapperType;

    PrimitiveType(
        final @NotNull String readableName,
        final char internalName,
        final @NotNull String wrapperType
    ) {
        this.readableName = readableName;
        this.internalName = internalName;
        this.wrapperType = wrapperType;
    }

    @Override
    public void asReadable(final @NotNull StringBuilder sb) {
        sb.append(this.readableName);
    }

    @Override
    public @NotNull String asReadable() {
        return this.readableName;
    }

    @Override
    public void asInternal(final @NotNull StringBuilder sb) {
        this.asInternal(sb, false);
    }

    @Override
    public @NotNull String asInternal() {
        return String.valueOf(this.internalName);
    }

    @Override
    public void asInternal(final @NotNull StringBuilder sb, final boolean withBindKey) {
        sb.append(this.internalName);
    }

    @Override
    public @NotNull PrimitiveType asSignature() {
        return this;
    }

    @Override
    public @NotNull PrimitiveType bind(final @NotNull TypeVariableBinder binder) {
        return this;
    }

    @Override
    public boolean isUnbound() {
        return false;
    }

    @Override
    public @NotNull PrimitiveType asDescriptor() {
        return this;
    }

    /**
     * Returns the wrapper type name for this primitive type.
     * @return The wrapper type name for this primitive type.
     */
    public @NotNull String getWrapperType() {
        return this.wrapperType;
    }

    /**
     * Returns the primitive type for the given internal name character if the
     * character matches a primitive type. Returns {@code null} if the given
     * character does not match a primitive type.
     *
     * @param c The character to match to a primitive type name.
     * @return The {@link PrimitiveType} for the given internal name character,
     *         or {@code null}.
     */
    public static @Nullable PrimitiveType fromChar(final char c) {
        return switch (c) {
            case 'C' -> PrimitiveType.CHAR;
            case 'B' -> PrimitiveType.BYTE;
            case 'S' -> PrimitiveType.SHORT;
            case 'I' -> PrimitiveType.INT;
            case 'J' -> PrimitiveType.LONG;
            case 'F' -> PrimitiveType.FLOAT;
            case 'D' -> PrimitiveType.DOUBLE;
            case 'Z' -> PrimitiveType.BOOLEAN;
            default -> null;
        };
    }

    /**
     * Convenience alternative to {@link #fromChar(char)} that accepts a
     * single-character {@link String} instead. If the given string is
     * {@code null} or contains more than a single character, {@code null} is
     * returned.
     *
     * @param c The character to match to a primitive type name.
     * @return The {@link PrimitiveType} for the given internal name character,
     *         or {@code null}.
     */
    public static @Nullable PrimitiveType fromChar(final @Nullable String c) {
        if (c == null) {
            return null;
        }
        if (c.length() != 1) {
            return null;
        }
        return PrimitiveType.fromChar(c.charAt(0));
    }

    /**
     * Returns the {@link ClassTypeDescriptor} for the wrapper type for this
     * primitive type.
     *
     * @return The {@link ClassTypeDescriptor} for the wrapper type for this
     *         primitive type.
     */
    public @NotNull ClassTypeDescriptor getWrapperTypeDescriptor() {
        return ClassTypeDescriptor.of(this.getWrapperType());
    }

    /**
     * Returns the {@link ClassTypeSignature} for the wrapper type for this
     * primitive type.
     *
     * @return The {@link ClassTypeSignature} for the wrapper type for this
     *         primitive type.
     */

    public @NotNull ClassTypeSignature getWrapperTypeSignature() {
        return ClassTypeSignature.of(this.getWrapperType());
    }

    @Override
    public String toString() {
        return this.asReadable();
    }
}
