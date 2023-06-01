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

import dev.denwav.hypo.model.HypoModelUtil;
import com.google.errorprone.annotations.concurrent.LazyInit;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A lazily computed storage of a single value. The value this class stores is not computed until it is requested, and
 * once it is computed it is stored for all subsequent accesses.
 *
 * <p>This class is thread-safe: The {@link Generator generator} will only be called a single time, it will never be
 * called concurrently for the same instance of this class, and concurrent accesses to this value while the generator is
 * running will receive the value returned by the generator.
 *
 * <p>Due to the lazy nature of this class, the first access to the value stored by this class may take significantly
 * longer than any additional accesses after, depending on how long the {@link Generator generator} takes to complete.
 * As this value is locked while the generator is running any additional accesses which queue up while the generator is
 * running will also take longer while all of the threads wait for the generator to complete.
 *
 * <p>This class may contain {@code null} values, if the {@link Generator generator} returns {@code null}.
 *
 * <p>Instances of this class can be created with the {@link #of(Generator)} method.
 *
 * @param <T> The type this class will store.
 * @param <X> The type of the exception the generator can throw.
 */
public final class LazyValue<T, X extends Throwable> {

    @LazyInit private @Nullable T value = null;
    private @Nullable Generator<T, X> generator;

    private LazyValue(final @NotNull Generator<T, X> generator) {
        this.generator = generator;
    }

    /**
     * Create a new lazy value which will use the given {@link Generator generator} to create the value once it is
     * requested.
     *
     * @param generator The function to run to create the value which this class will store.
     * @param <T> The type of the value which this class will store.
     * @param <X> The type of the exception the generator can throw.
     * @return The new lazy value.
     */
    public static <T, X extends Throwable> @NotNull LazyValue<T, X> of(final @NotNull Generator<T, X> generator) {
        return new LazyValue<>(generator);
    }

    /**
     * Get the value stored in this object, computing it first if needed. May return {@code null} if the generator
     * returns {@code null}.
     *
     * <p>This method will re-throw any exceptions thrown by the generator as unchecked.
     *
     * @return The value stored in this object.
     */
    public @Nullable T get() {
        try {
            return this.getOrThrow();
        } catch (final Throwable e) {
            throw HypoModelUtil.rethrow(e);
        }
    }

    /**
     * Non-null variant of {@link #get()}. This method will throw a {@link NullPointerException} if {@link #get()}
     * returns {@code null}.
     *
     * @return The value stored in this object.
     * @see #getOrThrowNotNull()
     */
    public @NotNull T getNotNull() {
        return Objects.requireNonNull(this.get());
    }

    /**
     * Get the value stored in this object, computing it first if needed. May return {@code null} if the generator
     * returns {@code null}.
     *
     * @return The value stored in this object.
     * @throws X If the generator throws an exception.
     * @see #get()
     */
    public @Nullable T getOrThrow() throws X {
        T val = this.value;
        if (val != null) {
            return val;
        }

        synchronized (this) {
            val = this.value;
            if (val != null) {
                return val;
            }

            final Generator<T, X> gen = this.generator;
            if (gen == null) {
                return null;
            }

            val = gen.get();
            this.value = val;
            this.generator = null;
            return val;
        }
    }

    /**
     * Non-null variant of {@link #getOrThrow()}. This method will throw a {@link NullPointerException} if
     * {@link #getOrThrow()} returns {@code null}.
     *
     * @return The value stored in this object.
     * @throws X If the generator throws an exception.
     */
    public @NotNull T getOrThrowNotNull() throws X {
        return Objects.requireNonNull(this.getOrThrow());
    }

    /**
     * {@link java.util.function.Supplier Supplier} analog for {@link LazyValue}. The core difference here is generators
     * can throw checked exceptions.
     *
     * <p>This interface is mostly intended to be implemented via a lambda or method reference.
     *
     * @param <T> The type of the value this generator produces.
     * @param <X> The exception the generator may throw.
     */
    @FunctionalInterface
    public interface Generator<T, X extends Throwable> {

        /**
         * Compute the value for this generator.
         *
         * @return The value computed for this generator.
         * @throws X If an exception is thrown while computing the value.
         */
        T get() throws X;
    }
}
