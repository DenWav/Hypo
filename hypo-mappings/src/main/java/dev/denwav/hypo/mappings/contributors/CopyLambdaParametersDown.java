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
import dev.denwav.hypo.mappings.changes.CopyLambdaParametersChange;
import dev.denwav.hypo.mappings.changes.MemberReference;
import dev.denwav.hypo.model.data.ClassData;
import dev.denwav.hypo.model.data.MethodData;
import java.util.List;
import org.cadixdev.lorenz.model.ClassMapping;
import org.cadixdev.lorenz.model.MethodMapping;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static dev.denwav.hypo.mappings.LorenzUtil.getClassMapping;
import static dev.denwav.hypo.mappings.LorenzUtil.getMethodMapping;

/**
 * Implementation of {@link ChangeContributor} which copies {@link MethodMapping method mappings} from a lambda interface
 * method to any synthetic lambdas in the source.
 */
public class CopyLambdaParametersDown implements ChangeContributor {

    private final boolean overrideLambdaMappings;

    private CopyLambdaParametersDown(final boolean overrideLambdaMappings) {
        this.overrideLambdaMappings = overrideLambdaMappings;
    }

    /**
     * Create a new instance of {@link CopyLambdaParametersDown}. This instance <b>will</b> overwrite any mappings that
     * are set on the synthetic lambda method.
     * @return A new instance of {@link CopyLambdaParametersDown}.
     */
    @Contract(value = "-> new", pure = true)
    public static @NotNull CopyLambdaParametersDown create() {
        return new CopyLambdaParametersDown(true);
    }

    /**
     * Create a new instance of {@link CopyLambdaParametersDown}. This instance <b>will not</b> overwrite any mappings that
     * are set on the synthetic lambda method.
     * @return A new instance of {@link CopyLambdaParametersDown}.
     */
    @Contract(value = "-> new", pure = true)
    public static @NotNull CopyLambdaParametersDown createWithoutOverwrite() {
        return new CopyLambdaParametersDown(false);
    }

    @Override
    public void contribute(
        final @Nullable ClassData currentClass,
        final @Nullable ClassMapping<?, ?> classMapping,
        final @NotNull HypoContext context,
        final @NotNull ChangeRegistry registry
    ) {
        if (currentClass == null) {
            return;
        }

        for (final MethodData method : currentClass.methods()) {
            if (!method.isSynthetic()) {
                continue;
            }

            final List<LambdaClosure> lambdaClosures = method.get(HypoHydration.LAMBDA_CALLS);
            if (lambdaClosures == null || lambdaClosures.isEmpty()) {
                continue;
            }

            LambdaClosure closure = null;
            for (final LambdaClosure possibleLambdaClosure : lambdaClosures) {
                if (possibleLambdaClosure.getLambda().equals(method)) {
                    closure = possibleLambdaClosure;
                    break;
                }
            }
            if (closure == null || closure.getInterfaceMethod() == null) {
                continue;
            }

            final MethodMapping lambdaMapping = getMethodMapping(classMapping, method);
            if (!this.overrideLambdaMappings) {
                if (lambdaMapping != null && !lambdaMapping.getParameterMappings().isEmpty()) {
                    continue;
                }
            }

            final MethodData ifaceMethod = closure.getInterfaceMethod();
            final int paramOffset = closure.getParamLvtIndices().length - 1;

            final ClassMapping<?, ?> ifaceClassMapping = getClassMapping(registry.getMappings(), ifaceMethod.parentClass().name());
            final MethodMapping ifaceMethodMapping = getMethodMapping(ifaceClassMapping, ifaceMethod);

            if (ifaceMethodMapping == null || ifaceMethodMapping.getParameterMappings().isEmpty()) {
                continue;
            }

            final MemberReference methodRef = MemberReference.of(method);
            registry.submitChange(CopyLambdaParametersChange.of(methodRef, ifaceMethodMapping, paramOffset));
        }
    }

    @Override
    public @NotNull String name() {
        return "CopyLambdaParametersDown";
    }
}
