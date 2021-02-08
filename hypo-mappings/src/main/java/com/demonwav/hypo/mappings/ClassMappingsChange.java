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

import org.cadixdev.lorenz.MappingSet;
import org.jetbrains.annotations.NotNull;

/**
 * A change to a {@link MappingSet}. The change targets the class name returned by {@link #targetClass()}. Only
 * a single change can target a given class name at a time.
 *
 * <p>Mappings changes are submitted to the {@link ChangeRegistry} passed to
 * {@link com.demonwav.hypo.mappings.contributors.ChangeContributor change contributors}.
 *
 * <p>Use {@link MappingsChange} for changes which target class members rather than classes.
 *
 * @see MergeableMappingsChange
 */
public interface ClassMappingsChange {

    /**
     * The name of the class this mappings change targets.
     *
     * @return The name of the class this mappings change targets.
     */
    @NotNull String targetClass();

    /**
     * Apply this class mappings change to the given {@link MappingSet}.
     *
     * @implNote For making changes to the Lorenz {@link MappingSet} model outside of what the API allows use
     *           {@link LorenzUtil}.
     *
     * @param input The {@link MappingSet} to apply the change to.
     */
    void applyChange(final @NotNull MappingSet input);
}
