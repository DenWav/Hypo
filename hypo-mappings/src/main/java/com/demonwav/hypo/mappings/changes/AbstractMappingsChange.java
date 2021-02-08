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

import com.demonwav.hypo.mappings.MappingsChange;
import java.util.Objects;
import org.cadixdev.lorenz.MappingSet;
import org.jetbrains.annotations.NotNull;

/**
 * Base abstract implementation of {@link MappingsChange} which handles the {@link MemberReference} target part of the
 * implementation.
 */
public abstract class AbstractMappingsChange implements MappingsChange {

    private final @NotNull MemberReference target;

    /**
     * Construct a new {@link AbstractMappingsChange} for the given {@link MemberReference} target.
     * @param target The {@link MemberReference} this change targets.
     */
    protected AbstractMappingsChange(final @NotNull MemberReference target) {
        this.target = target;
    }

    @Override
    public @NotNull MemberReference target() {
        return this.target;
    }

    @Override
    public void applyChange(@NotNull MappingSet input) {
        this.applyChange(input, this.target);
    }

    /**
     * Same as {@link #applyChange(MappingSet)} but it also includes this change's {@link #target()}.
     *
     * @param input The mapping set to change.
     * @param target The member this change targets.
     */
    public abstract void applyChange(final @NotNull MappingSet input, final @NotNull MemberReference target);

    @SuppressWarnings("EqualsGetClass")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        AbstractMappingsChange that = (AbstractMappingsChange) o;
        return this.target.equals(that.target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.target);
    }
}
