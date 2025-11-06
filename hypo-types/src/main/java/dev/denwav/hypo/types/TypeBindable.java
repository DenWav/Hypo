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

import dev.denwav.hypo.types.sig.ClassSignature;
import dev.denwav.hypo.types.sig.MethodSignature;
import dev.denwav.hypo.types.sig.ReferenceTypeSignature;
import dev.denwav.hypo.types.sig.ThrowsSignature;
import dev.denwav.hypo.types.sig.TypeSignature;
import dev.denwav.hypo.types.sig.param.TypeArgument;
import dev.denwav.hypo.types.sig.param.TypeParameter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * A type which can be bound to a {@link dev.denwav.hypo.types.sig.param.TypeParameter TypeParameter}, or may contain
 * nested objects which themselves may satisfy that scenario. The intended pattern is for objects which fall in the
 * latter category to simply recursively call this method on all matching nested values.
 *
 * <p>Type variable binding only applies to {@link dev.denwav.hypo.types.sig.TypeSignature TypeSignature}-based types.
 */
public sealed interface TypeBindable
    extends TypeRepresentable
    permits TypeSignature, ClassSignature, MethodSignature, ReferenceTypeSignature, ThrowsSignature, TypeArgument, TypeParameter {

    /**
     * Return a new instance of {@code this} (possibly as a different type) where all unbound type variables have been
     * bound to their associated type parameters using the provided {@code binder}. All valid type definitions must be
     * able to be bound, so failure to find a valid type parameter definition for an unbound type variable will result
     * in an {@link IllegalStateException}.
     *
     * @param binder The {@link TypeVariableBinder} to bind with.
     * @return A new instance of {@code this} (possibly as a different type) where all unbound type variables have been
     *         bound to their associated type parameters
     * @throws IllegalStateException If a type variable could not be bound to an appropriate type parameter.
     */
    @NotNull TypeBindable bind(final @NotNull TypeVariableBinder binder);

    /**
     * Returns true if {@code this} contains any instances of
     * {@link dev.denwav.hypo.types.sig.param.TypeVariable.Unbound TypeVariable.Unbound}. If this method returns
     * {@code true} then certain resolution operations such as
     * {@link dev.denwav.hypo.types.sig.TypeSignature#asDescriptor() TypeSignatureasDescriptor()} will fail.
     *
     * @return {@code true} if {@code this} contains an unbound type variable.
     */
    boolean isUnbound();

    /**
     * Internal method, use {@link #asInternal(StringBuilder)} or {@link #asInternal()} instead.
     * @param sb The string builder.
     * @param withBindKey Bind key flag.
     */
    @ApiStatus.Internal
    void asInternal(final @NotNull StringBuilder sb, final boolean withBindKey);

    @Override
    default @NotNull String internKey() {
        final StringBuilder sb = new StringBuilder();
        this.asInternal(sb, true);
        return sb.toString();
    }
}
