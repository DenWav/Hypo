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
import org.cadixdev.lorenz.MappingSet;
import org.cadixdev.lorenz.model.ClassMapping;
import org.cadixdev.lorenz.model.MethodMapping;
import org.cadixdev.lorenz.model.MethodParameterMapping;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * {@link dev.denwav.hypo.mappings.MappingsChange MappingsChange} which copies functional interface method parameter
 * mappings to lambda methods that implement it.
 */
public class CopyLambdaParametersChange
    extends AbstractMappingsChange
    implements MergeableMappingsChange<CopyLambdaParametersChange> {

    private final @NotNull MethodMapping mapping;
    private final int paramOffset;

    private CopyLambdaParametersChange(
        final @NotNull MemberReference target,
        final @NotNull MethodMapping mapping,
        final int paramOffset
    ) {
        super(target);
        this.mapping = mapping;
        this.paramOffset = paramOffset;
    }

    /**
     * Create a new instance of {@link CopyLambdaParametersChange}.
     *
     * @param target The {@link MemberReference} this change targets.
     * @param mapping The {@link MethodMapping} to copy parameter mappings from.
     * @param paramOffset The parameter index offset to apply to the lambda method mappings.
     * @return A new instance of {@link CopyLambdaParametersChange}.
     */
    @Contract(value = "_, _, _ -> new", pure = true)
    public static @NotNull CopyLambdaParametersChange of(
        final @NotNull MemberReference target,
        final @NotNull MethodMapping mapping,
        final int paramOffset
    ) {
        return new CopyLambdaParametersChange(target, mapping, paramOffset);
    }

    @Override
    public void applyChange(final @NotNull MappingSet input, final @NotNull MemberReference target) {
        final ClassMapping<?, ?> map = input.getOrCreateClassMapping(target.className());
        final MethodMapping newMapping = map.getOrCreateMethodMapping(target.name(), target.desc());

        for (final MethodParameterMapping paramMapping : this.mapping.getParameterMappings()) {
            newMapping.getOrCreateParameterMapping(paramMapping.getIndex() + this.paramOffset)
                .setDeobfuscatedName(paramMapping.getDeobfuscatedName());
        }
    }

    @Override
    public @NotNull MergeResult<CopyLambdaParametersChange> mergeWith(final @NotNull CopyLambdaParametersChange that) {
        if (this.mapping.getParameterMappings().size() > that.mapping.getParameterMappings().size()) {
            return MergeResult.success(this);
        } else {
            return MergeResult.success(that);
        }
    }

    @Override
    public String toString() {
        return "Copy interface parameter mappings '" + this.mapping.getFullDeobfuscatedName() + "' to lambda " + this.target();
    }
}
