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

package dev.denwav.hypo.mappings.contributors;

import dev.denwav.hypo.core.HypoContext;
import dev.denwav.hypo.hydrate.generic.HypoHydration;
import dev.denwav.hypo.mappings.ChangeRegistry;
import dev.denwav.hypo.mappings.changes.CopyMethodMappingChange;
import dev.denwav.hypo.mappings.changes.MemberReference;
import dev.denwav.hypo.mappings.changes.RemoveMappingChange;
import dev.denwav.hypo.model.data.ClassData;
import dev.denwav.hypo.model.data.MethodData;
import java.util.List;
import org.cadixdev.lorenz.model.ClassMapping;
import org.cadixdev.lorenz.model.MethodMapping;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static dev.denwav.hypo.mappings.LorenzUtil.findMethod;
import static dev.denwav.hypo.mappings.LorenzUtil.getClassMapping;
import static dev.denwav.hypo.mappings.LorenzUtil.getMethodMapping;

/**
 * Removes duplicate mappings for child methods and ensures that each method at the top of the method hierarchy is the
 * only method with mappings. This takes synthetic methods into account as well with
 * {@link HypoHydration#SYNTHETIC_SOURCE}.
 */
public class PropagateMappingsUp implements ChangeContributor {

    private PropagateMappingsUp() {}

    /**
     * Create a new instance of {@link PropagateMappingsUp}.
     * @return A new instance of {@link PropagateMappingsUp}.
     */
    @Contract(value = "-> new", pure = true)
    public static @NotNull PropagateMappingsUp create() {
        return new PropagateMappingsUp();
    }

    @Override
    public void contribute(
        final @Nullable ClassData currentClass,
        final @Nullable ClassMapping<?, ?> classMapping,
        final @NotNull HypoContext context,
        final @NotNull ChangeRegistry registry
    ) {
        if (currentClass == null || classMapping == null) {
            return;
        }

        for (final MethodMapping methodMapping : classMapping.getMethodMappings()) {
            final MethodData method = findMethod(currentClass, methodMapping);
            if (method == null) {
                continue;
            }

            walkUp(method, methodMapping, registry);
        }
    }

    private static void walkUp(
        final @NotNull MethodData method,
        final @NotNull MethodMapping baseMapping,
        final @NotNull ChangeRegistry registry
    ) {
        boolean walkSuper = true;

        final ClassMapping<?, ?> classMapping = getClassMapping(baseMapping.getMappings(), method.parentClass().name());
        final MethodMapping methodMapping;
        if (classMapping != null) {
            methodMapping = getMethodMapping(classMapping, method.name(), method.descriptorText());
            if (methodMapping != null && methodMapping != baseMapping) {
                // There already exists a parent mapping
                remove(registry, baseMapping);
                walkSuper = false;
            }
        } else {
            methodMapping = null;
        }

        final MethodData superMethod = method.superMethod();
        final List<MethodData> syntheticSources = method.get(HypoHydration.SYNTHETIC_SOURCE);
        final MethodData syntheticSource = syntheticSources != null && !syntheticSources.isEmpty() ? syntheticSources.get(0) : null;

        if (superMethod != null && !superMethod.parentClass().isContextClass() && walkSuper) {
            walkUp(superMethod, baseMapping, registry);
        } else if (methodMapping == null && syntheticSource == null) {
            // This method is the highest in the chain
            // It does not have an associated mapping, so we should add it
            registry.submitChange(CopyMethodMappingChange.of(MemberReference.of(method), baseMapping));
            remove(registry, baseMapping);
        } else if (superMethod != null && superMethod.parentClass().isContextClass() && syntheticSource == null) {
            // This is the highest we can go without dropping into the context provider
            // But this method already has a mapping, so we just need to remove the base
            remove(registry, baseMapping);
        }

        if (syntheticSource != null) {
            walkUp(syntheticSource, baseMapping, registry);
        }
    }

    private static void remove(final @NotNull ChangeRegistry registry, final @NotNull MethodMapping mapping) {
        registry.submitChange(RemoveMappingChange.of(MemberReference.of(mapping)));
    }

    @Override
    public @NotNull String name() {
        return "PropagateMappingsUp";
    }
}
