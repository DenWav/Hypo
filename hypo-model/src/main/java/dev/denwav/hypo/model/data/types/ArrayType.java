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

package dev.denwav.hypo.model.data.types;

import com.google.errorprone.annotations.Immutable;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * An array type. Array types in Java have 2 components:
 * <ol>
 *     <li>
 *         The base type, which is one of either:
 *         <ul>
 *             <li>{@link PrimitiveType Primitive type}</li>
 *             <li>{@link ClassType Class type}</li>
 *         </ul>
 *     </li>
 *     <li>The dimension of the array</li>
 * </ol>
 */
@Immutable
public final class ArrayType implements JvmType {

    private final @NotNull JvmType baseType;

    private final int dimension;

    /**
     * Create a new array type with the given base type and dimension.
     *
     * @param baseType The base type of this array type.
     * @param dimension The dimension of this array type.
     * @throws IllegalArgumentException If the given {@code dimension <= 0} or the given {@code baseType} is an
     *                                  {@link ArrayType}
     */
    public ArrayType(final @NotNull JvmType baseType, final int dimension) {
        if (dimension <= 0) {
            throw new IllegalArgumentException("Invalid array dimension: " + dimension);
        }
        if (baseType instanceof ArrayType) {
            throw new IllegalArgumentException("Invalid array base type: " + baseType.getClass().getName());
        }

        this.baseType = baseType;
        this.dimension = dimension;
    }

    /**
     * Get the base type of this array type.
     *
     * @return The base type of this array type.
     */
    public @NotNull JvmType baseType() {
        return this.baseType;
    }

    /**
     * Get the dimension of this array type.
     *
     * @return The dimension of this array type.
     */
    public int dimension() {
        return this.dimension;
    }

    private void internalPrefix(final @NotNull StringBuilder sb) {
        for (int i = 0; i < this.dimension; i++) {
            sb.append('[');
        }
    }

    private void readableSuffix(final @NotNull StringBuilder sb) {
        for (int i = 0; i < this.dimension; i++) {
            sb.append("[]");
        }
    }

    @Override
    public void asReadableName(final @NotNull StringBuilder sb) {
        this.baseType.asReadableName(sb);
        this.readableSuffix(sb);
    }

    @Override
    public void asInternalName(final @NotNull StringBuilder sb) {
        this.internalPrefix(sb);
        this.baseType.asInternalName(sb);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof ArrayType)) return false;
        final ArrayType arrayType = (ArrayType) o;
        return this.dimension == arrayType.dimension && this.baseType.equals(arrayType.baseType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.baseType, this.dimension);
    }

    @Override
    public String toString() {
        return this.asReadableName();
    }
}
