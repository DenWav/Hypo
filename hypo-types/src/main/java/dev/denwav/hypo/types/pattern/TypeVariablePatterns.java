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

import dev.denwav.hypo.types.sig.param.TypeParameter;
import dev.denwav.hypo.types.sig.param.TypeVariable;
import java.util.function.Predicate;
import org.jetbrains.annotations.NotNull;

/**
 * Factory for {@link TypePattern} for {@link TypeVariable} type objects.
 */
public final class TypeVariablePatterns {

    private TypeVariablePatterns() {}

    /**
     * A type pattern where the type object is a {@link TypeVariable}.
     * @return The type pattern.
     */
    public static @NotNull TypePattern isTypeVariable() {
        return (ctx, t) -> t instanceof TypeVariable;
    }

    /**
     * A type pattern where the type object is a {@link TypeVariable} that matches the given predicate.
     *
     * @param predicate The predicate to test against the {@link TypeVariable}.
     * @return The type pattern.
     */
    public static @NotNull TypePattern isTypeVariable(final @NotNull Predicate<? super TypeVariable> predicate) {
        return (ctx, t) -> t instanceof final TypeVariable v && predicate.test(v);
    }

    /**
     * A type pattern where the type object is a {@link TypeVariable} that is {@link TypeVariable#isUnbound()} bound.
     * @return The type pattern.
     */
    public static @NotNull TypePattern isBound() {
        return (ctx, t) -> t instanceof final TypeVariable v && !v.isUnbound();
    }

    /**
     * A type pattern where the type object is a {@link TypeVariable} where the
     * {@link TypeVariable#getDefinition() definition} matches the given predicate.
     *
     * @param predicate The predicate to test against the {@link TypeVariable}.
     * @return The type pattern.
     */
    public static @NotNull TypePattern isBound(final @NotNull Predicate<? super TypeParameter> predicate) {
        return (ctx, t) -> t instanceof final TypeVariable v && predicate.test(v.getDefinition());
    }

    /**
     * A type pattern where the type object is a {@link TypeVariable} where the
     * {@link TypeVariable#getDefinition() definition} satisfies the given {@link TypePattern}.
     *
     * @param definition The predicate to test against the {@link TypeVariable}.
     * @return The type pattern.
     */
    public static @NotNull TypePattern isBound(final @NotNull TypePattern definition) {
        return (ctx, t) -> t instanceof TypeVariable && definition.test(ctx, t);
    }

    /**
     * A type pattern where the type object is a {@link TypeVariable} that is {@link TypeVariable#isUnbound()} unbound.
     * @return The type pattern.
     */
    public static @NotNull TypePattern isUnbound() {
        return (ctx, t) -> t instanceof final TypeVariable v && v.isUnbound();
    }

    /**
     * A type pattern where the type object is a {@link TypeVariable.Unbound unbound TypeVariable} that matches the
     * given predicate.
     *
     * @param predicate The predicate to test against the {@link TypeVariable.Unbound unbound TypeVariable}.
     * @return The type pattern.
     */
    public static @NotNull TypePattern isUnbound(final @NotNull Predicate<? super TypeVariable.Unbound> predicate) {
        return (ctx, t) -> t instanceof final TypeVariable.Unbound u && predicate.test(u);
    }
}
