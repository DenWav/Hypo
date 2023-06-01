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

package dev.denwav.hypo.model.data;

import java.util.function.Supplier;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Generic data store for Hypo Java class model objects. Any class representing a Java class or a member of a Java
 * class must implement this interface and allow arbitrary storage of data based on {@link HypoKey} values.
 *
 * <p>All {@link HypoKey keys} are stored and matched by their identity. So, any key in use should be declared as a
 * {@code static final} variable and referenced directly whenever used.
 *
 * @see AbstractHypoData
 */
public interface HypoData {

    /**
     * Store the given value {@code t} into this data store associated with the given {@link HypoKey key}. A
     * {@code null} value means to remove the {@code key} from this store.
     *
     * @param key The key to associate the value with, or if the value is {@code null}, the key to remove.
     * @param t The value to store, or {@code null} to remove.
     * @param <T> The type of the value.
     * @return The value inserted into the store.
     */
    @Contract("_, _ -> param2")
    <T> @Nullable T store(final @NotNull HypoKey<T> key, final @Nullable T t);

    /**
     * Use the given {@link Supplier supplier} to compute a new value for the given {@link HypoKey key}, if there is
     * no value associated with the given key already. If there is already a value for the given key, simply return it.
     * This operation is atomic, if multiple threads call this method concurrently on a single supplier will run and
     * all threads will receive the same result value.
     *
     * @param key The key to associate the value with.
     * @param supplier The supplier to generate the new value for the key, if the key is not already present.
     * @param <T> The type of the value.
     * @return The value inserted into the store.
     */
    @Contract("_, _, -> param2")
    <T> @NotNull T compute(final @NotNull HypoKey<T> key, final @NotNull Supplier<T> supplier);

    /**
     * Get the value associated with the given {@link HypoKey key} from the store, or {@code null} if there is no value
     * for the given key.
     *
     * @param key the key to retrieve the value for.
     * @param <T> The type of the value.
     * @return The value associated with the given key, or {@code null} if there is none.
     * @see #require(HypoKey)
     * @see #require(HypoKey, Supplier)
     */
    <T> @Nullable T get(final @NotNull HypoKey<T> key);

    /**
     * Get the value associated with the given {@link HypoKey key}. If there is no value for the given key, this method
     * will throw a {@link NullPointerException} instead.
     *
     * @param key The key to retrieve the value for.
     * @param <T> The type of the value.
     * @return The value associated with the given key.
     * @throws NullPointerException If there is no value associated with the given key in the store.
     * @see #get(HypoKey)
     * @see #require(HypoKey, Supplier)
     */
    default <T> @NotNull T require(final @NotNull HypoKey<T> key) {
        return this.require(key, () -> new NullPointerException("Key not present: " + key.getName()));
    }

    /**
     * Get the value associated with the given {@link HypoKey key}. If there is no value for the given key, this method
     * will throw the exception returned from the given {@code supplier}.
     *
     * @param key The key to retrieve the value for.
     * @param supplier The supplier to generate the exception to throw if there is no value associated with the given
     *                 key in the store.
     * @param <T> The type of the value.
     * @param <X> The type of the exception to throw.
     * @return The value associated with the given key.
     * @throws X If there is no value associated with the given key in the store.
     * @see #get(HypoKey)
     * @see #require(HypoKey)
     */
    default <T, X extends Throwable> @NotNull T require(
        final @NotNull HypoKey<T> key,
        final @NotNull Supplier<? extends X> supplier
    ) throws X {
        final T value = this.get(key);
        if (value == null) {
            throw supplier.get();
        }
        return value;
    }

    /**
     * Returns {@code true} if this store contains a value associated with the given {@link HypoKey key}, otherwise
     * it will return {@code false}.
     *
     * @param key The key to check for existence in this store.
     * @return {@code true} if this store contains a value for the given key, {@code false} if otherwise.
     */
    boolean contains(final @NotNull HypoKey<?> key);
}
