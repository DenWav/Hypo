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
import dev.denwav.hypo.hydrate.generic.MethodClosure;
import dev.denwav.hypo.model.data.ClassData;
import dev.denwav.hypo.model.data.MethodData;
import dev.denwav.hypo.test.framework.TestScenarioBase;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static dev.denwav.hypo.model.data.MethodDescriptor.parseDescriptor;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SuppressWarnings("resource")
@DisplayName("[asm] Scenario 07 - Local class scopes inside lambda scopes (Java 17)")
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
    public void testLocalClasses() throws Exception {
        final var testClass = this.context().getProvider().findClass("scenario07/TestClass");
        assertNotNull(testClass);

        final MethodData testMethod = testClass.method("test", parseDescriptor("()V"));
        assertNotNull(testMethod);

        final List<MethodClosure<MethodData>> firstLambdas = testMethod.get(HypoHydration.LAMBDA_CALLS);
        assertNotNull(firstLambdas);
        assertEquals(1, firstLambdas.size());
        final MethodClosure<MethodData> firstLambda = firstLambdas.get(0);

        final List<MethodClosure<MethodData>> secondLambdas = firstLambda.getClosure().get(HypoHydration.LAMBDA_CALLS);
        assertNotNull(secondLambdas);
        assertEquals(2, secondLambdas.size());
        final MethodClosure<MethodData> secondLambda = secondLambdas.stream().filter(l -> l.getContainingMethod().equals(firstLambda.getClosure())).findFirst().orElse(null);
        assertNotNull(secondLambda);

        final List<MethodClosure<MethodData>> thirdLambdas = secondLambda.getClosure().get(HypoHydration.LAMBDA_CALLS);
        assertNotNull(thirdLambdas);
        assertEquals(2, thirdLambdas.size());
        final MethodClosure<MethodData> thirdLambda = thirdLambdas.stream().filter(l -> l.getContainingMethod().equals(secondLambda.getClosure())).findFirst().orElse(null);
        assertNotNull(thirdLambda);

        final List<MethodClosure<ClassData>> locals = thirdLambda.getClosure().get(HypoHydration.LOCAL_CLASSES);
        assertNotNull(locals);
        assertEquals(1, locals.size());
        final MethodClosure<ClassData> local = locals.get(0);
        assertNotNull(local);

        assertArrayEquals(new int[] { 0, 2, 3, 4 }, firstLambda.getParamLvtIndices());
        assertArrayEquals(new int[] { 0, 1, 2, 3 }, secondLambda.getParamLvtIndices());
        assertArrayEquals(new int[] { 0, 1, 2, 3 }, thirdLambda.getParamLvtIndices());
        assertArrayEquals(new int[] { 1, 2, 3 }, local.getParamLvtIndices());
    }
}
