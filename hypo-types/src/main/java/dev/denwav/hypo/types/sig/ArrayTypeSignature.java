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

package dev.denwav.hypo.types.sig;

import com.google.errorprone.annotations.Immutable;
import dev.denwav.hypo.types.intern.Intern;
import dev.denwav.hypo.types.TypeVariableBinder;
import dev.denwav.hypo.types.desc.ArrayTypeDescriptor;
import dev.denwav.hypo.types.kind.ArrayType;
import java.lang.reflect.GenericArrayType;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * An array type signature. Array type signatures consist of a {@link #getDimension() dimension} and a
 * {@link #getBaseType() base type}. As the base type is a {@link TypeSignature} it may contain generic type
 * information.
 */
@Immutable
public final class ArrayTypeSignature
    extends Intern<ArrayTypeSignature>
    implements ReferenceTypeSignature, ArrayType {

    private final int dimension;
    private final @NotNull TypeSignature baseType;

    /**
     * Create a new {@link ArrayTypeSignature}.
     * @param dimension The dimension for the array.
     * @param baseType The base type for the array.
     * @return A new {@link ArrayTypeSignature}.
     */
    public static @NotNull ArrayTypeSignature of(final int dimension, final @NotNull TypeSignature baseType) {
        return new ArrayTypeSignature(dimension, baseType).intern();
    }

    /**
     * Create an {@link ArrayTypeSignature} matching the given {@link GenericArrayType}.
     * @param arrayType The array type.
     * @return A new {@link ArrayTypeSignature} matching the given {@link GenericArrayType}.
     */
    public static @NotNull ArrayTypeSignature of(final @NotNull GenericArrayType arrayType) {
        GenericArrayType componentType = arrayType;
        int dim = 1;
        while (componentType.getGenericComponentType() instanceof final GenericArrayType nextArrayType) {
            componentType = nextArrayType;
            dim++;
        }

        return ArrayTypeSignature.of(dim, TypeSignature.of(componentType.getGenericComponentType()));
    }

    private ArrayTypeSignature(final int dimension, final @NotNull TypeSignature baseType) {
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
        this.asInternal(sb, false);
    }

    @Override
    public void asInternal(final @NotNull StringBuilder sb, final boolean withBindKey) {
        sb.ensureCapacity(sb.length() + this.dimension);
        //noinspection StringRepeatCanBeUsed
        for (int i = 0; i < this.dimension; i++) {
            sb.append("[");
        }
        this.baseType.asInternal(sb, withBindKey);
    }

    @Override
    public @NotNull ArrayTypeDescriptor asDescriptor() {
        return ArrayTypeDescriptor.of(this.dimension, this.baseType.asDescriptor());
    }

    @Override
    public @NotNull ArrayTypeSignature bind(final @NotNull TypeVariableBinder binder) {
        return ArrayTypeSignature.of(this.dimension, this.baseType.bind(binder));
    }

    @Override
    public boolean isUnbound() {
        return this.baseType.isUnbound();
    }

    @Override
    public int getDimension() {
        return this.dimension;
    }

    @Override
    public @NotNull TypeSignature getBaseType() {
        return this.baseType;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof final ArrayTypeSignature that)) {
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
