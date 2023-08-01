package dev.denwav.hypo.mappings.contributors;

import dev.denwav.hypo.core.HypoContext;
import dev.denwav.hypo.hydrate.generic.HypoHydration;
import dev.denwav.hypo.hydrate.generic.LambdaClosure;
import dev.denwav.hypo.mappings.ChangeRegistry;
import dev.denwav.hypo.mappings.MappingsChange;
import dev.denwav.hypo.mappings.changes.MemberReference;
import dev.denwav.hypo.model.data.ClassData;
import dev.denwav.hypo.model.data.ClassKind;
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

public class CopyLambdaParametersDown implements ChangeContributor {

    @Override
    public void contribute(final @Nullable ClassData currentClass, final @Nullable ClassMapping<?, ?> classMapping, final @NotNull HypoContext context, final @NotNull ChangeRegistry registry) throws Throwable {
        if (currentClass == null || classMapping == null || !currentClass.is(ClassKind.INTERFACE)) {
            return;
        }
        @Nullable MethodData notFinalInterfaceMethod = null;
        for (final MethodData method : currentClass.methods()) {
            if (method.isAbstract()) {
                if (notFinalInterfaceMethod != null) {
                    return; // more than 1 abstract method in an interface is not a lambda
                }
                notFinalInterfaceMethod = method;
            }
        }
        if (notFinalInterfaceMethod == null) {
            return;
        }
        final MethodData interfaceMethod = notFinalInterfaceMethod;
        final Optional<MethodMapping> interfaceMethodMapping = classMapping.getMethodMapping(MethodSignature.of(interfaceMethod.name(), interfaceMethod.descriptorText()));
        if (!interfaceMethodMapping.isPresent()) {
            return;
        }
        final List<LambdaClosure> lambdaClosures = interfaceMethod.get(HypoHydration.LAMBDA_CALLS);
        if (lambdaClosures == null || lambdaClosures.isEmpty()) {
            return;
        }
        for (final LambdaClosure closure : lambdaClosures) {
            if (closure.getContainingMethod().equals(closure.getLambda()) || !closure.getLambda().isSynthetic()) {
                continue;
            }
            final MethodData lambda = closure.getLambda();
            final int paramOffset = closure.getParamLvtIndices().length - 1;
            registry.submitChange(new MappingsChange() {
                @Override
                public @NotNull MemberReference target() {
                    return MemberReference.of(lambda);
                }

                @Override
                public void applyChange(final @NotNull MappingSet input) {
                    final MethodMapping methodMapping = input.getOrCreateClassMapping(lambda.parentClass().name()).getOrCreateMethodMapping(MethodSignature.of(lambda.name(), lambda.descriptorText()));
                    for (int i = 1; i <= interfaceMethod.descriptor().getParams().size(); i++) { // 1, skip "this" (I think)
                        final Optional<MethodParameterMapping> parameterMapping = interfaceMethodMapping.get().getParameterMapping(i);
                        if (parameterMapping.isPresent() && !methodMapping.hasParameterMapping(i + paramOffset)) {
                            methodMapping.createParameterMapping(i + paramOffset, parameterMapping.get().getDeobfuscatedName());
                        }
                    }
                }

                @Override
                public String toString() {
                    return lambda + " " + interfaceMethod;
                }
            });
        }
    }

    @Override
    public @NotNull String name() {
        return "CopyLambdaParametersDown";
    }
}
