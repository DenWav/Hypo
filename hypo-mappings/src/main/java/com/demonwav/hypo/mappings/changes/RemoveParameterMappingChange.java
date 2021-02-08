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

package com.demonwav.hypo.mappings.changes;

import com.demonwav.hypo.mappings.LorenzUtil;
import com.demonwav.hypo.mappings.MergeResult;
import com.demonwav.hypo.mappings.MergeableMappingsChange;
import org.cadixdev.lorenz.MappingSet;
import org.cadixdev.lorenz.model.ClassMapping;
import org.cadixdev.lorenz.model.MethodMapping;
import org.cadixdev.lorenz.model.MethodParameterMapping;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import static com.demonwav.hypo.mappings.LorenzUtil.getClassMapping;
import static com.demonwav.hypo.mappings.LorenzUtil.getMethodMapping;
import static com.demonwav.hypo.mappings.LorenzUtil.getParameterMapping;

/**
 * {@link com.demonwav.hypo.mappings.MappingsChange MappingsChange} which removes parameter mappings.
 */
public class RemoveParameterMappingChange
    extends AbstractMappingsChange
    implements MergeableMappingsChange<RemoveParameterMappingChange> {

    private final long indices;

    private RemoveParameterMappingChange(@NotNull MemberReference target, long index) {
        super(target);
        this.indices = index;
    }

    /**
     * Create a new instance of {@link RemoveParameterMappingChange}.
     *
     * @param target The {@link MemberReference} that contains the parameter mappings to remove.
     * @param index The index of the parameter mapping to remove.
     * @return A new instance of {@link RemoveParameterMappingChange}.
     */
    @Contract(value = "_, _ -> new", pure = true)
    public static @NotNull RemoveParameterMappingChange of(final @NotNull MemberReference target, final int index) {
        return new RemoveParameterMappingChange(target, toBit(index));
    }

    @Override
    public void applyChange(final @NotNull MappingSet input, final @NotNull MemberReference target) {
        if (target.desc() == null) {
            return;
        }

        final ClassMapping<?, ?> classMapping = getClassMapping(input, target.className());
        if (classMapping == null) {
            return;
        }

        final MethodMapping methodMapping = getMethodMapping(classMapping, target.name(), target.desc());
        if (methodMapping == null) {
            return;
        }

        for (int i = 0; i < 64; i++) {
            if (isBitSet(this.indices, i)) {
                final MethodParameterMapping paramMapping = getParameterMapping(methodMapping, i);
                if (paramMapping != null) {
                    LorenzUtil.removeParamMapping(paramMapping);
                }
            }
        }
    }

    @Override
    public @NotNull MergeResult<RemoveParameterMappingChange> mergeWith(
        final @NotNull RemoveParameterMappingChange that
    ) {
        return MergeResult.success(new RemoveParameterMappingChange(this.target(), this.indices | that.indices));
    }

    private static long toBit(final int number) {
        return 1L << number;
    }

    private static boolean isBitSet(final long bitset, final int index) {
        return ((bitset >> index) & 1) == 1;
    }
}
