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

package dev.denwav.hypo.mappings.changes;

import dev.denwav.hypo.mappings.LorenzUtil;
import dev.denwav.hypo.mappings.MappingsChange;
import dev.denwav.hypo.mappings.MergeResult;
import dev.denwav.hypo.mappings.MergeableMappingsChange;
import org.cadixdev.bombe.type.signature.FieldSignature;
import org.cadixdev.lorenz.MappingSet;
import org.cadixdev.lorenz.model.ClassMapping;
import org.cadixdev.lorenz.model.FieldMapping;
import org.cadixdev.lorenz.model.MethodMapping;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import static dev.denwav.hypo.mappings.LorenzUtil.getClassMapping;
import static dev.denwav.hypo.mappings.LorenzUtil.getFieldMapping;
import static dev.denwav.hypo.mappings.LorenzUtil.getMethodMapping;

/**
 * {@link MappingsChange MappingsChange} which removes a member mapping.
 */
public class RemoveMappingChange
    extends AbstractMappingsChange
    implements MergeableMappingsChange<RemoveMappingChange> {

    private RemoveMappingChange(final @NotNull MemberReference target) {
        super(target);
    }

    /**
     * Create a new instance of {@link RemoveMappingChange}.
     *
     * @param target The {@link MemberReference} of the mapping to remove.
     * @return A new instance of {@link RemoveMappingChange}.
     */
    @Contract(value = "_ -> new", pure = true)
    public static @NotNull RemoveMappingChange of(final @NotNull MemberReference target) {
        return new RemoveMappingChange(target);
    }

    @Override
    public void applyChange(final @NotNull MappingSet input, final @NotNull MemberReference target) {
        final ClassMapping<?, ?> map = getClassMapping(input, target.className());
        if (map == null) {
            return;
        }

        final String desc = target.desc();
        if (desc != null && desc.startsWith("(")) {
            final MethodMapping methodMap = getMethodMapping(map, target.name(), desc);
            if (methodMap != null) {
                LorenzUtil.removeMethodMapping(methodMap);
            }
        } else {
            final FieldMapping fieldMap;
            if (desc == null) {
                fieldMap = getFieldMapping(map, target.name());
            } else {
                fieldMap = getFieldMapping(map, FieldSignature.of(target.name(), desc));
            }
            if (fieldMap != null) {
                LorenzUtil.removeFieldMapping(fieldMap);
            }
        }

        if (!map.hasMappings()) {
            LorenzUtil.removeClassMapping(map);
        }
    }

    @Override
    public @NotNull MergeResult<RemoveMappingChange> mergeWith(final @NotNull RemoveMappingChange that) {
        return MergeResult.success(this);
    }

    @Override
    public String toString() {
        return "Remove " + this.target();
    }
}
