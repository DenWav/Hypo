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

package dev.denwav.hypo.hydrate;

import dev.denwav.hypo.core.HypoContext;
import dev.denwav.hypo.model.HypoModelUtil;
import dev.denwav.hypo.model.data.ClassData;
import dev.denwav.hypo.model.data.FieldData;
import dev.denwav.hypo.model.data.HypoData;
import dev.denwav.hypo.model.data.HypoKey;
import dev.denwav.hypo.model.data.MethodData;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

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
    public void hydrate(final @NotNull HypoContext context) throws IOException {
        try {
            this.baseHydrator.hydrate(context);

            final Graph<HydrationProvider<?>, DefaultEdge> providersGraph = this.createProviderGraph();

            HashSet<HydrationProvider<?>> stage;
            while (!(stage = this.getNextStage(providersGraph)).isEmpty()) {
                this.executeProviderStage(context, stage);
            }
        } catch (final ExecutionException | InterruptedException e) {
            HypoModelUtil.rethrow(e);
        }
    }

    private void executeProviderStage(
        final @NotNull HypoContext context,
        final @NotNull HashSet<HydrationProvider<?>> stage
    )throws ExecutionException, InterruptedException {
        final ExecutorService executor = context.getExecutor();
        ArrayList<Future<?>> futures = new ArrayList<>();

        for (final ClassData classData : context.getProvider().allClasses()) {
            futures.add(executor.submit((Callable<?>) () -> {
                if (containsAny(stage, this.classProviders)) {
                    for (final HydrationProvider<?> provider : this.classProviders) {
                        if (!stage.contains(provider)) {
                            continue;
                        }
                        if (provider.target().isInstance(classData)) {
                            provider.hydrate(HypoModelUtil.cast(classData), context);
                        }
                    }
                }

                if (containsAny(stage, this.methodProviders)) {
                    for (final MethodData method : classData.methods()) {
                        for (final HydrationProvider<?> provider : this.methodProviders) {
                            if (!stage.contains(provider)) {
                                continue;
                            }
                            if (provider.target().isInstance(method)) {
                                provider.hydrate(HypoModelUtil.cast(method), context);
                            }
                        }
                    }
                }

                if (containsAny(stage, this.fieldProviders)) {
                    for (final FieldData field : classData.fields()) {
                        for (final HydrationProvider<?> provider : this.fieldProviders) {
                            if (!stage.contains(provider)) {
                                continue;
                            }
                            if (provider.target().isInstance(field)) {
                                provider.hydrate(HypoModelUtil.cast(field), context);
                            }
                        }
                    }
                }

                return null;
            }));
        }

        for (final Future<?> future : futures) {
            future.get();
        }
    }

    private static  <T> boolean containsAny(final Set<T> set, final List<T> list) {
        for (final T t : list) {
            if (set.contains(t)) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("ReferenceEquality")
    private @NotNull Graph<HydrationProvider<?>, DefaultEdge> createProviderGraph() {
        final ArrayList<HydrationProvider<?>> allProviders =
            new ArrayList<>(this.classProviders.size() + this.methodProviders.size() + this.fieldProviders.size());
        allProviders.addAll(this.classProviders);
        allProviders.addAll(this.methodProviders);
        allProviders.addAll(this.fieldProviders);

        final Graph<HydrationProvider<?>, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);

        for (final HydrationProvider<?> currentProvider : allProviders) {
            g.addVertex(currentProvider);

            for (final HypoKey<?> dependentKey : currentProvider.dependsOn()) {
                for (final HydrationProvider<?> providingProvider : allProviders) {
                    for (final HypoKey<?> providedKey : providingProvider.provides()) {
                        if (dependentKey == providedKey) {
                            g.addVertex(providingProvider);
                            g.addEdge(providingProvider, currentProvider);
                        }
                    }
                }
            }
        }

        return g;
    }

    private @NotNull HashSet<HydrationProvider<?>> getNextStage(final @NotNull Graph<HydrationProvider<?>, ?> graph) {
        final LinkedHashSet<HydrationProvider<?>> stage = new LinkedHashSet<>();
        for (final HydrationProvider<?> provider : graph.vertexSet()) {
            if (graph.inDegreeOf(provider) == 0) {
                stage.add(provider);
            }
        }
        graph.removeAllVertices(stage);
        return stage;
    }
}
