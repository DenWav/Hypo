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

import dev.denwav.hypo.hydrate.generic.HypoHydration;
import dev.denwav.hypo.hydrate.generic.SuperCall;
import dev.denwav.hypo.mappings.MergeResult;
import dev.denwav.hypo.mappings.MergeableMappingsChange;
import dev.denwav.hypo.mappings.MappingsChange;
import dev.denwav.hypo.model.data.MethodData;
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

    private final @NotNull MethodData superMethod;
    private final @NotNull MethodMapping superMapping;
    private final @NotNull List<SuperCall.SuperCallParameter> params = new ArrayList<>();

    private CopyConstructorMappingChange(
        final @NotNull MemberReference target,
        final @NotNull MethodData superMethod,
        final @NotNull MethodMapping superMapping
    ) {
        super(target);
        this.superMethod = superMethod;
        this.superMapping = superMapping;
    }

    /**
     * Create a new instance of {@link CopyConstructorMappingChange}.
     *
     * @param target The {@link MemberReference} this change targets.
     * @param superMethod The {@link MethodData} of the super method constructor.
     * @param superMapping The {@link MethodMapping} of the super constructor to copy the parameters from.
     * @return A new instance of {@link CopyConstructorMappingChange}.
     */
    @Contract(value = "_, _, _ -> new", pure = true)
    public static @NotNull CopyConstructorMappingChange of(
        final @NotNull MemberReference target,
        final @NotNull MethodData superMethod,
        final @NotNull MethodMapping superMapping
    ) {
        return new CopyConstructorMappingChange(target, superMethod, superMapping);
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
        final int thisSuperSize = this.superMapping.getDescriptor().getParamTypes().size();
        final int thatSuperSize = that.superMapping.getDescriptor().getParamTypes().size();
        final int maxSuperSize = Math.max(thisSuperSize, thatSuperSize);

        boolean thisPresent = false;
        boolean thatPresent = false;

        for (int i = 0; i < maxSuperSize; i++) {
            if (!thisPresent) {
                if (this.superMapping.hasParameterMapping(i)) {
                    thisPresent = true;
                }
            }
            if (!thatPresent) {
                if (that.superMapping.hasParameterMapping(i)) {
                    thatPresent = true;
                }
            }
        }

        if (!thisPresent) {
            return MergeResult.success(that);
        }
        if (!thatPresent) {
            return MergeResult.success(this);
        }

        // Try to find which method is "higher"
        MethodData target = this.superMethod;
        while (true) {
            if (target.equals(that.superMethod)) {
                return MergeResult.success(that);
            }
            final SuperCall superCall = target.get(HypoHydration.SUPER_CALL_TARGET);
            if (superCall == null) {
                break;
            }
            target = superCall.getSuperConstructor();
        }

        target = that.superMethod;
        while (true) {
            if (target.equals(this.superMethod)) {
                return MergeResult.success(this);
            }
            final SuperCall superCall = target.get(HypoHydration.SUPER_CALL_TARGET);
            if (superCall == null) {
                break;
            }
            target = superCall.getSuperConstructor();
        }

        // If we can't find the other either direction then these 2 changes are completely diverged
        return MergeResult.failure("Cannot merge super calls from two constructors from different trees");
    }

    @Override
    public String toString() {
        return "Copy constructor mapping '" + this.superMapping.getFullDeobfuscatedName() + "' to " + this.target();
    }
}
