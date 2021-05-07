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

package com.demonwav.hypo.mappings;

import com.demonwav.hypo.core.HypoContext;
import com.demonwav.hypo.core.HypoException;
import com.demonwav.hypo.mappings.contributors.ChangeContributor;
import com.demonwav.hypo.mappings.contributors.ChangeContributorSet;
import com.demonwav.hypo.model.data.ClassData;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import org.cadixdev.lorenz.MappingSet;
import org.cadixdev.lorenz.model.ClassMapping;
import org.cadixdev.lorenz.model.InnerClassMapping;
import org.cadixdev.lorenz.model.TopLevelClassMapping;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Manager for the mappings completion process. Mappings completion is the combination of bytecode analysis with
 * {@code hypo-model} and {@code hypo-hydrate} with mappings analysis using
 * {@link ChangeContributor change contributors}.
 *
 * <p>Classes which implement {@link ChangeContributor} run on {@link ClassData} in a {@link HypoContext} along with the
 * current {@link MappingSet} and submit changes to make to the current {@link MappingSet} via the
 * {@link ChangeRegistry}. This class, the manager, handles the orchestration of each of these steps. This class does
 * assume the {@link HypoContext} has already been hydrated before it's passed in to the
 * {@link #create(HypoContext) create()} method.
 *
 * <p>Due to the limitations {@link ChangeRegistry} has in terms of dis-allowing multiple changes from targeting the
 * same class or member mapping, {@link ChangeChain} can be used to chain together sets of changes to be run serially
 * rather than in parallel. It's generally recommended to only run compatible
 * {@link ChangeContributor change contributors} in parallel - compatible meaning they won't submit changes against the
 * same target.
 *
 * @see ChangeRegistry
 * @see ChangeChain
 */
public class MappingsCompletionManager {

    private final @NotNull HypoContext context;

    private MappingsCompletionManager(final @NotNull HypoContext context) {
        this.context = context;
    }

    /**
     * Create a new instance of {@link MappingsCompletionManager} for the given {@link HypoContext}.
     *
     * <p>The given {@link HypoContext} is expected to have already gone through hydration.
     *
     * @param context The {@link HypoContext} to use for mappings completion.
     * @return The new {@link MappingsCompletionManager}.
     */
    @Contract(value = "_ -> new", pure = true)
    public static @NotNull MappingsCompletionManager create(final @NotNull HypoContext context) {
        return new MappingsCompletionManager(context);
    }

    /**
     * Run completion over the given {@link MappingSet} using the given list of
     * {@link ChangeContributor change contributors}. This will return a {@link ChangeRegistry} which has all of the
     * changes submitted from the given {@link ChangeContributor change contributors} ready to apply with
     * {@link ChangeRegistry#applyChanges(MappingSet)}.
     *
     * @param mappings The {@link MappingSet} to complete.
     * @param contributors The {@link ChangeContributor change contributors} to run against the mappings.
     * @return A {@link ChangeRegistry} filled in with the changes submitted by the given contributors.
     * @throws HypoException If one of the {@link ChangeContributor change contributors} fails.
     */
    public @NotNull ChangeRegistry completeMappings(
        final @NotNull MappingSet mappings,
        final @NotNull List<@NotNull ChangeContributor> contributors
    ) throws HypoException {
        final ChangeContributor contrib = ChangeContributorSet.wrap(contributors);
        final ChangeRegistry registry = new ChangeRegistry();

        final HashSet<String> visitedClasses = new HashSet<>();

        registry.setCurrentContributorName(contrib.name());

        final ExecutorService executor = this.context.getExecutor();
        final ArrayList<Future<?>> futures = new ArrayList<>();

        for (final TopLevelClassMapping mapping : mappings.getTopLevelClassMappings()) {
            this.complete(mapping, contrib, visitedClasses, executor, futures, registry);
        }

        for (final ClassData classData : this.context.getProvider().allClasses()) {
            if (visitedClasses.contains(classData.name())) {
                continue;
            }

            futures.add(executor.submit((Callable<?>) () -> {
                try {
                    contrib.contribute(classData, null, this.context, registry);
                } catch (final Throwable e) {
                    throw new HypoException("Error while contributing mappings changes with '" + contrib.name()
                        + "' for: " + classData.name(), e);
                }

                return null;
            }));
        }

        try {
            for (final Future<?> future : futures) {
                future.get();
            }
        } catch (final ExecutionException | InterruptedException e) {
            throw new HypoException("Mappings completion execution failed", e);
        }

        return registry;
    }

    private void complete(
        final @NotNull ClassMapping<?, ?> mappings,
        final @NotNull ChangeContributor contributor,
        final @NotNull HashSet<String> visitedClasses,
        final @NotNull ExecutorService executor,
        final @NotNull ArrayList<Future<?>> futures,
        final @NotNull ChangeRegistry registry
    ) {
        final String className = mappings.getFullObfuscatedName();
        visitedClasses.add(className);

        futures.add(executor.submit((Callable<?>) () -> {
            final ClassData classData;
            try {
                classData = this.context.getProvider().findClass(className);
            } catch (final IOException e) {
                throw new HypoException("Error while attempting to find class: " + className, e);
            }

            try {
                contributor.contribute(classData, mappings, this.context, registry);
            } catch (final Throwable e) {
                throw new HypoException("Error while contributing mappings changes with '" + contributor.name()
                    + "' for: " + className, e);
            }

            return null;
        }));

        for (final InnerClassMapping innerMapping : mappings.getInnerClassMappings()) {
            this.complete(innerMapping, contributor, visitedClasses, executor, futures, registry);
        }
    }
}
