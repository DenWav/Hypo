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

import com.demonwav.hypo.mappings.changes.MemberReference;
import org.cadixdev.lorenz.MappingSet;
import org.jetbrains.annotations.NotNull;

/**
 * A change to a {@link MappingSet}. The change targets the {@link MemberReference} returned by {@link #target()}. Only
 * a single change can target a given {@link MemberReference} at a time.
 *
 * <p>If multiple changes target a single {@link MemberReference} they must be {@link MergeableMappingsChange} which are
 * compatible with each other.
 *
 * <p>Mappings changes are submitted to the {@link ChangeRegistry} passed to
 * {@link com.demonwav.hypo.mappings.contributors.ChangeContributor change contributors}.
 *
 * <p>Use {@link ClassMappingsChange} for changes which target classes rather than class members.
 * @see MergeableMappingsChange
 */
public interface MappingsChange {

    /**
     * The {@link MemberReference} which corresponds with the obfuscated name the mapping which will be changed in
     * {@link #applyChange(MappingSet)}.
     *
     * @return The {@link MemberReference} which this change targets.
     */
    @NotNull MemberReference target();

    /**
     * Apply this change to the given {@link MappingSet}.
     *
     * @implNote For making changes to the Lorenz {@link MappingSet} model outside of what the API allows use
     *           {@link LorenzUtil}.
     *
     * @param input The {@link MappingSet} to change.
     */
    void applyChange(final @NotNull MappingSet input);
}
