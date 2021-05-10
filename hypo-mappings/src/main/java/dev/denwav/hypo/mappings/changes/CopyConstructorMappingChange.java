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

import dev.denwav.hypo.hydrate.generic.SuperCall;
import dev.denwav.hypo.mappings.MergeResult;
import dev.denwav.hypo.mappings.MergeableMappingsChange;
import dev.denwav.hypo.mappings.MappingsChange;
import java.util.ArrayList;
import java.util.List;
import org.cadixdev.lorenz.MappingSet;
import org.cadixdev.lorenz.model.ClassMapping;
import org.cadixdev.lorenz.model.MethodMapping;
import org.cadixdev.lorenz.model.MethodParameterMapping;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import static dev.denwav.hypo.mappings.LorenzUtil.getParameterMapping;

/**
 * {@link MappingsChange MappingsChange} which copies a set of parameter mappings for a given
 * constructor mapping.
 */
public class CopyConstructorMappingChange
    extends AbstractMappingsChange
    implements MergeableMappingsChange<CopyConstructorMappingChange> {

    private final @NotNull MethodMapping superMapping;
    private final @NotNull List<SuperCall.SuperCallParameter> params = new ArrayList<>();

    private CopyConstructorMappingChange(
        final @NotNull MemberReference target,
        final @NotNull MethodMapping superMapping
    ) {
        super(target);
        this.superMapping = superMapping;
    }

    /**
     * Create a new instance of {@link CopyConstructorMappingChange}.
     *
     * @param target The {@link MemberReference} this change targets.
     * @param superMapping The {@link MethodMapping} of the super constructor to copy the paramters from.
     * @return A new instance of {@link CopyConstructorMappingChange}.
     */
    @Contract(value = "_, _ -> new", pure = true)
    public static @NotNull CopyConstructorMappingChange of(
        final @NotNull MemberReference target,
        final @NotNull MethodMapping superMapping
    ) {
        return new CopyConstructorMappingChange(target, superMapping);
    }

    /**
     * Returns the {@link MethodMapping} which the parameter mappings will be copied from.
     * @return The {@link MethodMapping} which the parameter mappings will be copied from.
     */
    public @NotNull MethodMapping superMapping() {
        return this.superMapping;
    }

    /**
     * Returns the list of {@link SuperCall.SuperCallParameter parameters} which maps the super constructor parameters.
     * @return The list of {@link SuperCall.SuperCallParameter parameters} which maps the super constructor parameters.
     */
    public @NotNull List<SuperCall.SuperCallParameter> params() {
        return this.params;
    }

    /**
     * Add additional {@link SuperCall.SuperCallParameter parameters} to map super constructor parameters.
     * @param params The new {@link SuperCall.SuperCallParameter parameters} to map as well.
     */
    public void addParams(final @NotNull List<SuperCall.SuperCallParameter> params) {
        this.params.addAll(params);
    }

    @Override
    public void applyChange(final @NotNull MappingSet input, final @NotNull MemberReference target) {
        if (target.desc() == null) {
            return;
        }

        final ClassMapping<?, ?> classMapping = input.getOrCreateClassMapping(target.className());
        final MethodMapping targetMapping = classMapping.getOrCreateMethodMapping(target.name(), target.desc());

        for (final SuperCall.SuperCallParameter param : this.params) {
            final MethodParameterMapping paramMapping = getParameterMapping(this.superMapping, param.getSuperIndex());
            if (paramMapping == null) {
                continue;
            }
            targetMapping.getOrCreateParameterMapping(param.getThisIndex())
                .setDeobfuscatedName(paramMapping.getDeobfuscatedName());
        }
    }

    @Override
    public @NotNull MergeResult<CopyConstructorMappingChange> mergeWith(
        final @NotNull CopyConstructorMappingChange that
    ) {
        if (!this.superMapping.getDescriptor().equals(that.superMapping.getDescriptor())) {
            return MergeResult.failure("Cannot merge constructor mappings changes with different super " +
                "constructor mappings: " + this.superMapping + " and " + that.superMapping);
        }

        for (final SuperCall.SuperCallParameter param : that.params) {
            final boolean isDupe = this.params.stream()
                .anyMatch(p -> {
                    return (p.getThisIndex() == param.getThisIndex()) ^ (p.getSuperIndex() == param.getSuperIndex());
                });
            if (isDupe) {
                return MergeResult.failure("Cannot merge super call with duplicate parameter indexes: " +
                    this.params + " and " + that.params);
            }
        }

        this.params.addAll(that.params);
        return MergeResult.success(this);
    }

    @Override
    public String toString() {
        return "Copy constructor mapping '" + this.superMapping.getFullDeobfuscatedName() + "' to " + this.target();
    }
}
