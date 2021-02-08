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

package com.demonwav.hypo.core;

import com.demonwav.hypo.model.ClassDataDecorator;
import com.demonwav.hypo.model.ClassDataProvider;
import com.demonwav.hypo.model.ClassDataProviderSet;
import com.demonwav.hypo.model.HypoModelUtil;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.concurrent.LazyInit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Core context for Hypo executions. A context contains 4 major components:
 *
 * <ol>
 *     <li>The core {@link ClassDataProvider provider} - The classes to analyze</li>
 *     <li>The context {@link ClassDataProvider provider} - Additional classes on the classpath for completing the
 *         analysis of the core provider</li>
 *     <li>The current {@link HypoConfig configuration} - Global configuration values</li>
 *     <li>The current {@link ExecutorService executor} - The shared executor to use for all multi-threaded work</li>
 * </ol>
 *
 * <p>When a context is closed both {@link ClassDataProvider providers} are closed and the
 * {@link ExecutorService executor} is shut down. The context should never be closed while processes in the executor are
 * still running. To put that another way, any operations which use the executor should wait for all submitted jobs to
 * complete before continuing.
 *
 * <p>Create new instances with {@link #builder()}.
 */
public final class HypoContext implements AutoCloseable {

    private final @NotNull HypoConfig config;

    private final @NotNull ClassDataProvider provider;
    private final @NotNull ClassDataProvider contextProvider;

    @LazyInit private @Nullable ExecutorService executor = null;

    /**
     * Create a new instance of {@link HypoContext}. Use {@link #builder()} instead.
     *
     * @param config The {@link HypoConfig config} to use for this context.
     * @param provider The {@link ClassDataProvider provider} to use as the core provider to analyze.
     * @param contextProvider The {@link ClassDataProvider provider} to use as the context provider.
     */
    HypoContext(
        final @NotNull HypoConfig config,
        final @NotNull ClassDataProvider provider,
        final @NotNull ClassDataProvider contextProvider
    ) {
        this.config = config;
        this.provider = provider;
        this.contextProvider = contextProvider;
    }

    /**
     * Returns the {@link HypoConfig config}.
     * @return The {@link HypoConfig config}.
     */
    public @NotNull HypoConfig getConfig() {
        return this.config;
    }

    /**
     * Returns the core {@link ClassDataProvider provider}.
     * @return The core {@link ClassDataProvider provider}.
     */
    public @NotNull ClassDataProvider getProvider() {
        return this.provider;
    }

    /**
     * Returns the context {@link ClassDataProvider provider}.
     * @return The context {@link ClassDataProvider provider}.
     */
    public @NotNull ClassDataProvider getContextProvider() {
        return this.contextProvider;
    }

    /**
     * Returns the current {@link ExecutorService executor}, if there is one. If there isn't one already created, a new
     * executor will be created according to the {@link #getConfig() configuration} and returned. This executor will be
     * used for all subsequent requests until this context is {@link #close() closed}.
     *
     * <p>This method is thread safe: Multiple threads may call it concurrently and they will all receive the same
     * executor. Concurrent accesses to this method while the executor is being created will all receive the same
     * executor.
     *
     * @return The current {@link ExecutorService executor}.
     */
    public @NotNull ExecutorService getExecutor() {
        ExecutorService e = this.executor;
        if (e != null) {
            return e;
        }

        synchronized (this) {
            e = this.executor;
            if (e != null) {
                return e;
            }

            if (this.config.getParallelism() <= 0) {
                e = Executors.newWorkStealingPool();
            } else {
                e = Executors.newWorkStealingPool(this.config.getParallelism());
            }
            this.executor = e;
            return e;
        }
    }

    @Override
    public void close() throws Exception {
        final ExecutorService exec = this.executor;
        if (exec != null) {
            // close() should never be called while there are still tasks processing
            exec.shutdownNow();
            this.executor = null;
        }

        Exception thrown = null;
        try {
            this.provider.close();
        } catch (final Exception e) {
            thrown = e;
        }
        try {
            this.contextProvider.close();
        } catch (final Exception e) {
            thrown = HypoModelUtil.addSuppressed(thrown, e);
        }

        if (thrown != null) {
            throw thrown;
        }
    }

    /**
     * Create a new {@link Builder builder} for creating new instances of {@link HypoContext}.
     *
     * @return A new {@link Builder} instance.
     */
    @Contract("-> new")
    public static @NotNull HypoContext.Builder builder() {
        return new Builder();
    }

    /**
     * Builder class for creating new instances of {@link HypoContext}. Create new instances of this builder with
     * {@link HypoContext#builder()}.
     *
     * <p>This class is structured as the write-only version of {@link HypoContext}, which is read-only.
     */
    public static final class Builder {

        private @Nullable HypoConfig config;

        private final @NotNull List<@NotNull ClassDataProvider> providers = new ArrayList<>();
        private final @NotNull List<@NotNull ClassDataProvider> contextProviders = new ArrayList<>();

        /**
         * Constructor for {@link Builder}. Use {@link HypoContext#builder()} instead.
         */
        Builder() {}

        /**
         * Add the given {@link ClassDataProvider provider} as a core provider to analyze.
         *
         * @param provider The provider to add to the set of core providers.
         * @return {@code this} for chaining.
         */
        @CanIgnoreReturnValue
        @Contract(value = "_ -> this", mutates = "this")
        public @NotNull HypoContext.Builder withProvider(final @NotNull ClassDataProvider provider) {
            this.providers.add(provider);
            return this;
        }

        /**
         * Add multiple {@link ClassDataProvider providers} as core providers to analyze.
         *
         * @param providers The providers to add to the set of core providers.
         * @return {@code this} for chaining.
         */
        @CanIgnoreReturnValue
        @Contract(value = "_ -> this", mutates = "this")
        public @NotNull HypoContext.Builder withProviders(final @NotNull ClassDataProvider @NotNull ... providers) {
            this.providers.addAll(Arrays.asList(providers));
            return this;
        }

        /**
         * Add a {@link Collection collection} of {@link ClassDataProvider provider} as core providers to analyze.
         *
         * @param providers The providers to add to the set of core providers.
         * @return {@code this} for chaining.
         */
        @CanIgnoreReturnValue
        @Contract(value = "_ -> this", mutates = "this")
        public @NotNull HypoContext.Builder withProviders(
            final @NotNull Collection<@NotNull ? extends ClassDataProvider> providers
        ) {
            this.providers.addAll(providers);
            return this;
        }

        /**
         * Clear the current set of core {@link ClassDataProvider providers}.
         *
         * @return {@code this} for chaining.
         */
        @CanIgnoreReturnValue
        @Contract(value = "-> this", mutates = "this")
        public @NotNull HypoContext.Builder clearProviders() {
            this.providers.clear();
            return this;
        }

        /**
         * Add the given {@link ClassDataProvider provider} as a context provider.
         *
         * @param provider The provider to add to the set of context providers.
         * @return {@code this} for chaining.
         */
        @CanIgnoreReturnValue
        @Contract(value = "_ -> this", mutates = "this")
        public @NotNull HypoContext.Builder withContextProvider(final @NotNull ClassDataProvider provider) {
            this.contextProviders.add(provider);
            return this;
        }

        /**
         * Add multiple {@link ClassDataProvider providers} as context providers.
         *
         * @param providers The providers to add to the set of context providers.
         * @return {@code this} for chaining.
         */
        @CanIgnoreReturnValue
        @Contract(value = "_ -> this", mutates = "this")
        public @NotNull HypoContext.Builder withContextProviders(
            final @NotNull ClassDataProvider @NotNull ... providers
        ) {
            this.contextProviders.addAll(Arrays.asList(providers));
            return this;
        }

        /**
         * Add a {@link Collection collection} of {@link ClassDataProvider provider} as context providers.
         *
         * @param providers The providers to add to the set of context providers.
         * @return {@code this} for chaining.
         */
        @CanIgnoreReturnValue
        @Contract(value = "_ -> this", mutates = "this")
        public @NotNull HypoContext.Builder withContextProviders(
            final @NotNull Collection<@NotNull ? extends ClassDataProvider> providers
        ) {
            this.contextProviders.addAll(providers);
            return this;
        }

        /**
         * Clear the current set of context {@link ClassDataProvider providers}.
         *
         * @return {@code this} for chaining.
         */
        @CanIgnoreReturnValue
        @Contract(value = "-> this", mutates = "this")
        public @NotNull HypoContext.Builder clearContextProviders() {
            this.contextProviders.clear();
            return this;
        }

        /**
         * Set the {@link HypoConfig config} for the context to be built.
         *
         * <p>This defaults to:
         *
         * <pre>
         *     HypoConfig.builder().build()
         * </pre>
         *
         * @param config The {@link HypoConfig config} to set.
         * @return {@code this} for chaining.
         */
        @CanIgnoreReturnValue
        @Contract(value = "_ -> this", mutates = "this")
        public @NotNull HypoContext.Builder withConfig(final @NotNull HypoConfig config) {
            this.config = config;
            return this;
        }

        /**
         * Use the current values of this builder to create a new instance of {@link HypoContext} and return it.
         *
         * @return The new instance of {@link HypoContext} using the values set in this builder.
         */
        @Contract(value = "-> new", pure = true)
        public @NotNull HypoContext build() {
            HypoConfig conf = this.config;
            if (conf == null) {
                conf = HypoConfig.builder().build();
            }

            for (final ClassDataProvider provider : this.providers) {
                provider.setContextClassProvider(false);
            }
            for (final ClassDataProvider provider : this.contextProviders) {
                provider.setContextClassProvider(true);
            }

            final ArrayList<ClassDataProvider> provs = new ArrayList<>(this.providers);
            provs.addAll(this.contextProviders);

            final ClassDataProviderSet allProvider = ClassDataProviderSet.wrap(provs);
            final ClassDataDecorator decorator = conf.getDecorator().apply(allProvider);
            allProvider.setDecorator(decorator);

            final ClassDataProviderSet targetProvider = ClassDataProviderSet.wrap(this.providers);
            targetProvider.setDecorator(decorator);

            return new HypoContext(conf, targetProvider, allProvider);
        }
    }
}
