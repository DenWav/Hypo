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

import com.google.errorprone.annotations.Immutable;
import dev.denwav.hypo.types.desc.Descriptor;
import dev.denwav.hypo.types.desc.MethodDescriptor;
import dev.denwav.hypo.types.desc.TypeDescriptor;
import dev.denwav.hypo.types.intern.InternKey;
import dev.denwav.hypo.types.kind.ArrayType;
import dev.denwav.hypo.types.kind.ClassType;
import dev.denwav.hypo.types.kind.MethodType;
import dev.denwav.hypo.types.kind.ValueType;
import dev.denwav.hypo.types.sig.ArrayTypeSignature;
import dev.denwav.hypo.types.sig.ClassSignature;
import dev.denwav.hypo.types.sig.ClassTypeSignature;
import dev.denwav.hypo.types.sig.MethodSignature;
import dev.denwav.hypo.types.sig.ReferenceTypeSignature;
import dev.denwav.hypo.types.sig.Signature;
import dev.denwav.hypo.types.sig.ThrowsSignature;
import dev.denwav.hypo.types.sig.TypeSignature;
import dev.denwav.hypo.types.sig.param.BoundedTypeArgument;
import dev.denwav.hypo.types.sig.param.TypeArgument;
import dev.denwav.hypo.types.sig.param.TypeParameter;
import dev.denwav.hypo.types.sig.param.TypeVariable;
import dev.denwav.hypo.types.sig.param.WildcardBound;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A value which represents a type in Java. "types" include 5 major categories:
 * <ul>
 *     <li>{@link dev.denwav.hypo.types.desc.TypeDescriptor TypeDescriptor}</li>
 *     <li>{@link dev.denwav.hypo.types.desc.MethodDescriptor MethodDescriptor}</li>
 *     <li>{@link dev.denwav.hypo.types.sig.TypeSignature TypeSignature}</li>
 *     <li>{@link dev.denwav.hypo.types.sig.MethodSignature MethodSignature}</li>
 *     <li>{@link dev.denwav.hypo.types.sig.ClassSignature ClassSignature}</li>
 * </ul>
 *
 * <p>All types can be represented in two different ways, {@link #asReadable()}, and {@link #asInternal()}. Generally
 * "readable" will be in source code format, as would appear in a Java source file. Internal format matches exactly what
 * is present in compiled Java bytecode for the given type.
 *
 * <p>Implementations of this interface should have their {@link Object#toString() toString()} method defer to
 * {@link #asReadable()} to assist with debugging. {@link #asInternal()} should be used for serialization, as it matches
 * 1:1 with corresponding {@code parse()} methods for each type.
 */
@Immutable
public sealed interface TypeRepresentable extends InternKey
    permits ValueType, MethodType, ArrayType, ClassType, TypeBindable,
    Descriptor, TypeDescriptor, MethodDescriptor,
    Signature, TypeSignature, MethodSignature, ClassSignature, ReferenceTypeSignature,
    TypeParameter, TypeVariable, TypeVariable.Unbound, TypeArgument, BoundedTypeArgument, WildcardBound, ThrowsSignature {

    /**
     * Create a new {@link TypeRepresentable} using the best match implementation for the given {@link Type}. This
     * method will return {@link TypeSignature} or {@link TypeArgument} as these types contain the most information.
     * {@link TypeDescriptor type descriptors} cannot contain generic type information, so this method prefers types
     * that won't lose that information.
     *
     * @param type The {@link Type} to create a new {@link TypeRepresentable} from.
     * @return The new {@link TypeRepresentable}.
     */
    static @Nullable TypeRepresentable of(final Type type) {
        if (type instanceof final Class<?> clazz) {
            return TypeSignature.ofOrNull(clazz);
        } else if (type instanceof final ParameterizedType parameterizedType) {
            return ClassTypeSignature.of(parameterizedType);
        } else if (type instanceof final GenericArrayType arrayType) {
            return ArrayTypeSignature.of(arrayType);
        } else if (type instanceof final java.lang.reflect.TypeVariable<?> typeVar) {
            return TypeVariable.of(typeVar);
        } else if (type instanceof final WildcardType wildcardType) {
            return TypeArgument.of(wildcardType);
        } else {
            return null;
        }
    }

    /**
     * Print this type name as a human-readable name to the given {@link StringBuilder}.
     *
     * @param sb The {@link StringBuilder} to print this type's human-readable name to.
     */
    void asReadable(final @NotNull StringBuilder sb);

    /**
     * Print this type name as an internal JVM name to the given {@link StringBuilder}.
     *
     * @param sb The {@link StringBuilder} to print this type's internal JVM name to.
     */
    void asInternal(final @NotNull StringBuilder sb);

    @Override
    default @NotNull String internKey() {
        return this.asInternal();
    }

    /**
     * Returns this type's human-readable name.
     *
     * @return This type's human-readable name.
     */
    default @NotNull String asReadable() {
        final StringBuilder sb = new StringBuilder();
        this.asReadable(sb);
        return sb.toString();
    }

    /**
     * Returns this type's internal JVM name.
     *
     * @return This type's internal JVM name.
     */
    default @NotNull String asInternal() {
        final StringBuilder sb = new StringBuilder();
        this.asInternal(sb);
        return sb.toString();
    }
}
