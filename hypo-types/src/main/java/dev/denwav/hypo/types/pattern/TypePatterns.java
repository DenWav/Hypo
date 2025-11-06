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

package dev.denwav.hypo.types.pattern;

import dev.denwav.hypo.types.PrimitiveType;
import dev.denwav.hypo.types.VoidType;
import dev.denwav.hypo.types.desc.ClassTypeDescriptor;
import dev.denwav.hypo.types.desc.TypeDescriptor;
import dev.denwav.hypo.types.kind.ArrayType;
import dev.denwav.hypo.types.kind.ClassType;
import dev.denwav.hypo.types.kind.ValueType;
import dev.denwav.hypo.types.sig.ClassTypeSignature;
import dev.denwav.hypo.types.sig.ReferenceTypeSignature;
import dev.denwav.hypo.types.sig.TypeSignature;
import dev.denwav.hypo.types.sig.param.TypeArgument;
import java.util.List;
import java.util.function.Predicate;
import org.jetbrains.annotations.NotNull;

/**
 * Factory for {@link TypePattern} for {@link TypeDescriptor} and {@link TypeSignature} type objects.
 */
public final class TypePatterns {

    private TypePatterns() {}

    /*
     * TypeDescriptor & TypeSignature
     */

    /**
     * A type pattern where the type object is a {@link ValueType}.
     * @return The type pattern.
     */
    public static @NotNull TypePattern isType() {
        return (ctx, t) -> t instanceof ValueType;
    }

    /**
     * A type pattern where the type object is a {@link ValueType} that matches the given predicate.
     *
     * @param predicate The predicate to test against the {@link ValueType}.
     * @return The type pattern.
     */
    public static @NotNull TypePattern isType(final Predicate<? super ValueType> predicate) {
        return (ctx, t) -> t instanceof final ValueType v && predicate.test(v);
    }

    /**
     * A type pattern where the type object is a {@link ClassType}.
     * @return The type pattern.
     */
    public static @NotNull TypePattern isClass() {
        return (ctx, t) -> t instanceof ClassType;
    }

    /**
     * A type pattern where the type object is a {@link ClassType} that matches the given predicate.
     *
     * @param predicate The predicate to test against the {@link ClassType}.
     * @return The type pattern.
     */
    public static @NotNull TypePattern isClass(final @NotNull Predicate<? super ClassType> predicate) {
        return (ctx, t) -> t instanceof final ClassType c && predicate.test(c);
    }

    /**
     * A type pattern where the type object is a {@link ClassType} and the {@link ClassType#getName()} matches the given
     * {@link Predicate predicate}.
     *
     * @param name The predicate the class name must match.
     * @return The type pattern.
     */
    public static @NotNull TypePattern isClassNamed(final @NotNull Predicate<? super String> name) {
        return (ctx, t) -> t instanceof final ClassType c && name.test(c.getName());
    }
    /**
     * A type pattern where the type object is a {@link ClassType} and the {@link ClassType#getName()} matches the given
     * class name.
     *
     * @param name The text the class name must match.
     * @return The type pattern.
     */
    public static @NotNull TypePattern isClassNamed(final @NotNull String name) {
        return isClassNamed(name::equals);
    }

    /**
     * A type pattern where the type object is a reference type ({@link ClassType}, {@link ArrayType}, or
     * {@link ReferenceTypeSignature}).
     * @return The type pattern.
     */
    public static @NotNull TypePattern isReferenceType() {
        return (ctx, t) -> t instanceof ClassType || t instanceof ArrayType || t instanceof ReferenceTypeSignature;
    }

    /**
     * A type pattern where the type object is a primitive type (does not include {@code void}).
     * @return The type pattern.
     */
    public static @NotNull TypePattern isPrimitive() {
        return (ctx, t) -> t instanceof PrimitiveType;
    }

    /**
     * A type pattern where the type object is {@code void}.
     * @return The type pattern.
     */
    public static @NotNull TypePattern isVoid() {
        return (ctx, t) -> t == VoidType.INSTANCE;
    }

    /**
     * A type pattern where the type object is {@code char} (not the wrapper type).
     * @return The type pattern.
     */
    public static @NotNull TypePattern isChar() {
        return (ctx, t) -> t == PrimitiveType.CHAR;
    }

    /**
     * A type pattern where the type object is {@code byte} (not the wrapper type).
     * @return The type pattern.
     */
    public static @NotNull TypePattern isByte() {
        return (ctx, t) -> t == PrimitiveType.BYTE;
    }

