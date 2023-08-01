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

package dev.denwav.hypo.model.data;

import com.google.errorprone.annotations.Immutable;
import dev.denwav.hypo.model.HypoModelUtil;
import dev.denwav.hypo.model.data.types.ArrayType;
import dev.denwav.hypo.model.data.types.ClassType;
import dev.denwav.hypo.model.data.types.JvmType;
import dev.denwav.hypo.model.data.types.PrimitiveType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The type descriptor for a method. This refers to a method's basic descriptor, not its generic type signature.
 *
 * <p>A method's descriptor consists of 2 components:
 *
 * <ol>
 *     <li>The method parameter list</li>
 *     <li>The method return type</li>
 * </ol>
 */
@Immutable
public final class MethodDescriptor {

    @SuppressWarnings("Immutable") // errorprone doesn't know that this is immutable
    private final @NotNull List<@NotNull JvmType> params;
    private final @NotNull JvmType returnType;

    /**
     * Create a new descriptor for the given method parameter types and return type.
     *
     * @param params The method parameter types.
     * @param returnType The method return type.
     */
    public MethodDescriptor(final @NotNull List<@NotNull JvmType> params, final @NotNull JvmType returnType) {
        this.params = HypoModelUtil.asImmutableList(params);
        this.returnType = returnType;
    }

    /**
     * Parses the given method descriptor string into a new {@link MethodDescriptor}. The descriptor string must be a
     * valid descriptor in the internal JVM format. Invalid descriptor strings will result in a
     * {@link IllegalArgumentException}.
     *
     * @param desc The method descriptor string to parse.
     * @return The new {@link MethodDescriptor}.
     * @throws IllegalArgumentException If the given descriptor is not valid.
     */
    public static @NotNull MethodDescriptor parseDescriptor(final @NotNull String desc) {
        if (!desc.startsWith("(")) {
            throw new IllegalArgumentException("desc is invalid: Does not start with '(': " + desc);
        }

        final ArrayList<JvmType> params = new ArrayList<>();
        JvmType returnType = null;

        final JvmType[] ref = new JvmType[1];

        final int len = desc.length();
        // starting at 1, skipping first '('
        for (int i = 1; i < len; i++) {
            i = parseType(ref, desc, i);
            final JvmType t = ref[0];
            if (t == null) {
                parseType(ref, desc, i + 1);
                returnType = ref[0];
                break;
            }
            params.add(t);
        }

        if (returnType == null) {
            throw new IllegalArgumentException("desc is invalid: Does not have a return type: " + desc);
        }

        return new MethodDescriptor(params, returnType);
    }

    private static int parseType(final @Nullable JvmType @NotNull [] ref, final @NotNull String desc, final int index) {
        /*
         * The `ref` parameter is our ugly way of doing an `out` parameter, since we need to return the type parsed as
         * well as the new index. So we return `int` for the new index and simply set the type result into `ref`. `ref`
         * is an array of length 1 - there aren't any validation checks here as this is a private method.
         *
         * This method also returns the index _before_ the next index. So each of the single-character types simply
         * return `index` instead of `index + 1`. This is intentional for 2 reasons:
         *
         *  1. The calling method, `parseString`, is doing range checks on the `desc` string. If the character just
         *     parsed is the final character in the string we don't want to overrun that on accident.
         *  2. The calling method is doing a standard `for` loop, so the index variable will be incremented
         *     automatically anyways if it satisfies the bounds-check. If we increment here then that will result in a
         *     double-increment.
         */

        final char c = desc.charAt(index);
        switch (c) {
            case 'B':
                ref[0] = PrimitiveType.BYTE;
                return index;
            case 'S':
                ref[0] = PrimitiveType.SHORT;
                return index;
            case 'I':
                ref[0] = PrimitiveType.INT;
                return index;
            case 'J':
                ref[0] = PrimitiveType.LONG;
                return index;
            case 'F':
                ref[0] = PrimitiveType.FLOAT;
                return index;
            case 'D':
                ref[0] = PrimitiveType.DOUBLE;
                return index;
            case 'C':
                ref[0] = PrimitiveType.CHAR;
                return index;
            case 'Z':
                ref[0] = PrimitiveType.BOOLEAN;
                return index;
            case 'V':
                ref[0] = PrimitiveType.VOID;
                return index;
            case 'L':
                final int end = desc.indexOf(';', index);
                if (end == -1) {
                    throw new IllegalArgumentException("desc is invalid: Class type at index " + index +
                        " is not terminated: " + desc);
                }
                ref[0] = new ClassType(desc.substring(index, end + 1));
                return end;
            case '[':
                final int len = desc.length();
                int dim = 1;
                int i = index;
                i++;
                for (; i < len; i++) {
                    if (desc.charAt(i) == '[') {
                        dim++;
                    } else {
                        break;
                    }
                }

                final int newIndex = parseType(ref, desc, i);
                final JvmType parsed = ref[0];
                if (parsed == null) {
                    throw new IllegalArgumentException("desc is invalid: Array type at index " + index +
                        " is not terminated: " + desc);
                }
                ref[0] = new ArrayType(parsed, dim);
                return newIndex;
            case ')':
                ref[0] = null;
                return index;
            default:
                throw new IllegalArgumentException("desc is invalid: Unknown type char at index " + index + " '" + c +
                    "': " + desc);
        }
    }

    /**
     * Returns the method parameter component of the descriptor.
     * @return The method parameter component of the descriptor.
     */
    public @NotNull List<@NotNull JvmType> getParams() {
        return this.params;
    }

    /**
     * Returns the method return type of the descriptor.
     * @return The method return type of the descriptor.
     */
    public @NotNull JvmType getReturnType() {
        return this.returnType;
    }

    /**
     * Returns the string representation of this descriptor in the internal JVM format.
     *
     * <p>Use {@link #toString()} for a more human-readable string representation.
     *
     * @return The string representation of this descriptor in the internal JVM format.
     */
    @Contract(pure = true)
    public @NotNull String toInternalString() {
        final StringBuilder sb = new StringBuilder();
        sb.append('(');
        for (final JvmType param : this.params) {
            param.asInternalName(sb);
        }
        sb.append(')');
        this.returnType.asInternalName(sb);
        return sb.toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        final MethodDescriptor that = (MethodDescriptor) o;
        return this.params.equals(that.params) && this.returnType.equals(that.returnType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.params, this.returnType);
    }

    /**
     * Returns the string representation of this descriptor in a human-readable format.
     *
     * <p>Use {@link #toInternalString()} for a string representation in the internal JVM format.
     *
     * @return The string representation of this descriptor in a human-readable format.
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append('(');
        final Iterator<@NotNull JvmType> iter = this.params.iterator();
        while (iter.hasNext()) {
            iter.next().asReadableName(sb);
            if (iter.hasNext()) {
                sb.append(", ");
            }
        }
        sb.append(") ");
        this.returnType.asReadableName(sb);
        return sb.toString();
    }
}
