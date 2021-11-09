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

package dev.denwav.hypo.mappings.contributors;

import dev.denwav.hypo.core.HypoContext;
import dev.denwav.hypo.mappings.ChangeRegistry;
import dev.denwav.hypo.mappings.ClassMappingsChange;
import dev.denwav.hypo.mappings.MappingsChange;
import dev.denwav.hypo.model.data.ClassData;
import org.cadixdev.lorenz.model.ClassMapping;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * API for defining changes to a {@link org.cadixdev.lorenz.MappingSet MappingSet} based on a hydrated
 * {@link HypoContext}.
 *
 * <p>All change contributors must have a {@link #name() name}, which may simply be the class name. This name is for
 * error reporting and debugging purposes only.
 */
public interface ChangeContributor {

    /**
     * Submit {@link MappingsChange mappings changes} and
     * {@link ClassMappingsChange class mappings changes} to the {@link ChangeRegistry} based
     * on analysis of the given {@link ClassData} and {@link ClassMapping}.
     *
     * <p>If {@code currentClass} is {@code null} that means the {@link ClassMapping} exists without a corresponding
     * {@link ClassData} in the {@link HypoContext}.
     *
     * <p>If {@code classMapping} is {@code null} that means the {@link ClassData} exists in the {@link HypoContext}
     * without a corresponding {@link ClassMapping} in the mapping set.
     *
     * <p>Either {@code currentClass} or {@code classMapping} may be {@code null}, but they cannot both be {@code null}
     * at the same time. They will usually both be present.
     *
     * <p>This contributor may submit any number of mappings changes to the {@link ChangeRegistry}, but generally it
     * should only submit changes relevant to the current {@link ClassData} and/or {@link ClassMapping} being processed.
     *
     * @param currentClass The {@link ClassData} for this mapping, may be {@code null} if the mapping exists without a
     *                     corresponding class in the context.
     * @param classMapping The {@link ClassMapping} for this class data, may be {@code null} if the class exists without
     *                     a corresponding mapping in the mapping set.
     * @param context The {@link HypoContext} for this execution.
     * @param registry The {@link ChangeRegistry} to submit mappings changes to.
     * @throws Throwable If there is an error during method execution.
     */
    void contribute(
        final @Nullable ClassData currentClass,
        final @Nullable ClassMapping<?, ?> classMapping,
        final @NotNull HypoContext context,
        final @NotNull ChangeRegistry registry
    ) throws Throwable;

    /**
     * The name of this change contributor. By contention the name of a contributor is typically its class name, but
     * that is not a requirement. If the contributor has config values set on it which changes its behavior it provably
     * makes sense to include them in the name as well somehow.
     *
     * <p>Change contributor names are used for error reporting and debugging purposes, so in any case the name should
     * be clear and easy to understand which contributor it's referring to.
     *
     * @return The name of this change contributor.
     */
    @NotNull String name();
}