    /**
     * A type pattern where the type object is {@code short} (not the wrapper type).
     * @return The type pattern.
     */
    public static @NotNull TypePattern isShort() {
        return (ctx, t) -> t == PrimitiveType.SHORT;
    }

    /**
     * A type pattern where the type object is {@code int} (not the wrapper type).
     * @return The type pattern.
     */
    public static @NotNull TypePattern isInt() {
        return (ctx, t) -> t == PrimitiveType.INT;
    }

    /**
     * A type pattern where the type object is {@code long} (not the wrapper type).
     * @return The type pattern.
     */
    public static @NotNull TypePattern isLong() {
        return (ctx, t) -> t == PrimitiveType.LONG;
    }

    /**
     * A type pattern where the type object is {@code float} (not the wrapper type).
     * @return The type pattern.
     */
    public static @NotNull TypePattern isFloat() {
        return (ctx, t) -> t == PrimitiveType.FLOAT;
    }

    /**
     * A type pattern where the type object is {@code double} (not the wrapper type).
     * @return The type pattern.
     */
    public static @NotNull TypePattern isDouble() {
        return (ctx, t) -> t == PrimitiveType.DOUBLE;
    }

    /**
     * A type pattern where the type object is {@code boolean} (not the wrapper type).
     * @return The type pattern.
     */
    public static @NotNull TypePattern isBoolean() {
        return (ctx, t) -> t == PrimitiveType.BOOLEAN;
    }

    /**
     * A type pattern where the type object is a primitive integer type: {@code byte}, {@code short}, {@code int}, or
     * {@code long} (not the wrapper types).
     * @return The type pattern.
     */
    public static @NotNull TypePattern isIntegerType() {
        return (ctx, t) -> {
            if (t instanceof final PrimitiveType p) {
                return switch (p) {
                    case BYTE, SHORT, INT, LONG -> true;
                    default -> false;
                };
            }
            return false;
        };
    }

    /**
     * A type pattern where the type object is a primitive floating point type: {@code float} or {@code double}
     * (not the wrapper types).
     * @return The type pattern.
     */
    public static @NotNull TypePattern isFloatingPointType() {
        return (ctx, t) -> {
            if (t instanceof final PrimitiveType p) {
                return switch (p) {
                    case FLOAT, DOUBLE -> true;
                    default -> false;
                };
            }
            return false;
        };
    }

    /**
     * A type pattern where the type object is a type that is 2 slots wide in the LVT: {@code long} or {@code double}.
     * All other types, including reference types, are only 1 slot wide, which includes the wrapper types for the 2
     * matching primitive types.
     * @return The type pattern.
     */
    public static @NotNull TypePattern isWide() {
        return (ctx, t) -> {
            if (t instanceof final PrimitiveType p) {
                return switch (p) {
                    case LONG, DOUBLE -> true;
                    default -> false;
                };
            }
            return false;
        };
    }

    /**
     * A type pattern where the type object is allowed to be assigned to a field or a variable. Type objects which are
     * not assignable are, for example, {@code void}, method descriptors, and class signatures.
     * @return The type pattern.
     */
    public static @NotNull TypePattern isAssignable() {
        return (ctx, t) -> t instanceof ValueType && t != VoidType.INSTANCE;
    }

    /**
     * A type pattern where the type object is allowed to be a method's return type. Type objects which are not
     * returnable are, for example, method descriptors and class signatures.
     * @return The type pattern.
     */
    public static @NotNull TypePattern isReturnable() {
        return (ctx, t) -> t instanceof ValueType;
    }

    /**
     * A type pattern where the type object is an {@link ArrayType}.
     * @return The type pattern.
     */
    public static @NotNull TypePattern isArray() {
        return (ctx, t) -> t instanceof ArrayType;
    }

    /**
     * A type pattern where the type object is an {@link ArrayType}, and the component type for the array matches the
     * given pattern.
     *
     * @param baseType A pattern to match against the component type of the array.
     * @return The type pattern.
     */
    public static @NotNull TypePattern isArray(final @NotNull TypePattern baseType) {
        return (ctx, t) -> t instanceof final ArrayType a && baseType.test(ctx, a.getBaseType());
    }

    /**
     * A type pattern where the type object is an {@link ArrayType}, and the array's dimension matches the given value.
     *
     * @param dimension The dimension the array type must match.
     * @return The type pattern.
     */
    public static @NotNull TypePattern isArray(final int dimension) {
        return (ctx, t) -> t instanceof final ArrayType a && a.getDimension() == dimension;
    }

