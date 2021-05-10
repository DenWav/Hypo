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

import dev.denwav.hypo.mappings.MappingsChange;
import dev.denwav.hypo.mappings.MergeResult;
import dev.denwav.hypo.mappings.MergeableMappingsChange;
import org.cadixdev.bombe.type.FieldType;
import org.cadixdev.lorenz.MappingSet;
import org.cadixdev.lorenz.model.ClassMapping;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * {@link MappingsChange MappingsChange} which adds a new member mapping.
 */
public class AddNewMappingChange
    extends AbstractMappingsChange
    implements MergeableMappingsChange<AddNewMappingChange> {

    private final @NotNull String newName;

    private AddNewMappingChange(final @NotNull MemberReference target, final @NotNull String newName) {
        super(target);
        this.newName = newName;
    }

    /**
     * Create a new {@link AddNewMappingChange} for the given {@code target} with the {@code newName} for the
     * deobfuscated name.
     *
     * @param target The {@link MemberReference} this change targets.
     * @param newName The deobfuscated name for the new mapping.
     * @return A new {@link AddNewMappingChange} instance.
     */
    @Contract(value = "_, _ -> new", pure = true)
    public static @NotNull AddNewMappingChange of(final @NotNull MemberReference target, final @NotNull String newName) {
        return new AddNewMappingChange(target, newName);
    }

    @Override
    public void applyChange(final @NotNull MappingSet input, final @NotNull MemberReference target) {
        final ClassMapping<?, ?> map = input.getOrCreateClassMapping(target.className());
        final String desc = target.desc();
        if (desc != null && desc.indexOf('(') != -1) {
            map.getOrCreateMethodMapping(target.name(), desc).setDeobfuscatedName(this.newName);
        } else {
            if (desc == null) {
                map.getOrCreateFieldMapping(target.name()).setDeobfuscatedName(this.newName);
            } else {
                map.getOrCreateFieldMapping(target.name(), FieldType.of(desc)).setDeobfuscatedName(this.newName);
            }
        }

    }

    @Override
    public @NotNull MergeResult<AddNewMappingChange> mergeWith(final @NotNull AddNewMappingChange that) {
        if (this.newName.equals(that.newName)) {
            return MergeResult.success(this);
        }

        return MergeResult.failure("Cannot merge add mapping changes with different names");
    }

    @Override
    public String toString() {
        return "Add mapping '" + this.newName + "' to " + this.target();
    }
}
