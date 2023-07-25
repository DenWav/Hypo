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
import dev.denwav.hypo.hydrate.HydrationProvider;
import dev.denwav.hypo.hydrate.generic.HypoHydration;
import dev.denwav.hypo.hydrate.generic.MethodClosure;
import dev.denwav.hypo.model.data.MethodData;
import dev.denwav.hypo.test.framework.TestScenarioBase;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static dev.denwav.hypo.model.data.MethodDescriptor.parseDescriptor;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("resource")
@DisplayName("[asm] Scenario 05 - Lambda scopes (Java 17)")
public class Scenario05Test extends TestScenarioBase {

    @Override
    public @NotNull Env env() {
        return new Env() {
            @Override
            public @NotNull String forContext() {
                return "scenario-05";
            }

            @Override
            public @NotNull Iterable<HydrationProvider<?>> hydration() {
                return List.of(LambdaCallHydrator.create());
            }
        };
    }

    @Test
    @DisplayName("Test lambda call hydrator")
    public void testLambdaCalls() throws Exception {
        final var testClass = this.context().getProvider().findClass("scenario05/TestClass");
        assertNotNull(testClass);

        final MethodData testMethod = testClass.method("test", parseDescriptor("()V"));
        assertNotNull(testMethod);

        final List<MethodClosure<MethodData>> methodClosures = testMethod.get(HypoHydration.LAMBDA_CALLS);
        assertNotNull(methodClosures);
        assertEquals(1, methodClosures.size());

        final MethodClosure<MethodData> methodClosure = methodClosures.get(0);
        assertNotNull(methodClosure);
        final MethodData call = methodClosure.getClosure();
        assertNotNull(call);
        assertArrayEquals(new int[] { 1, 2, 4 }, methodClosure.getParamLvtIndices());

        assertEquals(testClass, call.parentClass());
        assertTrue(call.isSynthetic());
        assertTrue(call.isStatic());

        final List<MethodClosure<MethodData>> nestedCalls = call.get(HypoHydration.LAMBDA_CALLS);
        assertNotNull(nestedCalls);
        assertEquals(3, nestedCalls.size());

        final MethodClosure<MethodData> firstNestedMethodClosure = nestedCalls.stream()
            .filter(c -> c.getClosure().name().equals("thing"))
            .findFirst()
            .orElse(null);
        assertNotNull(firstNestedMethodClosure);
        final MethodData firstNestedCall = firstNestedMethodClosure.getClosure();
        assertNotNull(firstNestedCall);
        assertEquals(testClass, firstNestedCall.parentClass());
        // this is a method reference
        assertFalse(firstNestedCall.isSynthetic());
        assertTrue(firstNestedCall.isStatic());

        final MethodClosure<MethodData> secondNestedMethodClosure = nestedCalls.stream()
            .filter(c -> !c.getClosure().name().equals("thing"))
            .filter(c -> !c.getContainingMethod().name().equals("test"))
            .findFirst()
            .orElse(null);
        assertNotNull(secondNestedMethodClosure);
        final MethodData secondNestedCall = secondNestedMethodClosure.getClosure();
        assertNotNull(secondNestedCall);
        assertEquals(testClass, secondNestedCall.parentClass());
        assertTrue(secondNestedCall.isSynthetic());
        assertTrue(secondNestedCall.isStatic());

        final MethodClosure<MethodData> outerNestedMethodClosure = nestedCalls.stream()
            .filter(c -> c.getClosure().name().equals("thing"))
            .findFirst()
            .orElse(null);
        assertNotNull(outerNestedMethodClosure);
    }
}
