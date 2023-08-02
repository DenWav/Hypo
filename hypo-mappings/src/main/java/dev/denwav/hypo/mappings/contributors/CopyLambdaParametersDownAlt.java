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

package dev.denwav.hypo.mappings.contributors;

import dev.denwav.hypo.core.HypoContext;
import dev.denwav.hypo.hydrate.generic.HypoHydration;
import dev.denwav.hypo.hydrate.generic.LambdaClosure;
import dev.denwav.hypo.mappings.ChangeRegistry;
import dev.denwav.hypo.mappings.MappingsChange;
import dev.denwav.hypo.mappings.changes.MemberReference;
import dev.denwav.hypo.model.data.ClassData;
import dev.denwav.hypo.model.data.MethodData;
import java.util.List;
import java.util.Optional;
import org.cadixdev.bombe.type.signature.MethodSignature;
import org.cadixdev.lorenz.MappingSet;
import org.cadixdev.lorenz.model.ClassMapping;
import org.cadixdev.lorenz.model.MethodMapping;
import org.cadixdev.lorenz.model.MethodParameterMapping;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Implementation of {@link ChangeContributor} which copies {@link MethodMapping method mappings} from a lambda interface
 * method to any synthetic lambdas in the source.
 */
public class CopyLambdaParametersDownAlt implements ChangeContributor {

    private final boolean overrideLambdaMappings;

    private CopyLambdaParametersDownAlt(final boolean overrideLambdaMappings) {
        this.overrideLambdaMappings = overrideLambdaMappings;
    }

    /**
     * Create a new instance of {@link CopyLambdaParametersDownAlt}. This instance <b>will</b> overwrite any mappings that
     * are set on the synthetic lambda method.
     * @return A new instance of {@link CopyLambdaParametersDownAlt}.
     */
    @Contract(value = "-> new", pure = true)
    public static @NotNull CopyLambdaParametersDownAlt create() {
        return new CopyLambdaParametersDownAlt(true);
    }

    /**
     * Create a new instance of {@link CopyLambdaParametersDownAlt}. This instance <b>will not</b> overwrite any mappings that
     * are set on the synthetic lambda method.
     * @return A new instance of {@link CopyLambdaParametersDownAlt}.
     */
    @Contract(value = "-> new", pure = true)
    public static @NotNull CopyLambdaParametersDownAlt createWithoutOverwrite() {
        return new CopyLambdaParametersDownAlt(false);
    }
    @Override
    public void contribute(final @Nullable ClassData currentClass, final @Nullable ClassMapping<?, ?> classMapping, final @NotNull HypoContext context, final @NotNull ChangeRegistry registry) throws Throwable {
        if (currentClass == null) {
            return;
        }

        for (final MethodData method : currentClass.methods()) {
            if (!method.isSynthetic()) continue;
            final List<LambdaClosure> lambdaClosures = method.get(HypoHydration.LAMBDA_CALLS);
            if (lambdaClosures == null || lambdaClosures.isEmpty()) continue;

            LambdaClosure notFinalLambdaClosure = null;
            for (final LambdaClosure possibleLambdaClosure : lambdaClosures) {
                if (possibleLambdaClosure.getLambda().equals(method) && !possibleLambdaClosure.getContainingMethod().equals(method)) {
                    notFinalLambdaClosure = possibleLambdaClosure;
                    break;
                }
            }
            if (notFinalLambdaClosure == null || notFinalLambdaClosure.getInterfaceMethod() == null) {
                return;
            }
            final LambdaClosure lambdaClosure = notFinalLambdaClosure;
            final MethodData interfaceMethod = lambdaClosure.getInterfaceMethod();


            final int paramOffset = lambdaClosure.getParamLvtIndices().length - 1;
            final MethodSignature lambdaMethodSignature = MethodSignature.of(method.name(), method.descriptorText());
            registry.submitChange(new MappingsChange() {
                @Override
                public @NotNull MemberReference target() {
                    return MemberReference.of(method);
                }

                @Override
                public void applyChange(final @NotNull MappingSet input) {
                    final Optional<MethodMapping> interfaceMethodMapping = input.getClassMapping(interfaceMethod.parentClass().name()).flatMap(c -> c.getMethodMapping(MethodSignature.of(interfaceMethod.name(), interfaceMethod.descriptorText())));
                    if (interfaceMethodMapping.isPresent()) {
                        for (int i = 1; i <= interfaceMethod.descriptor().getParams().size(); i++) { // 1, skip "this" (I think)
                            final int lambdaParamIdx = i + paramOffset;
                            final Optional<MethodParameterMapping> interfaceMethodParamMapping = interfaceMethodMapping.get().getParameterMapping(i);
                            if (interfaceMethodParamMapping.isPresent() && (CopyLambdaParametersDownAlt.this.overrideLambdaMappings || !this.hasParameterMapping(method, input, lambdaParamIdx))) {
                                input.getOrCreateClassMapping(method.parentClass().name()).getOrCreateMethodMapping(lambdaMethodSignature).getOrCreateParameterMapping(lambdaParamIdx).setDeobfuscatedName(interfaceMethodParamMapping.get().getDeobfuscatedName());
                            }
                        }
                    }
                }

                private boolean hasParameterMapping(final MethodData lambdaMethod, final MappingSet input, int paramIdx) {
                    return input.getClassMapping(lambdaMethod.parentClass().name()).flatMap(c -> c.getMethodMapping(lambdaMethodSignature)).map(m -> m.hasParameterMapping(paramIdx)).orElse(false);
                }
            });
        }
    }

    @Override
    public @NotNull String name() {
        return "CopyLambdaParametersDownAlt";
    }
}
