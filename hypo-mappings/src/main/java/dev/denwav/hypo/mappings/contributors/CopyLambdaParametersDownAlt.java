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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CopyLambdaParametersDownAlt implements ChangeContributor {

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
            registry.submitChange(new MappingsChange() {
                @Override
                public @NotNull MemberReference target() {
                    return MemberReference.of(method);
                }

                @Override
                public void applyChange(final @NotNull MappingSet input) {
                    final MethodMapping methodMapping = input.getOrCreateClassMapping(method.parentClass().name()).getOrCreateMethodMapping(MethodSignature.of(method.name(), method.descriptorText()));
                    final Optional<MethodMapping> interfaceMethodMapping = input.getClassMapping(interfaceMethod.parentClass().name()).flatMap(c -> c.getMethodMapping(MethodSignature.of(interfaceMethod.name(), interfaceMethod.descriptorText())));
                    if (interfaceMethodMapping.isPresent()) {
                        for (int i = 1; i <= interfaceMethod.descriptor().getParams().size(); i++) { // 1, skip "this" (I think)
                            final Optional<MethodParameterMapping> parameterMapping = interfaceMethodMapping.get().getParameterMapping(i);
                            if (parameterMapping.isPresent() && !methodMapping.hasParameterMapping(i + paramOffset)) {
                                methodMapping.createParameterMapping(i + paramOffset, parameterMapping.get().getDeobfuscatedName());
                            }
                        }
                    }
                }
            });
        }
    }

    @Override
    public @NotNull String name() {
        return "CopyLambdaParametersDownAlt";
    }
}
