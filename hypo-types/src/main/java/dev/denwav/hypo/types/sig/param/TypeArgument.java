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

package dev.denwav.hypo.types.sig.param;

import com.google.errorprone.annotations.Immutable;
import dev.denwav.hypo.types.TypeBindable;
import dev.denwav.hypo.types.TypeRepresentable;
import dev.denwav.hypo.types.TypeVariableBinder;
import dev.denwav.hypo.types.sig.ReferenceTypeSignature;
import dev.denwav.hypo.types.sig.TypeSignature;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * A type argument is the type value passed into the type parameter list of a generic type signature. Type arguments are
 * optional for type signatures to maintain backwards compatibility with pre-generic types. When a type does include
 * type arguments, however, they must match 1:1 with the declared type parameters.
 */
@Immutable
public sealed interface TypeArgument
    extends TypeBindable, TypeRepresentable
    permits ReferenceTypeSignature, BoundedTypeArgument, WildcardArgument {

    @Override
    @NotNull TypeArgument bind(final @NotNull TypeVariableBinder binder);

    /**
     * Create a new {@link TypeArgument} from the given {@link WildcardType}.
     * @param wildcardType The wildcard type.
     * @return A new {@link TypeArgument} from the given {@link WildcardType}.
     */
    static @NotNull TypeArgument of(final WildcardType wildcardType) {
        final Type[] upper = wildcardType.getUpperBounds();
        final Type[] lower = wildcardType.getLowerBounds();

        if (upper.length == 1 && upper[0] == Objects.class && lower.length == 0) {
            return WildcardArgument.INSTANCE;
        } else if (upper.length > 0) {
            return BoundedTypeArgument.of(
                WildcardBound.UPPER,
                (ReferenceTypeSignature) TypeSignature.of(wildcardType.getUpperBounds()[0])
            );
        } else {
            return BoundedTypeArgument.of(
                WildcardBound.LOWER,
                (ReferenceTypeSignature) TypeSignature.of(wildcardType.getLowerBounds()[0])
            );
        }
    }
}
