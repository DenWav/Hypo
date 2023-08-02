package dev.denwav.hypo.asm.scenarios;

import dev.denwav.hypo.model.data.ClassData;
import dev.denwav.hypo.model.data.MethodData;
import dev.denwav.hypo.test.framework.TestScenarioBase;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("[asm] Scenario 10 - Local class static detection (Java 17)")
public class Scenario10Test extends TestScenarioBase {

    @Override
    public @NotNull Env env() {
        return () -> "scenario-10";
    }

    @Test
    @DisplayName("Test isStaticInnerClass() in instance method")
    public void testIsStaticInInstanceMethod() {
        final var testClass = this.findClass("scenario10/TestClass$1LocalClass");
        final var testRecord = this.findClass("scenario10/TestClass$1LocalRecord");
        final var testEnum = this.findClass("scenario10/TestClass$1LocalEnum");

        assertFalse(testClass.isStaticInnerClass());
        assertTrue(testRecord.isStaticInnerClass());
        assertTrue(testEnum.isStaticInnerClass());
    }

    @Test
    @DisplayName("Test isStaticInnerClass() in instance method")
    public void testIsStaticInStaticMethod() {
        final var testClass = this.findClass("scenario10/TestClass$2LocalClass");
        final var testRecord = this.findClass("scenario10/TestClass$2LocalRecord");
        final var testEnum = this.findClass("scenario10/TestClass$2LocalEnum");

        assertTrue(testClass.isStaticInnerClass());
        assertTrue(testRecord.isStaticInnerClass());
        assertTrue(testEnum.isStaticInnerClass());
    }
}
