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

import dev.denwav.hypo.types.sig.param.TypeParameter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An object which provides bindings for {@link dev.denwav.hypo.types.sig.param.TypeVariable TypeVariables}. This is
 * generally as simple as finding the instance of {@link TypeParameter} which correspond with the given type variable
 * name.
 *
 * <p>Typical usage of this class is by passing it into the {@link TypeBindable#bind(TypeVariableBinder)} method. A
 * convenience method is also provided, {@link #bind(TypeBindable)}, which inverts that mechanism. This can make it
 * simpler when dealing with possibly {@code null} values.
 */
public interface TypeVariableBinder {

    /**
     * A convenience method which inverts the typical order of the {@link TypeBindable#bind(TypeVariableBinder)} method
     * call. This can be used to safely call the bind method on possibly {@code null} objects.
     *
     * @param bindable The bindable object to pass this object into.
     * @return The result of calling {@link TypeBindable#bind(TypeVariableBinder)} if {@code bindable} is not null,
     * @param <T> The type of the bindable.
     */
    default <T extends TypeBindable> @Nullable T bind(final @Nullable T bindable) {
        if (bindable == null) {
            return null;
        }
        return HypoTypesUtil.cast(bindable.bind(this));
    }

    /**
     * Return the associated {@link TypeParameter} for the given variable name.
     *
     * @param name The variable name.
     * @return The associated {@link TypeParameter}, or {@code null} if not found.
     */
    @Nullable TypeParameter bindingFor(final @NotNull String name);

    /**
     * Essentially a no-op operation which binds all unbound type variables to a fictional {@link TypeParameter}. This
     * can be used to guarantee a {@link dev.denwav.hypo.types.sig.TypeSignature TypeSignature} will always resolve,
     * even if the result is not accurate.
     *
     * @return A basic {@link TypeVariableBinder} which produces fictional bindings.
     */
    static TypeVariableBinder object() {
        return TypeParameter::of;
    }
}
