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

package com.demonwav.hypo.mappings.contributors;

import com.demonwav.hypo.core.HypoContext;
import com.demonwav.hypo.mappings.ChangeRegistry;
import com.demonwav.hypo.mappings.changes.MemberReference;
import com.demonwav.hypo.mappings.changes.RemoveClassMappingChange;
import com.demonwav.hypo.mappings.changes.RemoveMappingChange;
import com.demonwav.hypo.mappings.changes.RemoveParameterMappingChange;
import com.demonwav.hypo.model.data.ClassData;
import com.demonwav.hypo.model.data.MethodData;
import org.cadixdev.lorenz.model.ClassMapping;
import org.cadixdev.lorenz.model.FieldMapping;
import org.cadixdev.lorenz.model.MethodMapping;
import org.cadixdev.lorenz.model.MethodParameterMapping;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.demonwav.hypo.mappings.LorenzUtil.findField;
import static com.demonwav.hypo.mappings.LorenzUtil.findMethod;

/**
 * Removes any class, method, field, or method parameter mappings which target classes or members or parameters which
 * don't exist.
 */
public class RemoveUnusedMappings implements ChangeContributor {

    private RemoveUnusedMappings() {}

    /**
     * Create a new instance of {@link RemoveUnusedMappings}.
     * @return A new instance of {@link RemoveUnusedMappings}.
     */
    @Contract(value = "-> new", pure = true)
    public static @NotNull RemoveUnusedMappings create() {
        return new RemoveUnusedMappings();
    }

    @Override
    public void contribute(
        final @Nullable ClassData currentClass,
        final @Nullable ClassMapping<?, ?> classMapping,
        final @NotNull HypoContext context,
        final @NotNull ChangeRegistry registry
    ) {
        if (classMapping == null) {
            return;
        }
        if (currentClass == null) {
            registry.submitChange(RemoveClassMappingChange.of(classMapping.getFullObfuscatedName()));
            return;
        }

        for (final MethodMapping methodMapping : classMapping.getMethodMappings()) {
            final MethodData method = findMethod(currentClass, methodMapping);
            if (method == null) {
                registry.submitChange(RemoveMappingChange.of(MemberReference.of(methodMapping)));
                continue;
            }

            if (method.isConstructor() && methodMapping.getParameterMappings().isEmpty()) {
                // Constructor mappings without parameters are useless
                registry.submitChange(RemoveMappingChange.of(MemberReference.of(methodMapping)));
                continue;
            }

            MemberReference methodRef = null;
            for (final MethodParameterMapping paramMapping : methodMapping.getParameterMappings()) {
                if (method.paramLvt(paramMapping.getIndex()) == null) {
                    if (methodRef == null) {
                        methodRef = MemberReference.of(method);
                    }
                    registry.submitChange(RemoveParameterMappingChange.of(methodRef, paramMapping.getIndex()));
                }
            }
        }

        for (final FieldMapping fieldMapping : classMapping.getFieldMappings()) {
            if (findField(currentClass, fieldMapping).isEmpty()) {
                registry.submitChange(RemoveMappingChange.of(MemberReference.of(fieldMapping)));
            }
        }
    }

    @Override
    public @NotNull String name() {
        return "RemoveUnusedMappings";
    }
}
