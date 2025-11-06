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

package dev.denwav.hypo.types.desc;

import com.google.errorprone.annotations.Immutable;
import dev.denwav.hypo.types.intern.Intern;
import dev.denwav.hypo.types.kind.ArrayType;
import dev.denwav.hypo.types.sig.ArrayTypeSignature;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link TypeDescriptor} representing an array type. Array types consist of a {@link #getDimension() dimension} and a
 * {@link #getBaseType() base type}, which can be either a {@link dev.denwav.hypo.types.PrimitiveType PrimitiveType} or
 * a {@link ClassTypeDescriptor}.
 *
 * <p>Array type descriptors have the internal format of {@code [<base_type>}. The leading {@code [} is repeated to
 * denote the dimensionality of the array, so a 3-dimension array type would start with {@code [[[}.
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-ArrayType">ArrayType</a>
 */
@Immutable
public final class ArrayTypeDescriptor extends Intern<ArrayTypeDescriptor> implements TypeDescriptor, ArrayType {

    private final int dimension;
    private final @NotNull TypeDescriptor baseType;

    /**
     * Create a {@link ArrayTypeDescriptor} instance.
     *
     * @param dimension The dimension for the new type.
     * @param baseType The base type for the new type.
     * @return The new {@link ArrayTypeDescriptor}.
     */
    public static @NotNull ArrayTypeDescriptor of(final int dimension, final @NotNull TypeDescriptor baseType) {
        return new ArrayTypeDescriptor(dimension, baseType).intern();
    }

    private ArrayTypeDescriptor(
        final int dimension,
        final @NotNull TypeDescriptor baseType
    ) {
        this.dimension = dimension;
        this.baseType = baseType;
    }

    @Override
    public void asReadable(final @NotNull StringBuilder sb) {
        this.baseType.asReadable(sb);
        sb.ensureCapacity(sb.length() + 2 * this.dimension);
        //noinspection StringRepeatCanBeUsed
        for (int i = 0; i < this.dimension; i++) {
            sb.append("[]");
        }
    }

    @Override
    public void asInternal(final @NotNull StringBuilder sb) {
        sb.ensureCapacity(sb.length() + this.dimension);
        //noinspection StringRepeatCanBeUsed
        for (int i = 0; i < this.dimension; i++) {
            sb.append('[');
        }
        this.baseType.asInternal(sb);
    }

    @Override
    public @NotNull ArrayTypeSignature asSignature() {
        return ArrayTypeSignature.of(this.dimension, this.baseType.asSignature());
    }

    /**
     * Get the dimension of this array type.
     * @return The dimension of this array type.
     */
    @Override
    public int getDimension() {
        return this.dimension;
    }

    /**
     * Get the base type of this array type.
     * @return The base type of this array type.
     */
    @Override
    public @NotNull TypeDescriptor getBaseType() {
        return this.baseType;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof final ArrayTypeDescriptor that)) {
            return false;
        }
        return this.dimension == that.dimension
            && Objects.equals(this.baseType, that.baseType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.dimension, this.baseType);
    }

    @Override
    public String toString() {
        return this.asReadable();
    }
}
