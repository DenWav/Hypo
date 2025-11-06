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

import dev.denwav.hypo.types.sig.param.BoundedTypeArgument;
import dev.denwav.hypo.types.sig.param.TypeArgument;
import dev.denwav.hypo.types.sig.param.WildcardArgument;
import dev.denwav.hypo.types.sig.param.WildcardBound;
import java.util.function.Predicate;
import org.jetbrains.annotations.NotNull;

/**
 * Factory for {@link TypePattern} for {@link TypeArgument}, {@link WildcardArgument}, and {@link BoundedTypeArgument}
 * type objects.
 */
public final class TypeArgumentPatterns {

    private TypeArgumentPatterns() {}

    /**
     * A type pattern where the type object is a {@link TypeArgument}.
     * @return The type pattern.
     */
    public static @NotNull TypePattern isTypeArgument() {
        return (ctx, t) -> t instanceof TypeArgument;
    }

    /**
     * A type pattern where the type object is a {@link TypeArgument} that matches the given predicate.
     *
     * @param predicate The predicate to test against the {@link TypeArgument}.
     * @return The type pattern.
     */
    public static @NotNull TypePattern isTypeArgument(final @NotNull Predicate<? super TypeArgument> predicate) {
        return (ctx, t) -> t instanceof final TypeArgument a && predicate.test(a);
    }

    /**
     * A type pattern where the type object is a {@link WildcardArgument}.
     * @return The type pattern.
     */
    public static @NotNull TypePattern isWildcard() {
        return (ctx, t) -> t == WildcardArgument.INSTANCE;
    }

    /**
     * A type pattern where the type object is a {@link BoundedTypeArgument}.
     * @return The type pattern.
     */
    public static @NotNull TypePattern isBoundedArgument() {
        return (ctx, t) -> t instanceof BoundedTypeArgument;
    }

    /**
     * A type pattern where the type object is a {@link BoundedTypeArgument} and {@link BoundedTypeArgument#getBounds()}
     * is {@link WildcardBound#UPPER}.
     * @return The type pattern.
     */
    public static @NotNull TypePattern hasUpperBound() {
        return (ctx, t) -> t instanceof final BoundedTypeArgument b && b.getBounds() == WildcardBound.UPPER;
    }

    /**
     * A type pattern where the type object is a {@link BoundedTypeArgument} and {@link BoundedTypeArgument#getBounds()}
     * is {@link WildcardBound#LOWER}.
     * @return The type pattern.
     */
    public static @NotNull TypePattern hasLowerBound() {
        return (ctx, t) -> t instanceof final BoundedTypeArgument b && b.getBounds() == WildcardBound.LOWER;
    }

    /**
     * A type pattern where the type object is a {@link BoundedTypeArgument} and
     * {@link BoundedTypeArgument#getSignature() bound type} of the argument matches the given type pattern.
     *
     * @param bounds The pattern to test against the bound type of the {@link BoundedTypeArgument}.
     * @return The type pattern.
     */
    public static @NotNull TypePattern hasBounds(final TypePattern bounds) {
       return (ctx, t) -> t instanceof final BoundedTypeArgument b && bounds.test(ctx, b.getSignature());
    }
}
