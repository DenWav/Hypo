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

package dev.denwav.hypo.hydrate;

import dev.denwav.hypo.core.HypoContext;
import dev.denwav.hypo.model.HypoModelUtil;
import dev.denwav.hypo.model.data.ClassData;
import dev.denwav.hypo.model.data.FieldData;
import dev.denwav.hypo.model.data.HypoData;
import dev.denwav.hypo.model.data.MethodData;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Default implementation of {@link HydrationManager}.
 */
public class DefaultHydrationManager implements HydrationManager {

    private final @NotNull ClassDataHydrator baseHydrator;

    private final @NotNull List<HydrationProvider<?>> classProviders = new ArrayList<>();
    private final @NotNull List<HydrationProvider<?>> methodProviders = new ArrayList<>();
    private final @NotNull List<HydrationProvider<?>> fieldProviders = new ArrayList<>();

    /**
     * Constructs a new instance of {@link DefaultHydrationManager} using the default implementation of
     * {@link ClassDataHydrator}.
     *
     * @see DefaultHydrationManager#DefaultHydrationManager(ClassDataHydrator)
     */
    public DefaultHydrationManager() {
        this(ClassDataHydrator.createDefault());
    }

    /**
     * Constructs a new instance of {@link DefaultHydrationManager} using the given
     * {@link ClassDataHydrator baseHydrator}.
     *
     * @param baseHydrator The {@link ClassDataHydrator} to use as the {@code baseHydrator}.
     * @see DefaultHydrationManager#DefaultHydrationManager()
     */
    public DefaultHydrationManager(final @NotNull ClassDataHydrator baseHydrator) {
        this.baseHydrator = baseHydrator;
    }

    @Override
    @Contract("_ -> this")
    public @NotNull HydrationManager register(final @NotNull HydrationProvider<? extends HypoData> provider) {
        final Class<? extends HypoData> targetClass = provider.target();
        if (ClassData.class.isAssignableFrom(targetClass)) {
            this.classProviders.add(provider);
        } else if (MethodData.class.isAssignableFrom(targetClass)) {
            this.methodProviders.add(provider);
        } else if (FieldData.class.isAssignableFrom(targetClass)) {
            this.fieldProviders.add(provider);
        } else {
            throw new IllegalArgumentException("Given HydrationProvider (" + provider +
                ") targets an invalid type: " + targetClass);
        }
        return this;
    }

    @Override
    public void hydrate(@NotNull HypoContext context) throws IOException {
        final ExecutorService executor = context.getExecutor();

        try {
            this.baseHydrator.hydrate(context);

            final ArrayList<Future<?>> futures = new ArrayList<>();

            for (final ClassData classData : context.getProvider().allClasses()) {
                futures.add(executor.submit((Callable<?>) () -> {
                    for (final HydrationProvider<?> provider : this.classProviders) {
                        if (classData.getClass().isAssignableFrom(provider.target())) {
                            provider.hydrate(HypoModelUtil.cast(classData), context);
                        }
                    }

                    for (final MethodData method : classData.methods()) {
                        for (final HydrationProvider<?> provider : this.methodProviders) {
                            if (method.getClass().isAssignableFrom(provider.target())) {
                                provider.hydrate(HypoModelUtil.cast(method), context);
                            }
                        }
                    }

                    for (final FieldData field : classData.fields()) {
                        for (final HydrationProvider<?> provider : this.fieldProviders) {
                            if (field.getClass().isAssignableFrom(provider.target())) {
                                provider.hydrate(HypoModelUtil.cast(field), context);
                            }
                        }
                    }

                    return null;
                }));
            }

            for (final Future<?> future : futures) {
                future.get();
            }
        } catch (final ExecutionException | InterruptedException e) {
            HypoModelUtil.rethrow(e);
        }
    }
}
