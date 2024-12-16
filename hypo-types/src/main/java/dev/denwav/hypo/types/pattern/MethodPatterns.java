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

import dev.denwav.hypo.types.desc.MethodDescriptor;
import dev.denwav.hypo.types.kind.MethodType;
import dev.denwav.hypo.types.kind.ValueType;
import dev.denwav.hypo.types.sig.MethodSignature;
import dev.denwav.hypo.types.sig.ThrowsSignature;
import dev.denwav.hypo.types.sig.TypeParameterHolder;
import dev.denwav.hypo.types.sig.param.TypeParameter;
import java.util.function.Predicate;
import org.jetbrains.annotations.NotNull;

/**
 * Factory for {@link TypePattern} for {@link dev.denwav.hypo.types.desc.MethodDescriptor MethodDescriptor} and
 * {@link MethodSignature} type objects.
 */
public final class MethodPatterns {

    private MethodPatterns() {}

    /*
     * MethodDescriptor & MethodSignature
     */

    /**
     * A type pattern where the type object is a {@link MethodType}.
     * @return The type pattern.
     */
    public @NotNull TypePattern isMethod() {
        return (ctx, t) -> t instanceof MethodType;
    }

    /**
     * A type pattern where the type object is a {@link MethodType} that matches the given predicate.
     *
     * @param predicate The predicate to test against the {@link MethodType}.
     * @return The type pattern.
     */
    public @NotNull TypePattern isMethod(final Predicate<? super MethodType> predicate) {
        return (ctx, t) -> t instanceof final MethodType m && predicate.test(m);
    }

    /**
     * A type pattern where the type object is a {@link MethodType}, and the method has at least one parameter.
     * @return The type pattern.
     */
    public @NotNull TypePattern hasParams() {
        return (ctx, t) -> t instanceof final MethodType m && !m.getParameters().isEmpty();
    }

    /**
     * A type pattern where the type object is a {@link MethodType}, and the method has no parameters.
     * @return The type pattern.
     */
    public @NotNull TypePattern hasNoParams() {
        return (ctx, t) -> t instanceof final MethodType m && m.getParameters().isEmpty();
    }

    /**
     * A type pattern where the type object is a {@link MethodType} that also has exactly as many parameters as the
     * number of patterns given, and each pattern matches the corresponding parameter.
     *
     * @param params Array of type patterns which must match 1:1 with the parameters of the {@link MethodType}.
     * @return The type pattern.
     */
    public @NotNull TypePattern hasParams(final @NotNull TypePattern... params) {
        return (ctx, t) -> {
            if (!(t instanceof final MethodType m)) {
                return false;
            }
            if (m.getParameters().size() != params.length) {
                return false;
            }
            for (int i = 0; i < m.getParameters().size(); i++) {
                final ValueType paramType = m.getParameters().get(i);
                if (!params[i].test(ctx, paramType)) {
                    return false;
                }
            }
            return true;
        };
    }

    /**
     * A type pattern where the type object is a {@link MethodType} and the return type matches the given pattern.
     *
     * @param returnType The pattern to match the return type of the {@link MethodType}.
     * @return The type pattern.
     */
    public @NotNull TypePattern hasReturnType(final @NotNull TypePattern returnType) {
        return (ctx, t) -> t instanceof final MethodType m && returnType.test(ctx, m.getReturnType());
    }

    /*
     * MethodDescriptor
     */

    /**
     * A type pattern where the type object is a {@link MethodDescriptor}.
     * @return The type pattern.
     */
    public @NotNull TypePattern isMethodDescriptor() {
        return (ctx, t) -> t instanceof MethodDescriptor;
    }

    /**
     * A type pattern where the type object is a {@link MethodDescriptor} that matches the given predicate.
     *
     * @param predicate The predicate to test against the {@link MethodDescriptor}.
     * @return The type pattern.
     */
    public @NotNull TypePattern isMethodDescriptor(final @NotNull Predicate<? super MethodDescriptor> predicate) {
        return (ctx, t) -> t instanceof final MethodDescriptor d && predicate.test(d);
    }

    /*
     * MethodSignature
     */

