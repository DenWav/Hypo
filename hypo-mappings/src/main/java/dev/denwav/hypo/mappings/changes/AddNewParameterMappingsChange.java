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

package dev.denwav.hypo.mappings.changes;

import dev.denwav.hypo.mappings.MappingsChange;
import dev.denwav.hypo.mappings.MergeResult;
import dev.denwav.hypo.mappings.MergeableMappingsChange;
import org.cadixdev.lorenz.MappingSet;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * {@link MappingsChange MappingsChange} which adds or sets a parameter mapping.
 */
public class AddNewParameterMappingsChange
    extends AbstractMappingsChange
    implements MergeableMappingsChange<AddNewParameterMappingsChange> {

    private final @NotNull String newName;

    private AddNewParameterMappingsChange(
        final @NotNull MemberReference reference,
        final @NotNull String newName
    ) {
        super(reference);
        this.newName = newName;
    }

    /**
     * Create a new instance of {@link AddNewParameterMappingsChange}.
     *
     * @param target The {@link MemberReference} that refers to the parameter mapping to add.
     * @param newName The new name of the parameter mapping to dad.
     * @return A new instance of {@link AddNewParameterMappingsChange}.
     */
    @Contract(value = "_, _ -> new", pure = true)
    public static @NotNull AddNewParameterMappingsChange of(
        final @NotNull MemberReference target,
        final @NotNull String newName
    ) {
        return new AddNewParameterMappingsChange(target, newName);
    }

    @Override
    public void applyChange(
        final @NotNull MappingSet input,
        final @NotNull MemberReference target
    ) {
        if (target.desc() == null) {
            return;
        }

        input.getOrCreateClassMapping(target.className())
            .getOrCreateMethodMapping(target.name(), target.desc())
            .getOrCreateParameterMapping(target.index())
            .setDeobfuscatedName(this.newName);
    }

    @Override
    public @NotNull MergeResult<AddNewParameterMappingsChange> mergeWith(
        final @NotNull AddNewParameterMappingsChange that
    ) {
        if (this.newName.equals(that.newName)) {
            return MergeResult.success(this);
        }

        return MergeResult.failure("Cannot merge add parameter mapping changes with different names");
    }

    @Override
    public String toString() {
        return "Add parameter mapping '" + this.newName + "' to " + this.target();
    }
}
