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
import dev.denwav.hypo.mappings.ChangeRegistry;
import dev.denwav.hypo.mappings.changes.AddNewParameterMappingsChange;
import dev.denwav.hypo.mappings.changes.MemberReference;
import dev.denwav.hypo.model.data.ClassData;
import dev.denwav.hypo.model.data.ClassKind;
import dev.denwav.hypo.model.data.FieldData;
import dev.denwav.hypo.model.data.MethodData;
import dev.denwav.hypo.types.PrimitiveType;
import dev.denwav.hypo.types.desc.TypeDescriptor;
import java.util.ArrayList;
import java.util.List;
import org.cadixdev.lorenz.model.ClassMapping;
import org.cadixdev.lorenz.model.FieldMapping;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static dev.denwav.hypo.mappings.LorenzUtil.getFieldMapping;

/**
 * Implementation of {@link ChangeContributor} which copies record component names down to any canonical constructors
 * present, using the record component names as the parameter names for the canonical constructor. If the component
 * fields have existing field mappings, the deobfuscated names will be used for the parameter names instead of the
 * current names.
 */
public final class CopyRecordParameters implements ChangeContributor {

    private CopyRecordParameters() {
    }

    /**
     * Create a new instance of {@link CopyRecordParameters}.
     *
     * @return A new instance of {@link CopyRecordParameters}.
     */
    @Contract(value = "-> new", pure = true)
    public static @NotNull CopyRecordParameters create() {
        return new CopyRecordParameters();
    }

    @Override
    public void contribute(
        final @Nullable ClassData currentClass,
        final @Nullable ClassMapping<?, ?> classMapping,
        final @NotNull HypoContext context,
        final @NotNull ChangeRegistry registry
    ) {
        if (currentClass == null) {
            return;
        }

        if (currentClass.isNot(ClassKind.RECORD)) {
            return;
        }

        final List<@NotNull FieldData> components = currentClass.recordComponents();
        if (components == null) {
            return;
        }

        final ArrayList<@Nullable FieldMapping> componentMappings = new ArrayList<>();
        for (final FieldData component : components) {
            final FieldMapping fieldMapping = getFieldMapping(classMapping, component.name());
            componentMappings.add(fieldMapping);
        }

        outer:
        for (final MethodData method : currentClass.methods()) {
            if (!method.isConstructor()) {
                continue;
            }

            // canonical constructor must be the same as the record components
            final int len = method.params().size();
            if (len != components.size()) {
                continue;
            }

            // must have the same types
            for (int i = 0; i < len; i++) {
                if (!method.param(i).equals(components.get(i).descriptor())) {
                    continue outer;
                }
            }

            // start at 1 because all records are static, so there will never be a `this$0` reference
            // there will always be a `this` reference, though
            int lvtIndex = 1;

            // constructor matches
            for (int i = 0; i < method.params().size(); i++) {
                final FieldMapping fieldMapping = componentMappings.get(i);
                final String newName;
                if (fieldMapping != null) {
                    newName = fieldMapping.getDeobfuscatedName();
                } else {
                    newName = components.get(i).name();
                }

                final MemberReference ref = MemberReference.of(method, lvtIndex);
                registry.submitChange(AddNewParameterMappingsChange.of(ref, newName));

                lvtIndex++;
                final TypeDescriptor paramType = method.param(i);
                if (paramType == PrimitiveType.LONG || paramType == PrimitiveType.DOUBLE) {
                    lvtIndex++;
                }
            }
        }
    }

    @Override
    public @NotNull String name() {
        return "CopyRecordParameters";
    }
}
