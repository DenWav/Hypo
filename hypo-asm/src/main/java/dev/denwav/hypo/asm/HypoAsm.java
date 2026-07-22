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

package dev.denwav.hypo.asm;

import dev.denwav.hypo.core.HypoConfig;
import dev.denwav.hypo.core.HypoContext;
import dev.denwav.hypo.model.ClassDataProvider;
import dev.denwav.hypo.model.ClassProviderRoot;
import dev.denwav.hypo.model.HypoModelUtil;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Helper entry-points for setting up and using a {@link HypoContext} using {@code hypo-asm} as the core implementation.
 */
public final class HypoAsm {

    private HypoAsm() {}

    /**
     * Helper for creating a {@link HypoContext} for a single jar file. The classpath of the currently running JVM will
     * be added as a {@link HypoContext.Builder#withContextProvider(ClassDataProvider)} context provider.
     *
     * @param jar The jar file to use for the {@link HypoContext}.
     * @return The new {@link HypoContext}.
     * @throws UncheckedIOException If an IO error occurs.
     */
    public static HypoContext context(final @NotNull Path jar) throws UncheckedIOException {
        return HypoAsm.context(List.of(jar), true, null);
    }

    /**
     * Helper for creating a {@link HypoContext} for a collection of jar files. The classpath of the currently running JVM will
     * be added as a {@link HypoContext.Builder#withContextProvider(ClassDataProvider)} context provider.
     *
     * @param jars The collection of jar files to use for the {@link HypoContext}.
     * @return The new {@link HypoContext}.
     * @throws UncheckedIOException If an IO error occurs.
     */
    public static HypoContext context(final @NotNull Collection<? extends Path> jars) throws UncheckedIOException {
        return HypoAsm.context(jars, true, null);
    }

