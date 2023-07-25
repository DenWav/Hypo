package dev.denwav.hypo.asm.scenarios;

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
@DisplayName("[asm] Scenario 06 - Local class scopes (Java 17)")
public class Scenario06Test extends TestScenarioBase {

    @Override
    public @NotNull Env env() {
        return new Env() {
            @Override
            public @NotNull String forContext() {
                return "scenario-06";
            }

            @Override
            public @NotNull Iterable<HydrationProvider<?>> hydration() {
                return List.of(LocalClassHydrator.create());
            }
        };
    }

    @Test
    @DisplayName("Test local classes hydrator")
    public void testLocalClasses() throws Exception {
        final var testClass = this.context().getProvider().findClass("scenario06/TestClass");
        assertNotNull(testClass);

        final MethodData testMethod = testClass.method("test", parseDescriptor("()V"));
        assertNotNull(testMethod);

        final List<MethodClosure<ClassData>> localClasses = testMethod.get(HypoHydration.LOCAL_CLASSES);
        assertNotNull(localClasses);
        assertEquals(3, localClasses.size());

        final MethodClosure<ClassData> firstAnon = localClasses.get(0);
        assertNotNull(firstAnon);
        assertEquals(testMethod, firstAnon.getContainingMethod());
        assertEquals("scenario06/TestClass$1", firstAnon.getClosure().name());
        assertEquals(0, firstAnon.getParamLvtIndices().length);

        final MethodClosure<ClassData> secondAnon = localClasses.get(1);
        assertNotNull(secondAnon);
        assertEquals(testMethod, secondAnon.getContainingMethod());
        assertEquals("scenario06/TestClass$2", secondAnon.getClosure().name());
        assertArrayEquals(new int [] { 1, 2 }, secondAnon.getParamLvtIndices());

        final MethodClosure<ClassData> firstLocal = localClasses.get(2);
        assertNotNull(firstLocal);
        assertEquals(testMethod, firstLocal.getContainingMethod());
        assertEquals("scenario06/TestClass$1LocalClass", firstLocal.getClosure().name());
        assertArrayEquals(new int [] { 1, 2 }, firstLocal.getParamLvtIndices());
    }
}
