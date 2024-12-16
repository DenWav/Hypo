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

package dev.denwav.hypo.asm.scenarios;

import dev.denwav.hypo.asm.hydrate.LambdaCallHydrator;
import dev.denwav.hypo.asm.hydrate.LocalClassHydrator;
import dev.denwav.hypo.hydrate.HydrationProvider;
import dev.denwav.hypo.hydrate.generic.HypoHydration;
import dev.denwav.hypo.hydrate.generic.LambdaClosure;
import dev.denwav.hypo.hydrate.generic.LocalClassClosure;
import dev.denwav.hypo.model.data.MethodData;
import dev.denwav.hypo.test.framework.TestScenarioBase;
import dev.denwav.hypo.types.desc.MethodDescriptor;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("[asm] Scenario 07 - Local class scopes inside lambda scopes (Java 21)")
public class Scenario07Test extends TestScenarioBase {

    @Override
    public @NotNull Env env() {
        return new Env() {
            @Override
            public @NotNull String forContext() {
                return "scenario-07";
            }

            @Override
            public @NotNull Iterable<HydrationProvider<?>> hydration() {
                return List.of(LambdaCallHydrator.create(), LocalClassHydrator.create());
            }
        };
    }

    @Test
    @DisplayName("Test local classes hydrator")
    public void testLocalClasses() {
        final var testClass = this.findClass("scenario07/TestClass");
        assertNotNull(testClass);

        final MethodData testMethod = testClass.method("test", MethodDescriptor.parse("()V"));
        assertNotNull(testMethod);

        final List<LambdaClosure> firstLambdas = testMethod.get(HypoHydration.LAMBDA_CALLS);
        assertNotNull(firstLambdas);
        assertEquals(1, firstLambdas.size());
        final LambdaClosure firstLambda = firstLambdas.getFirst();

        final List<LambdaClosure> secondLambdas = firstLambda.getLambda().get(HypoHydration.LAMBDA_CALLS);
        assertNotNull(secondLambdas);
        assertEquals(2, secondLambdas.size());
        final LambdaClosure secondLambda = secondLambdas.stream().filter(l -> l.getContainingMethod().equals(firstLambda.getLambda())).findFirst().orElse(null);
        assertNotNull(secondLambda);

        final List<LambdaClosure> thirdLambdas = secondLambda.getLambda().get(HypoHydration.LAMBDA_CALLS);
        assertNotNull(thirdLambdas);
        assertEquals(2, thirdLambdas.size());
        final LambdaClosure thirdLambda = thirdLambdas.stream().filter(l -> l.getContainingMethod().equals(secondLambda.getLambda())).findFirst().orElse(null);
        assertNotNull(thirdLambda);

        final List<LocalClassClosure> locals = thirdLambda.getLambda().get(HypoHydration.LOCAL_CLASSES);
        assertNotNull(locals);
        assertEquals(1, locals.size());
        final LocalClassClosure local = locals.get(0);
        assertNotNull(local);

        assertArrayEquals(new int[] { 0, 2, 3, 4 }, firstLambda.getParamLvtIndices());
        assertArrayEquals(new int[] { 0, 1, 2, 3 }, secondLambda.getParamLvtIndices());
        assertArrayEquals(new int[] { 0, 1, 2, 3 }, thirdLambda.getParamLvtIndices());
        assertArrayEquals(new int[] { 1, 2, 3 }, local.getParamLvtIndices());
    }
}
