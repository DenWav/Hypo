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

import dev.denwav.hypo.types.sig.ClassSignature;
import dev.denwav.hypo.types.sig.ClassTypeSignature;
import dev.denwav.hypo.types.sig.TypeParameterHolder;
import java.util.List;
import java.util.function.Predicate;
import org.jetbrains.annotations.NotNull;

/**
 * Factory for {@link TypePattern} for {@link ClassSignature} type objects.
 */
public final class ClassSignaturePatterns {

    private ClassSignaturePatterns() {}

    /**
     * A type pattern where the type object is a {@link ClassSignature}.
     * @return The type pattern.
     */
    public static @NotNull TypePattern isClassSignature() {
        return (ctx, t) -> t instanceof ClassSignature;
    }

    /**
     * A type pattern where the type object is a {@link ClassSignature} that matches the given predicate.
     *
     * @param predicate The predicate to test against the {@link ClassSignature}.
     * @return The type pattern.
     */
    public static @NotNull TypePattern isClassSignature(final @NotNull Predicate<? super ClassSignature> predicate) {
        return (ctx, t) -> t instanceof final ClassSignature c && predicate.test(c);
    }

    /**
     * A type pattern where the type object is a {@link TypeParameterHolder} and it has at least one type parameter.
     * @return The type pattern.
     */
    public static @NotNull TypePattern hasTypeParameters() {
        // same impl, but it might make more sense to use this class vs the other due to context
        return MethodPatterns.hasTypeParameters();
    }

    /**
     * A type pattern where the type object is a {@link TypeParameterHolder} and it has no type parameters.
     * @return The type pattern.
     */
    public static @NotNull TypePattern hasNoTypeParameters() {
        // same impl, but it might make more sense to use this class vs the other due to context
        return MethodPatterns.hasNoTypeParameters();
    }

    /**
     * A type pattern where the type object is a {@link TypeParameterHolder} that also has exactly as many type
     * parameters as the number of patterns given, and each pattern matches the corresponding type parameter.
     *
     * @implNote This re-uses {@link MethodPatterns#hasTypeParameters(TypePattern...)}, either this method or that
     *           method behave the same, these factories are just placed on the methods for the objects they are found
     *           on for convenience and clarity.
     *
     * @param typeParameters Array of type patterns which must match 1:1 with the type parameters of the
     *        {@link TypeParameterHolder}.
     * @return The type pattern.
     */
    public static @NotNull TypePattern hasTypeParameters(final TypePattern... typeParameters) {
        // same impl, but it might make more sense to use this class vs the other due to context
        return MethodPatterns.hasTypeParameters(typeParameters);
    }

    /**
     * A type pattern where the type object is a {@link ClassSignature} where the super class matches the given type
     * pattern.
     *
     * @param superClass A pattern to match the super class of the {@link ClassSignature}.
     * @return The type pattern.
     */
    public static @NotNull TypePattern hasSuperClass(final TypePattern superClass) {
        return (ctx, t) -> t instanceof final ClassSignature c && superClass.test(ctx, c.getSuperClass());
    }

    /**
     * A type pattern where the type object is a {@link ClassSignature} and it has at least one super interface.
     * @return The type pattern.
     */
    public static @NotNull TypePattern hasSuperInterfaces() {
        return (ctx, t) -> t instanceof final ClassSignature c && !c.getSuperInterfaces().isEmpty();
    }

    /**
     * A type pattern where the type object is a {@link ClassSignature} and it has no type super interfaces.
     * @return The type pattern.
     */
    public static @NotNull TypePattern hasNoSuperInterfaces() {
        return (ctx, t) -> t instanceof final ClassSignature c && c.getSuperInterfaces().isEmpty();
    }

    /**
     * A type pattern where the type object is a {@link ClassSignature} that also has exactly as many super interfaces
     * as the number of patterns given, and each pattern matches the corresponding super interface.
     *
     * @param superInterfaces Array of type patterns which must match 1:1 with the super interfaces of the
     *        {@link ClassSignature}.
     * @return The type pattern.
     */
    public static @NotNull TypePattern hasSuperInterfaces(final TypePattern... superInterfaces) {
        return (ctx, t) -> {
            if (!(t instanceof final ClassSignature c)) {
                return false;
            }
            final List<? extends ClassTypeSignature> supers = c.getSuperInterfaces();
            if (supers.size() != superInterfaces.length) {
                return false;
            }
            for (int i = 0; i < supers.size(); i++) {
                final ClassTypeSignature superSig = supers.get(i);
                if (!superInterfaces[i].test(ctx, superSig)) {
                    return false;
                }
            }
            return true;
        };
    }
}