    /**
     * A type pattern where the type object is an {@link ArrayType}, the array's dimension matches the given value, and
     * the component type for the array matches the given pattern.
     *
     * @param dimension The dimension the array type must match.
     * @param baseType A pattern to match against the component type of the array.
     * @return The type pattern.
     */
    public static @NotNull TypePattern isArray(final int dimension, final @NotNull TypePattern baseType) {
        return (ctx, t) ->
            t instanceof final ArrayType a && a.getDimension() == dimension && baseType.test(ctx, a.getBaseType());
    }

    /**
     * A type pattern where the type object is an {@link ArrayType} that matches the given predicate.
     *
     * @param predicate The predicate to test against the {@link ArrayType}.
     * @return The type pattern.
     */
    public static @NotNull TypePattern isArray(final @NotNull Predicate<? super ArrayType> predicate) {
        return (ctx, t) -> t instanceof final ArrayType a && predicate.test(a);
    }

    /*
     * TypeDescriptor
     */

    /**
     * A type pattern where the type object is a {@link TypeDescriptor}.
     * @return The type pattern.
     */
    public static @NotNull TypePattern isTypeDescriptor() {
        return (ctx, t) -> t instanceof TypeDescriptor;
    }

    /**
     * A type pattern where the type object is a {@link TypeDescriptor} that matches the given predicate.
     *
     * @param predicate The predicate to test against the {@link TypeDescriptor}.
     * @return The type pattern.
     */
    public static @NotNull TypePattern isTypeDescriptor(final @NotNull Predicate<? super TypeDescriptor> predicate) {
        return (ctx, t) -> t instanceof final TypeDescriptor d && predicate.test(d);
    }

    /*
     * TypeSignature
     */

    /**
     * A type pattern where the type object is a {@link TypeSignature}.
     * @return The type pattern.
     */
    public static @NotNull TypePattern isTypeSignature() {
        return (ctx, t) -> t instanceof TypeSignature;
    }

    /**
     * A type pattern where the type object is a {@link TypeSignature} that matches the given predicate.
     *
     * @param predicate The predicate to test against the {@link TypeSignature}.
     * @return The type pattern.
     */
    public static @NotNull TypePattern isTypeSignature(final @NotNull Predicate<? super TypeSignature> predicate) {
        return (ctx, t) -> t instanceof final TypeSignature d && predicate.test(d);
    }

    /**
     * A type pattern where the type object is a {@link ClassTypeSignature} that also has at least one type argument.
     * @return The type pattern.
     */
    public static @NotNull TypePattern hasTypeArguments() {
        return (ctx, t) -> t instanceof final ClassTypeSignature c && !c.getTypeArguments().isEmpty();
    }

    /**
     * A type pattern where the type object is a {@link ClassTypeSignature} that also has no type arguments.
     * @return The type pattern.
     */
    // TODO clarify documentation on what this covers
    public static @NotNull TypePattern hasNoTypeArguments() {
        return (ctx, t) -> {
            if (t instanceof final ClassTypeSignature c) {
                return c.getTypeArguments().isEmpty();
            } else {
                return t instanceof ClassTypeDescriptor;
            }
        };
    }

    /**
     * A type pattern where the type object is a {@link ClassTypeSignature} that also has exactly as many type arguments
     * as the number of patterns given, and each pattern matches the corresponding type argument.
     *
     * @param typeParameters Array of type patterns which must match 1:1 with the type arguments of the
     *        {@link ClassTypeSignature}.
     * @return The type pattern.
     */
    public static @NotNull TypePattern hasTypeArguments(final @NotNull TypePattern... typeParameters) {
        return (ctx, t) -> {
            if (!(t instanceof final ClassTypeSignature sig)) {
                return false;
            }
            final List<? extends TypeArgument> args = sig.getTypeArguments();
            if (args.size() != typeParameters.length) {
                return false;
            }
            for (int i = 0; i < args.size(); i++) {
                final TypeArgument arg = args.get(i);
                if (!typeParameters[i].test(ctx, arg)) {
                    return false;
                }
            }
            return true;
        };
    }

    /**
     * A type pattern where the type object is a {@link ClassTypeSignature} that is owned by a
     * {@link ClassTypeSignature} that satisfies the given type pattern.
     *
     * @param owner The pattern which must match the owner of the {@link ClassTypeSignature}.
     * @return The type pattern.
     * @see ClassTypeSignature#getOwnerClass()
     */
    public static @NotNull TypePattern ownerIs(final @NotNull TypePattern owner) {
        return (ctx, t) -> {
            if (t instanceof final ClassTypeSignature c) {
                final ClassTypeSignature parentSig = c.getOwnerClass();
                if (parentSig == null) {
                    return false;
                }
                return owner.test(ctx, parentSig);
            }
            return false;
        };
    }
}
