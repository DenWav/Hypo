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

import dev.denwav.hypo.types.sig.ReferenceTypeSignature;
import dev.denwav.hypo.types.sig.param.TypeParameter;
import java.util.List;
import java.util.function.Predicate;
import org.jetbrains.annotations.NotNull;

/**
 * Factory for {@link TypePattern} for {@link TypeParameter} type objects.
 */
public final class TypeParameterPatterns {

    private TypeParameterPatterns() {}

    /**
     * A type pattern where the type object is a {@link TypeParameter}.
     * @return The type pattern.
     */
    public static @NotNull TypePattern isTypeParameter() {
        return (ctx, t) -> t instanceof TypeParameter;
    }

    /**
     * A type pattern where the type object is a {@link TypeParameter} that matches the given predicate.
     *
     * @param predicate The predicate to test against the {@link TypeParameter}.
     * @return The type pattern.
     */
    public static @NotNull TypePattern isTypeParameter(final @NotNull Predicate<? super TypeParameter> predicate) {
        return (ctx, t) -> t instanceof final TypeParameter p && predicate.test(p);
    }

    /**
     * A type pattern where the type object is a {@link TypeParameter} and the parameter's name matches the given
     * predicate.
     *
     * @param name The predicate the type parameter's name must match.
     * @return The type pattern.
     */
    public static @NotNull TypePattern hasName(final @NotNull Predicate<String> name) {
        return (ctx, t) -> t instanceof final TypeParameter p && name.test(p.getName());
    }

    /**
     * A type pattern where the type object is a {@link TypeParameter} and the parameter's name matches the given name.
     *
     * @param name The text the type parameter's name must match.
     * @return The type pattern.
     */
    public static @NotNull TypePattern hasName(final @NotNull String name) {
        return hasName(name::equals);
    }

    /**
     * A type pattern where the type object is a {@link TypeParameter} and the type parameter contains a class bound.
     * Class bounds may not be specified on a type parameter if there are interface bounds defined instead. In any case,
     * when no class bound is defined (when {@link TypeParameter#getClassBound()} is {@code null}) then it is implied to
     * be {@code Ljava/lang/Object;}.
     *
     * @return The type pattern.
     */
    public static @NotNull TypePattern hasClassBound() {
        return (ctx, t) ->
            t instanceof TypeParameter p && p.getClassBound() != null;
    }


    /**
     * A type pattern where the type object is a {@link TypeParameter} and the type parameter's class bound matches the
     * given pattern.
     *
     * @param classBound The pattern the type parameter's class bound must match.
     * @return The type pattern.
     */
    public static @NotNull TypePattern hasClassBound(final @NotNull TypePattern classBound) {
        return (ctx, t) ->
            t instanceof TypeParameter p && p.getClassBound() != null && classBound.test(ctx, p.getClassBound());
    }

    /**
     * A type pattern where the type object is a {@link TypeParameter} that has at least one interface bound.
     * @return The type pattern.
     */
    public static @NotNull TypePattern hasInterfaceBounds() {
        return (ctx, t) -> t instanceof final TypeParameter p && !p.getInterfaceBounds().isEmpty();
    }

    /**
     * A type pattern where the type object is a {@link TypeParameter} that has no interface bounds.
     * @return The type pattern.
     */
    public static @NotNull TypePattern hasNoInterfaceBounds() {
        return (ctx, t) -> t instanceof final TypeParameter p && p.getInterfaceBounds().isEmpty();
    }

    /**
     * A type pattern where the type object is a {@link TypeParameter} that also has exactly as many interface bounds
     * as the number of patterns given, and each pattern matches the corresponding interface bound.
     *
     * @param interfaceBounds Array of type patterns which must match 1:1 with the interface bounds of the
     *        {@link TypeParameter}.
     * @return The type pattern.
     */
    public static @NotNull TypePattern hasInterfaceBounds(final @NotNull TypePattern... interfaceBounds) {
        return (ctx, t) -> {
            if (!(t instanceof final TypeParameter p)) {
                return false;
            }
            final List<? extends ReferenceTypeSignature> inters = p.getInterfaceBounds();
            if (inters.size() != interfaceBounds.length) {
                return false;
            }
            for (int i = 0; i < inters.size(); i++) {
                final ReferenceTypeSignature inter = inters.get(i);
                if (!interfaceBounds[i].test(ctx, inter)) {
                    return false;
                }
            }
            return true;
        };
    }
}
