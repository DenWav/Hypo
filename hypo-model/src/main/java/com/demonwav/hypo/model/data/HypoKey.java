/*
 * Hypo, an extensible and pluggable Java bytecode analytical model.
 *
 * Copyright (C) 2021  Kyle Wood (DemonWav)
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

package com.demonwav.hypo.model.data;

import com.google.errorprone.annotations.Immutable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Marker class for retrieving and setting arbitrary data on {@link HypoData} objects. All keys are stored and compared
 * by identity, so any key in use should be declared as a {@code static final} variable and referenced directly.
 *
 * <p>Key can be created using the {@link #create(String)} method.
 *
 * @param <T> The type of the value associated with the key.
 */
@Immutable
@SuppressWarnings("unused") // type param T is used externally
public final class HypoKey<T> {

    private final @NotNull String name;

    private HypoKey(final @NotNull String name) {
        this.name = name;
    }

    /**
     * Create a new key with the given name. The name is entirely for debugging use only - {@link HypoKey} objects only
     * have identity, they have no value. All comparisons are lookups with {@link HypoKey} objects is done using the
     * object identity.
     *
     * @param name The name of the key, for debugging purposes.
     * @param <T> The type of value which will be associated with this key.
     * @return The new key.
     */
    @Contract(value = "_ -> new", pure = true)
    public static <T> @NotNull HypoKey<T> create(final @NotNull String name) {
        return new HypoKey<>(name);
    }

    /**
     * Returns the name of this key, for debugging use.
     * @return The name of this key, for debugging use.
     */
    public @NotNull String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return "HypoKey[" + this.name + "]";
    }

    /*
     * The following 2 methods are overridden solely to make it extra clear this class only has identity, no value.
     */

    @Override
    public final boolean equals(final Object o) {
        return this == o;
    }

    @Override
    public final int hashCode() {
        return System.identityHashCode(this);
    }
}
