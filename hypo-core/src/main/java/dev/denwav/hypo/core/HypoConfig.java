/*
 * Hypo, an extensible and pluggable Java bytecode analytical model.
 *
 * Copyright (C) 2021  Kyle Wood (DenWav)
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

package dev.denwav.hypo.core;

import dev.denwav.hypo.model.ClassDataDecorator;
import dev.denwav.hypo.model.ClassDataProvider;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.Immutable;
import java.util.function.Function;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Core global configuration for Hypo executions.
 *
 * <p>Create new instances of this class using {@link #builder()}.
 */
@Immutable
public final class HypoConfig {

    private final int parallelism;
    @SuppressWarnings("Immutable")
    private final @NotNull Function<ClassDataProvider, ClassDataDecorator> decorator;

    /**
     * Create a new instance of {@link HypoConfig}. Use {@link #builder()} instead.
     *
     * @param parallelism The parallelism level to use for Hypo executions.
     * @param decorator The decorator to use for {@link ClassDataProvider#setDecorator(ClassDataDecorator)}.
     */
    HypoConfig(
        final int parallelism,
        final @NotNull Function<ClassDataProvider, ClassDataDecorator> decorator
    ) {
        this.parallelism = parallelism;
        this.decorator = decorator;
    }

    /**
     * Returns the parallelism level to use for Hypo executions.
     * @return The parallelism level to use for Hypo executions.
     */
    public int getParallelism() {
        return this.parallelism;
    }

    /**
     * Returns the {@link ClassDataDecorator decorator} constructor to use for
     * {@link ClassDataProvider#setDecorator(ClassDataDecorator)}.
     *
     * @return The {@link ClassDataDecorator decorator} constructor to use.
     */
    public @NotNull Function<ClassDataProvider, ClassDataDecorator> getDecorator() {
        return this.decorator;
    }

    /**
     * Create a new {@link Builder builder} for creating new instances of {@link HypoConfig}.
     *
     * @return A new {@link Builder} instance.
     */
    @Contract(value = "-> new ", pure = true)
    public static @NotNull Builder builder() {
        return new Builder();
    }

    /**
     * Builder class for creating new instances of {@link HypoConfig}. Create new instances of this builder with
     * {@link HypoConfig#builder()}.
     *
     * <p>This class is structured as the write-only version of {@link HypoConfig}, which is read-only.
     */
    public static final class Builder {

        private int parallelism = -1;
        private @NotNull Function<ClassDataProvider, ClassDataDecorator> decorator = DefaultClassDataDecorator::new;

        /**
         * Constructor for {@link Builder}. Use {@link HypoConfig#builder()} instead.
         */
        Builder() {}

        /**
         * Set the level of parallelism to use for Hypo executions.
         *
         * <p>Defaults to {@code -1}, which means to use {@link Runtime#availableProcessors()}.
         *
         * @param parallelism The level of parallelism to use for Hypo executions.
         * @return {@code this} for chaining.
         */
        @CanIgnoreReturnValue
        @Contract(value = "_ -> this", mutates = "this")
        public @NotNull Builder withParallelism(final int parallelism) {
            this.parallelism = parallelism;
            return this;
        }

        /**
         * Set the {@link ClassDataDecorator decorator} constructor to use for
         * {@link ClassDataProvider#setDecorator(ClassDataDecorator)}.
         *
         * <p>Defaults to {@link DefaultClassDataDecorator#DefaultClassDataDecorator(ClassDataProvider)
         * DefaultClassDataDecorator::new}.
         *
         * @param decorator The {@link ClassDataDecorator decorator} constructor to use.
         * @return {@code this} for chaining.
         */
        @CanIgnoreReturnValue
        @Contract(value = "_ -> this", mutates = "this")
        public @NotNull Builder withDecorator(
            final @NotNull Function<ClassDataProvider, ClassDataDecorator> decorator
        ) {
            this.decorator = decorator;
            return this;
        }

        /**
         * Use the current values of this builder to create a new instance of {@link HypoConfig} and return it.
         *
         * @return The new instance of {@link HypoConfig} using the value set in this builder.
         */
        @CanIgnoreReturnValue
        @Contract(value = "-> new", pure = true)
        public @NotNull HypoConfig build() {
            return new HypoConfig(this.parallelism, this.decorator);
        }
    }
}