    /**
     * A type pattern where the type object is a {@link MethodSignature}.
     * @return The type pattern.
     */
    public @NotNull TypePattern isMethodSignature() {
        return (ctx, t) -> t instanceof MethodSignature;
    }

    /**
     * A type pattern where the type object is a {@link MethodSignature} that matches the given predicate.
     *
     * @param predicate The predicate to test against the {@link MethodSignature}.
     * @return The type pattern.
     */
    public @NotNull TypePattern isMethodSignature(final @NotNull Predicate<? super MethodSignature> predicate) {
        return (ctx, t) -> t instanceof final MethodSignature s && predicate.test(s);
    }

    /**
     * A type pattern where the type object is a {@link MethodSignature} that has at least one throws signature.
     * @return The type pattern.
     */
    public @NotNull TypePattern hasThrows() {
        return (ctx, t) -> {
            if (!(t instanceof final MethodSignature sig)) {
                return false;
            }
            return !sig.getThrowsSignatures().isEmpty();
        };
    }

    /**
     * A type pattern where the type object is a {@link MethodSignature} that has no throws signature.
     * @return The type pattern.
     */
    public @NotNull TypePattern hasNoThrows() {
        return (ctx, t) -> {
            if (!(t instanceof final MethodSignature sig)) {
                return false;
            }
            return sig.getThrowsSignatures().isEmpty();
        };
    }

    /**
     * A type pattern where the type object is a {@link MethodSignature} that also has exactly as many throws signatures
     * as the number of patterns given, and each pattern matches the corresponding throws signature.
     *
     * @param throwsClauses Array of type patterns which must match 1:1 with the throws signatures of the
     *        {@link MethodSignature}.
     * @return The type pattern.
     */
    public @NotNull TypePattern hasThrows(final @NotNull TypePattern... throwsClauses) {
        return (ctx, t) -> {
            if (!(t instanceof final MethodSignature sig)) {
                return false;
            }
            if (sig.getThrowsSignatures().size() != throwsClauses.length) {
                return false;
            }
            for (int i = 0; i < sig.getThrowsSignatures().size(); i++) {
                final ThrowsSignature throwsSig = sig.getThrowsSignatures().get(i);
                if (!throwsClauses[i].test(ctx, throwsSig)) {
                    return false;
                }
            }
            return true;
        };
    }

    /**
     * A type pattern where the type object is a {@link TypeParameterHolder} and it has at least one type parameter.
     * @return The type pattern.
     */
    public static @NotNull TypePattern hasTypeParameters() {
        return (ctx, t) -> {
            if (t instanceof final TypeParameterHolder paramHolder) {
                return !paramHolder.getTypeParameters().isEmpty();
            }
            return false;
        };
    }

    /**
     * A type pattern where the type object is a {@link TypeParameterHolder} and it has no type parameters.
     * @return The type pattern.
     */
    public static @NotNull TypePattern hasNoTypeParameters() {
        return (ctx, t) -> {
            if (t instanceof final TypeParameterHolder paramHolder) {
                return paramHolder.getTypeParameters().isEmpty();
            }
            return false;
        };
    }

    /**
     * A type pattern where the type object is a {@link TypeParameterHolder} that also has exactly as many type
     * parameters as the number of patterns given, and each pattern matches the corresponding type parameter.
     *
     * @implNote This is re-used for {@link ClassSignaturePatterns#hasTypeParameters(TypePattern...)}, either this
     *           method or that method behave the same, these factories are just placed on the methods for the objects
     *           they are found on for convenience and clarity.
     *
     * @param typeParameters Array of type patterns which must match 1:1 with the type parameteres of the
     *        {@link TypeParameterHolder}.
     * @return The type pattern.
     */
    public static @NotNull TypePattern hasTypeParameters(final TypePattern... typeParameters) {
        return (ctx, t) -> {
            if (t instanceof final TypeParameterHolder paramHolder) {
                if (paramHolder.getTypeParameters().size() != typeParameters.length) {
                    return false;
                }
                for (int i = 0; i < paramHolder.getTypeParameters().size(); i++) {
                    final TypeParameter param = paramHolder.getTypeParameters().get(i);
                    if (!typeParameters[i].test(ctx, param)) {
                        return false;
                    }
                }
                return true;
            }
            return false;
        };
    }
}
