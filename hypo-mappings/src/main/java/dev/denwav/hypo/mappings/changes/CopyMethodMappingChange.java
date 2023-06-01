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

import dev.denwav.hypo.mappings.MergeResult;
import dev.denwav.hypo.mappings.MergeableMappingsChange;
import dev.denwav.hypo.mappings.MappingsChange;
import java.util.Collection;
import org.cadixdev.lorenz.MappingSet;
import org.cadixdev.lorenz.model.ClassMapping;
import org.cadixdev.lorenz.model.MethodMapping;
import org.cadixdev.lorenz.model.MethodParameterMapping;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * {@link MappingsChange MappingsChange} which copies a method mapping.
 */
public class CopyMethodMappingChange
    extends AbstractMappingsChange
    implements MergeableMappingsChange<CopyMethodMappingChange> {

    private final @NotNull MethodMapping mapping;

    private CopyMethodMappingChange(final @NotNull MemberReference target, final @NotNull MethodMapping mapping) {
        super(target);
        this.mapping = mapping;
    }

    /**
     * Create a new instance of {@link CopyMethodMappingChange}.
     *
     * @param target The {@link MemberReference} this change targets.
     * @param mapping The {@link MethodMapping} to copy the mapping to.
     * @return A new instance of {@link CopyMethodMappingChange}.
     */
    @Contract(value = "_, _ -> new", pure = true)
    public static @NotNull CopyMethodMappingChange of(final @NotNull MemberReference target, final @NotNull MethodMapping mapping) {
        return new CopyMethodMappingChange(target, mapping);
    }

    /**
     * Returns the {@link MethodMapping} to copy.
     * @return The {@link MethodMapping} to copy.
     */
    public @NotNull MethodMapping methodMapping() {
        return this.mapping;
    }

    @Override
    public void applyChange(final @NotNull MappingSet input, final @NotNull MemberReference target) {
        final ClassMapping<?, ?> map = input.getOrCreateClassMapping(target.className());

        final MethodMapping newMapping = map.getOrCreateMethodMapping(target.name(), target.desc());
        newMapping.setDeobfuscatedName(this.mapping.getDeobfuscatedName());

        for (final MethodParameterMapping paramMapping : this.mapping.getParameterMappings()) {
            newMapping.getOrCreateParameterMapping(paramMapping.getIndex())
                .setDeobfuscatedName(paramMapping.getDeobfuscatedName());
        }
    }

    @Override
    public @NotNull MergeResult<CopyMethodMappingChange> mergeWith(final @NotNull CopyMethodMappingChange that) {
        if (!this.mapping.getDeobfuscatedName().equals(that.mapping.getDeobfuscatedName())) {
            return MergeResult.failure("Cannot merge copies with different target mappings");
        }

        final Collection<MethodParameterMapping> thisParams = this.mapping.getParameterMappings();
        final Collection<MethodParameterMapping> thatParams = that.mapping.getParameterMappings();

        if (thisParams.size() > thatParams.size()) {
            // this.mappings has more param mappings, seems better
            return MergeResult.success(this);
        } else {
            return MergeResult.success(that);
        }
    }

    @Override
    public String toString() {
        return "Copy mapping '" + this.mapping.getFullDeobfuscatedName() + "' to " + this.target();
    }
}
