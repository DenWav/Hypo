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

import dev.denwav.hypo.hydrate.generic.SuperCall;
import dev.denwav.hypo.mappings.MappingsChange;
import dev.denwav.hypo.mappings.MergeResult;
import dev.denwav.hypo.mappings.MergeableMappingsChange;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;
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
     * @param superMapping The {@link MethodMapping} of the super constructor to copy the parameters from.
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
        final int thisCount = this.superMapping.getParameterMappings().size();
        final int thatCount = that.superMapping.getParameterMappings().size();

        if (thisCount == 0 && thatCount != 0) {
            return MergeResult.success(that);
        }
        if (thatCount == 0) {
            return MergeResult.success(this);
        }

        final HashMap<Integer, String> mergedParams = new HashMap<>();
        // both `this` and `that` carry mappings
        for (final MethodParameterMapping thisParamMapping : this.superMapping.getParameterMappings()) {
            final int index = thisParamMapping.getIndex();
            for (final SuperCall.SuperCallParameter thisParam : this.params) {
                if (index == thisParam.getSuperIndex()) {
                    mergedParams.put(thisParam.getThisIndex(), thisParamMapping.getDeobfuscatedName());
                    break;
                }
            }
        }

        for (final MethodParameterMapping thatParamMapping : that.superMapping.getParameterMappings()) {
            final int index = thatParamMapping.getIndex();
            for (final SuperCall.SuperCallParameter thatParam : that.params) {
                if (index != thatParam.getSuperIndex()) {
                    continue;
                }
                final String existingMapping = mergedParams.get(thatParam.getThisIndex());
                if (existingMapping == null) {
                    mergedParams.put(thatParam.getThisIndex(), thatParamMapping.getDeobfuscatedName());
                } else {
                    if (existingMapping.equals(thatParamMapping.getDeobfuscatedName())) {
                        // nothing to do, they match
                        break;
                    }
                    // We don't have a good solution here. They don't match and we don't really have a way of
                    // determining which one may take precedence. Don't fail here, just take one of the names.
                    //
                    // The following logic to determine which name to take is effectively random, and shouldn't be
                    // considered meaningful in any way. It's simply here as an attempt at keeping the result of this
                    // method deterministic.

                    // Keep the one with more param names present
                    if (thisCount > thatCount) {
                        break;
                    } else if (thisCount < thatCount) {
                        mergedParams.put(thatParam.getThisIndex(), thatParamMapping.getDeobfuscatedName());
                        break;
                    }

                    // Keep the one with the shorter class name
                    final int thisClassNameLen = this.superMapping.getParent().getDeobfuscatedName().length();
                    final int thatClassNameLen = that.superMapping.getParent().getDeobfuscatedName().length();
                    if (thisClassNameLen < thatClassNameLen) {
                        break;
                    } else if (thisClassNameLen > thatClassNameLen) {
                        mergedParams.put(thatParam.getThisIndex(), thatParamMapping.getDeobfuscatedName());
                        break;
                    }

                    // Keep the shorter parameter name
                    final int existingLen = existingMapping.length();
                    final int otherLen = thatParamMapping.getDeobfuscatedName().length();
                    if (existingLen < otherLen) {
                        break;
                    } else if (existingLen > otherLen) {
                        mergedParams.put(thatParam.getThisIndex(), thatParamMapping.getDeobfuscatedName());
                        break;
                    }

                    // Keep the parameter name with the smaller hashcode (remember, this is arbitrary and random)
                    // This is our last attempt, if the hashcodes are equal, even though the strings are not, we will
                    // just take the other one.
                    final int existingHash = existingMapping.hashCode();
                    final int otherHash = thatParamMapping.getDeobfuscatedName().hashCode();
                    if (existingHash < otherHash) {
                        break;
                    } else if (existingHash > otherHash) {
                        mergedParams.put(thatParam.getThisIndex(), thatParamMapping.getDeobfuscatedName());
                    }
                }
            }
        }

        // they both agree, keep the one with more
        if (thisCount > thatCount) {
            return MergeResult.success(this);
        } else {
            return MergeResult.success(that);
        }
    }

    @Override
    public String toString() {
        return "Copy constructor mapping '" + this.superMapping.getFullDeobfuscatedName() + "' to " + this.target();
    }
}