    /**
     * Helper for creating a {@link HypoContext} for a single jar file. {@link HypoConfig} is optional. Providing {@code false} for {@code withJvm}
     * prevents the classpath of the currently running JVM from being added as a
     * {@link HypoContext.Builder#withContextProvider(ClassDataProvider)} context provider.
     *
     * @param jar The jar file to use for the {@link HypoContext}.
     * @param withJvm If {@code true}, include the classpath of the currently running JVM as a context provider.
     * @param config Optional, the {@link HypoConfig} for this context.
     * @return The new {@link HypoContext}.
     * @throws UncheckedIOException If an IO error occurs.
     */
    public static HypoContext context(
        final @NotNull Path jar,
        final boolean withJvm,
        final @Nullable HypoConfig config
    ) throws UncheckedIOException {
        try {
            final HypoContext.Builder ctx = HypoContext.builder().withProvider(AsmClassDataProvider.of(ClassProviderRoot.fromJar(jar)));
            if (config != null) {
                ctx.withConfig(config);
            }
            if (withJvm) {
                ctx.withContextProvider(AsmClassDataProvider.of(ClassProviderRoot.ofJdk()));
            }
            return ctx.build();
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Helper for creating a {@link HypoContext} for a collection of jar files. {@link HypoConfig} is optional. Providing {@code false} for
     * {@code withJvm} prevents the classpath of the currently running JVM from being added as a
     * {@link HypoContext.Builder#withContextProvider(ClassDataProvider)} context provider.
     *
     * @param jars The collection of jar files to use for the {@link HypoContext}.
     * @param withJvm If {@code true}, include the classpath of the currently running JVM as a context provider.
     * @param config Optional, the {@link HypoConfig} for this context.
     * @return The new {@link HypoContext}.
     * @throws UncheckedIOException If an IO error occurs.
     */
    public static HypoContext context(
        final @NotNull Collection<? extends Path> jars,
        final boolean withJvm,
        final @Nullable HypoConfig config
    ) throws UncheckedIOException {
        return switch (jars.size()) {
            case 0 -> throw new IllegalArgumentException("");
            case 1 -> HypoAsm.context(jars.iterator().next(), withJvm, config);
            default -> {
                try {
                    final HypoContext.Builder ctx = HypoContext.builder();
                    for (final Path jar : jars) {
                        ctx.withProvider(AsmClassDataProvider.of(ClassProviderRoot.fromJar(jar)));
                    }
                    if (config != null) {
                        ctx.withConfig(config);
                    }
                    if (withJvm) {
                        ctx.withContextProvider(AsmClassDataProvider.of(ClassProviderRoot.ofJdk()));
                    }
                    yield ctx.build();
                } catch (final IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        };
    }

    /**
     * Execute the given {@code consumer} using a {@link HypoContext} for the given jar file. The context is safely {@link HypoContext#close() closed}
     * upon completion of the {@code consumer}. The classpath of the currently running JVM will be added as a
     * {@link HypoContext.Builder#withContextProvider(ClassDataProvider)} context provider.
     *
     * @param jar The jar file to use for the {@link HypoContext}.
     * @param consumer The consumer to execute with the {@link HypoContext}.
     * @param <X> The exception the consumer may throw.
     * @throws UncheckedIOException If an IO error occurs.
     * @throws X If the consumer throws {@code X} exception.
     */
    public static <X extends Throwable> void run(
        final @NotNull Path jar,
        final HypoModelUtil.IoThrowingConsumer<HypoContext, X> consumer
    ) throws UncheckedIOException, X {
        try (final HypoContext context = HypoAsm.context(jar)) {
            consumer.acceptThrowing(context);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Execute the given {@code consumer} using a {@link HypoContext} for the given jar file. The context is safely {@link HypoContext#close() closed}
     * upon completion of the {@code consumer}. Providing {@code false} for {@code withJvm} prevents the classpath of the currently running JVM from
     * being added as a {@link HypoContext.Builder#withContextProvider(ClassDataProvider)} context provider.
     *
     * @param jar The jar file to use for the {@link HypoContext}.
     * @param withJvm If {@code true}, include the classpath of the currently running JVM as a context provider.
     * @param config Optional, the {@link HypoConfig} for this context.
     * @param consumer The consumer to execute with the {@link HypoContext}.
     * @param <X> The exception the consumer may throw.
     * @throws UncheckedIOException If an IO error occurs.
     * @throws X If the consumer throws {@code X} exception.
     */
    public static <X extends Throwable> void run(
        final @NotNull Path jar,
        final boolean withJvm,
        final @Nullable HypoConfig config,
        final HypoModelUtil.IoThrowingConsumer<HypoContext, X> consumer
    ) throws UncheckedIOException, X {
        try (final HypoContext context = HypoAsm.context(jar, withJvm, config)) {
            consumer.acceptThrowing(context);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Execute the given {@code consumer} using a {@link HypoContext} for a collection of jar files. The context is safely {@link HypoContext#close() closed}
     * upon completion of the {@code consumer}. Providing {@code false} for {@code withJvm} prevents the classpath of the currently running JVM from
     * being added as a {@link HypoContext.Builder#withContextProvider(ClassDataProvider)} context provider.
     *
     * @param jars The collection of jar files to use for the {@link HypoContext}.
     * @param withJvm If {@code true}, include the classpath of the currently running JVM as a context provider.
     * @param config Optional, the {@link HypoConfig} for this context.
     * @param consumer The consumer to execute with the {@link HypoContext}.
     * @param <X> The exception the consumer may throw.
     * @throws UncheckedIOException If an IO error occurs.
     * @throws X If the consumer throws {@code X} exception.
     */
    public static <X extends Throwable> void run(
        final @NotNull Collection<? extends Path> jars,
        final boolean withJvm,
        final @Nullable HypoConfig config,
        final HypoModelUtil.IoThrowingConsumer<HypoContext, X> consumer
    ) throws UncheckedIOException, X {
        try (final HypoContext context = HypoAsm.context(jars, withJvm, config)) {
            consumer.acceptThrowing(context);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Execute the given {@code function} using a {@link HypoContext} for the given jar file, and return the result. The context is safely
     * {@link HypoContext#close() closed} upon completion of the {@code function}. The classpath of the currently running JVM will be added as a
     * {@link HypoContext.Builder#withContextProvider(ClassDataProvider)} context provider.
     *
     * @param jar The jar file to use for the {@link HypoContext}.
     * @param func The function to execute with the {@link HypoContext}.
     * @param <X> The exception the function may throw.
     * @return The result of {@code func}.
     * @throws UncheckedIOException If an IO error occurs.
     * @throws X If the function throws {@code X} exception.
     */
    public static <R, X extends Throwable> R use(
        final @NotNull Path jar,
        final HypoModelUtil.IoThrowingFunction<HypoContext, R, X> func
    ) throws UncheckedIOException, X {
        try (final HypoContext context = HypoAsm.context(jar)) {
            return func.applyThrowing(context);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Execute the given {@code function} using a {@link HypoContext} for the given jar file, and return the result. The context is safely
     * {@link HypoContext#close() closed} upon completion of the {@code function}. Providing {@code false} for {@code withJvm} prevents the classpath
     * of the currently running JVM from being added as a {@link HypoContext.Builder#withContextProvider(ClassDataProvider)} context provider.
     *
     * @param jar The jar file to use for the {@link HypoContext}.
     * @param withJvm If {@code true}, include the classpath of the currently running JVM as a context provider.
     * @param config Optional, the {@link HypoConfig} for this context.
     * @param func The function to execute with the {@link HypoContext}.
     * @param <X> The exception the function may throw.
     * @return The result of {@code func}.
     * @throws UncheckedIOException If an IO error occurs.
     * @throws X If the function throws {@code X} exception.
     */
    public static <R, X extends Throwable> R use(
        final @NotNull Path jar,
        final boolean withJvm,
        final @Nullable HypoConfig config,
        final HypoModelUtil.IoThrowingFunction<HypoContext, R, X> func
    ) throws UncheckedIOException, X {
        try (final HypoContext context = HypoAsm.context(jar, withJvm, config)) {
            return func.applyThrowing(context);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Execute the given {@code function} using a {@link HypoContext} for a collection of jar files, and return the result. The context is safely
     * {@link HypoContext#close() closed} upon completion of the {@code function}. Providing {@code false} for {@code withJvm} prevents the classpath
     * of the currently running JVM from being added as a {@link HypoContext.Builder#withContextProvider(ClassDataProvider)} context provider.
     *
     * @param jars The collection of jar files to use for the {@link HypoContext}.
     * @param withJvm If {@code true}, include the classpath of the currently running JVM as a context provider.
     * @param config Optional, the {@link HypoConfig} for this context.
     * @param func The function to execute with the {@link HypoContext}.
     * @param <X> The exception the function may throw.
     * @return The result of {@code func}.
     * @throws UncheckedIOException If an IO error occurs.
     * @throws X If the function throws {@code X} exception.
     */
    public static <R, X extends Throwable> R use(
        final @NotNull Collection<? extends Path> jars,
        final boolean withJvm,
        final @Nullable HypoConfig config,
        final HypoModelUtil.IoThrowingFunction<HypoContext, R, X> func
    ) throws UncheckedIOException, X {
        try (final HypoContext context = HypoAsm.context(jars, withJvm, config)) {
            return func.applyThrowing(context);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
