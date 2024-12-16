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
import dev.denwav.hypo.hydrate.generic.LambdaClosure;
import dev.denwav.hypo.model.data.MethodData;
import dev.denwav.hypo.test.framework.TestScenarioBase;
import dev.denwav.hypo.types.desc.MethodDescriptor;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("[asm] Scenario 05 - Lambda scopes (Java 21)")
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

            @Override
            public boolean includeJdk() {
                return true;
            }
        };
    }

    private MethodData runnableRun;
    private MethodData functionApply;

    @BeforeEach
    public void setupRunnable() {
        this.runnableRun = this.findClass("java/lang/Runnable")
            .method("run", MethodDescriptor.parse("()V"));
        this.functionApply = this.findClass("java/util/function/Function")
            .method("apply", MethodDescriptor.parse("(Ljava/lang/Object;)Ljava/lang/Object;"));
    }

    @Test
    @DisplayName("Test lambda call hydrator")
    public void testLambdaCalls() {
        final var testClass = this.findClass("scenario05/TestClass");
        assertNotNull(testClass);

        final MethodData testMethod = testClass.method("test", MethodDescriptor.parse("()V"));
        assertNotNull(testMethod);

        final List<LambdaClosure> methodClosures = testMethod.get(HypoHydration.LAMBDA_CALLS);
        assertNotNull(methodClosures);
        assertEquals(1, methodClosures.size());

        final LambdaClosure methodClosure = methodClosures.getFirst();
        assertNotNull(methodClosure);
        assertEquals(this.runnableRun, methodClosure.getInterfaceMethod());
        final MethodData call = methodClosure.getLambda();
        assertNotNull(call);
        assertArrayEquals(new int[] { 0, 1, 2, 4 }, methodClosure.getParamLvtIndices());

        assertEquals(testClass, call.parentClass());
        assertTrue(call.isSynthetic());
        assertFalse(call.isStatic());

        final List<LambdaClosure> nestedCalls = call.get(HypoHydration.LAMBDA_CALLS);
        assertNotNull(nestedCalls);
        assertEquals(3, nestedCalls.size());

        final LambdaClosure firstNestedMethodClosure = nestedCalls.stream()
            .filter(c -> c.getLambda().name().equals("thing"))
            .findFirst()
            .orElse(null);
        assertNotNull(firstNestedMethodClosure);
        final MethodData firstNestedCall = firstNestedMethodClosure.getLambda();
        assertNotNull(firstNestedCall);
        assertEquals(testClass, firstNestedCall.parentClass());
        // this is a method reference
        assertFalse(firstNestedCall.isSynthetic());
        assertTrue(firstNestedCall.isStatic());

        final LambdaClosure secondNestedMethodClosure = nestedCalls.stream()
            .filter(c -> !c.getLambda().name().equals("thing"))
            .filter(c -> !c.getContainingMethod().name().equals("test"))
            .findFirst()
            .orElse(null);
        assertNotNull(secondNestedMethodClosure);
        final MethodData secondNestedCall = secondNestedMethodClosure.getLambda();
        assertNotNull(secondNestedCall);
        assertEquals(testClass, secondNestedCall.parentClass());
        assertTrue(secondNestedCall.isSynthetic());
        assertTrue(secondNestedCall.isStatic());

        final LambdaClosure outerNestedMethodClosure = nestedCalls.stream()
            .filter(c -> c.getLambda().name().equals("thing"))
            .findFirst()
            .orElse(null);
        assertNotNull(outerNestedMethodClosure);
    }

    @Test
    @DisplayName("Test lambda call hydrator on statics")
    public void testStaticLambdaCalls() {
        final var testClass = this.findClass("scenario05/TestClass");
        assertNotNull(testClass);

        final MethodData testMethod = testClass.method("testStatic", MethodDescriptor.parse("()V"));
        assertNotNull(testMethod);

        final List<LambdaClosure> methodClosures = testMethod.get(HypoHydration.LAMBDA_CALLS);
        assertNotNull(methodClosures);
        assertEquals(1, methodClosures.size());

        final LambdaClosure methodClosure = methodClosures.getFirst();
        assertNotNull(methodClosure);
        final MethodData call = methodClosure.getLambda();
        assertNotNull(call);
        assertArrayEquals(new int[] { 0 }, methodClosure.getParamLvtIndices());

        assertEquals(testClass, call.parentClass());
        assertTrue(call.isSynthetic());
        assertTrue(call.isStatic());
    }

    @Test
    @DisplayName("Test lambda call hydrator collecting interface method")
    public void testFunction() {
        final var testClass = this.findClass("scenario05/TestClass");
        assertNotNull(testClass);

        final MethodData testMethod = testClass.method("testFunction", MethodDescriptor.parse("()V"));
        assertNotNull(testMethod);

        final List<LambdaClosure> lambdas = testMethod.get(HypoHydration.LAMBDA_CALLS);
        assertNotNull(lambdas);

        final LambdaClosure lambda = lambdas.getFirst();
        assertNotNull(lambda);

        assertEquals(testMethod, lambda.getContainingMethod());

        assertEquals(this.functionApply, lambda.getInterfaceMethod());
    }
}
