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
import dev.denwav.hypo.hydrate.generic.SuperCall;
import dev.denwav.hypo.mappings.ChangeRegistry;
import dev.denwav.hypo.mappings.changes.CopyConstructorMappingChange;
import dev.denwav.hypo.mappings.changes.CopyMethodMappingChange;
import dev.denwav.hypo.mappings.changes.MemberReference;
import dev.denwav.hypo.model.data.ClassData;
import dev.denwav.hypo.model.data.ConstructorData;
import dev.denwav.hypo.model.data.MethodData;
import java.util.List;
import org.cadixdev.lorenz.MappingSet;
import org.cadixdev.lorenz.model.ClassMapping;
import org.cadixdev.lorenz.model.MethodMapping;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static dev.denwav.hypo.mappings.LorenzUtil.getClassMapping;
import static dev.denwav.hypo.mappings.LorenzUtil.getMethodMapping;

/**
 * Implementation of {@link ChangeContributor} which copies {@link MethodMapping method mappings} down the method
 * hierarchy. This recursively walks the children of methods with mappings and copies those mappings onto the children
 * as well. This also takes synthetic target methods into account using {@link HypoHydration#SYNTHETIC_TARGET}.
 */
public class CopyMappingsDown implements ChangeContributor {

    private final boolean stopOnChildMapping;

    private CopyMappingsDown(final boolean stopOnChildMapping) {
        this.stopOnChildMapping = stopOnChildMapping;
    }

    /**
     * Create a new instance of {@link CopyMappingsDown}. This instance will overwrite mappings any child method has
     * with mappings provided by the parent method.
     * @return A new instance of {@link CopyMappingsDown}.
     */
    @Contract(value = "-> new", pure = true)
    public static @NotNull CopyMappingsDown create() {
        return new CopyMappingsDown(false);
    }

    /**
     * Create a new instance of {@link CopyMappingsDown}. This instance will not overwrite mappings on any child method.
     * If a child method already contains mappings, that method will become the new "root" and the mappings in that
     * child method will be propagated to its children instead.
     * @return A new instance of {@link CopyMappingsDown}.
     */
    @Contract(value = "-> new", pure = true)
    public static @NotNull CopyMappingsDown createWithoutOverwrite() {
        return new CopyMappingsDown(true);
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

        for (final MethodData method : currentClass.methods()) {
            final MethodMapping methodMapping = getMethodMapping(classMapping, method.name(), method.descriptorText());
            if (methodMapping == null) {
                continue;
            }

            if (!method.isConstructor()) {
                this.walkDown(method, methodMapping, registry);
            } else {
                walkConstructor(method, methodMapping, registry, null);
            }
        }
    }

    private void walkDown(
        final @NotNull MethodData method,
        final @NotNull MethodMapping mapping,
        final @NotNull ChangeRegistry registry
    ) {
        for (final MethodData childMethod : method.childMethods()) {
            this.setChangeAndWalkDown(childMethod, mapping, registry);
        }

        final MethodData syntheticTarget = method.get(HypoHydration.SYNTHETIC_TARGET);
        if (syntheticTarget != null) {
            this.setChangeAndWalkDown(syntheticTarget, mapping, registry);
        }
    }

    private void setChangeAndWalkDown(
        final @NotNull MethodData method,
        final @NotNull MethodMapping mapping,
        final @NotNull ChangeRegistry registry
    ) {
        if (this.stopOnChildMapping) {
            final ClassMapping<?, ?> classMapping = getClassMapping(mapping.getMappings(), method.parentClass().name());
            final MethodMapping methodMapping = getMethodMapping(classMapping, method.name(), method.descriptorText());
            if (methodMapping != null) {
                return;
            }
        }
        registry.submitChange(CopyMethodMappingChange.of(MemberReference.of(method), mapping));

        this.walkDown(method, mapping, registry);
    }

    private static void walkConstructor(
        final @NotNull MethodData method,
        final @NotNull MethodMapping mapping,
        final @NotNull ChangeRegistry registry,
        final @Nullable SuperCall parentSuperCall
    ) {
        final List<SuperCall> superCalls = method.get(HypoHydration.SUPER_CALLER_SOURCES);
        if (superCalls == null || superCalls.isEmpty()) {
            return;
        }

        final MappingSet mappings = mapping.getMappings();

        for (final SuperCall superCall : superCalls) {
            // we are the super constructor, find the `this` constructor
            final ConstructorData childConst = superCall.getThisConstructor();
            final ClassMapping<?, ?> childClassMapping = getClassMapping(mappings, childConst.parentClass().name());

            if (childClassMapping != null) {
                final MethodMapping childMapping = getMethodMapping(childClassMapping, childConst.name(), childConst.descriptorText());
                if (childMapping != null) {
                    boolean hasParamMappings = false;
                    final int len = method.params().size();
                    for (int i = 0; i < len; i++) {
                        if (childMapping.hasParameterMapping(i)) {
                            hasParamMappings = true;
                            break;
                        }
                    }
                    if (hasParamMappings) {
                        // This constructor has its own mappings, so don't copy ours
                        continue;
                    }
                }
            }

            final MemberReference thisReference = MemberReference.of(childConst);
            final CopyConstructorMappingChange change = CopyConstructorMappingChange.of(thisReference, mapping);
            registry.submitChange(change);

            final SuperCall thisSuperCall;
            if (parentSuperCall == null) {
                thisSuperCall = superCall;
            } else {
                thisSuperCall = parentSuperCall.chain(superCall);
            }

            change.addParams(thisSuperCall.getParams());

            walkConstructor(childConst, mapping, registry, thisSuperCall);
        }
    }

    @Override
    public @NotNull String name() {
        return "CopyMappingsDown";
    }
}
