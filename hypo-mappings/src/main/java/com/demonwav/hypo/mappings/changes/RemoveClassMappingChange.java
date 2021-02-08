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

import com.demonwav.hypo.mappings.ClassMappingsChange;
import com.demonwav.hypo.mappings.LorenzUtil;
import org.cadixdev.lorenz.MappingSet;
import org.cadixdev.lorenz.model.ClassMapping;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import static com.demonwav.hypo.mappings.LorenzUtil.getClassMapping;

/**
 * {@link ClassMappingsChange} which removes the target class mapping.
 */
public class RemoveClassMappingChange implements ClassMappingsChange {

    private final @NotNull String targetClass;

    private RemoveClassMappingChange(final @NotNull String targetClass) {
        this.targetClass = targetClass;
    }

    /**
     * Create a new instance of {@link RemoveClassMappingChange}.
     *
     * @param targetClass The name of the class mapping to remove.
     * @return A new instance of {@link RemoveClassMappingChange}.
     */
    @Contract(value = "_ -> new", pure = true)
    public static @NotNull RemoveClassMappingChange of(final @NotNull String targetClass) {
        return new RemoveClassMappingChange(targetClass);
    }

    @Override
    public @NotNull String targetClass() {
        return this.targetClass;
    }

    @Override
    public void applyChange(@NotNull MappingSet input) {
        final ClassMapping<?, ?> mapping = getClassMapping(input, this.targetClass);
        if (mapping == null) {
            return;
        }

        LorenzUtil.removeClassMapping(mapping);
    }
}
