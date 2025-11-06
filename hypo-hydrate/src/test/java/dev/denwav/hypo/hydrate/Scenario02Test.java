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

package dev.denwav.hypo.hydrate;

import dev.denwav.hypo.model.data.ClassData;
import dev.denwav.hypo.model.data.MethodData;
import dev.denwav.hypo.test.framework.TestScenarioBase;
import dev.denwav.hypo.types.desc.MethodDescriptor;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("[hydrate] Scenario 02 - Overrides with overloads (Java 21)")
class Scenario02Test extends TestScenarioBase {

    @Override
    public @NotNull Env env() {
        return () -> "scenario-02";
    }

    @Test
    @DisplayName("Test overload 1/4")
    void testNoArgs() {
        final ClassData testClass = this.findClass("scenario02.TestClass");
        final ClassData testSuperClass = this.findClass("scenario02.TestSuperClass");

        final MethodData testMethod = findMethod(testClass, "test", "()V");
        final MethodData expectedTestSuperMethod = findMethod(testSuperClass, "test", "()V");

        final MethodData actualSuperTestMethod = testMethod.superMethod();
        assertNotNull(actualSuperTestMethod);

        assertEquals(expectedTestSuperMethod, actualSuperTestMethod);
    }

    @Test
    @DisplayName("Test overload 2/4")
    void testInt() {
        final ClassData testClass = this.findClass("scenario02.TestClass");
        final ClassData testSuperClass = this.findClass("scenario02.TestSuperClass");

        final MethodData testMethod = findMethod(testClass, "test", "(I)V");
        final MethodData expectedTestSuperMethod = findMethod(testSuperClass, "test", "(I)V");

        final MethodData actualSuperTestMethod = testMethod.superMethod();
        assertNotNull(actualSuperTestMethod);

        assertEquals(expectedTestSuperMethod, actualSuperTestMethod);
    }

    @Test
    @DisplayName("Test overload 3/4")
    void testTwoInts() {
        final ClassData testClass = this.findClass("scenario02.TestClass");
        final ClassData testSuperClass = this.findClass("scenario02.TestSuperClass");

        final MethodData testMethod = findMethod(testClass, "test", "(II)V");
        final MethodData expectedTestSuperMethod = findMethod(testSuperClass, "test", "(II)V");

        final MethodData actualSuperTestMethod = testMethod.superMethod();
        assertNotNull(actualSuperTestMethod);

        assertEquals(expectedTestSuperMethod, actualSuperTestMethod);
    }

    @Test
    @DisplayName("Test overload 4/4")
    void testNoOverride() {
        final ClassData testClass = this.findClass("scenario02.TestClass");
        final ClassData testSuperClass = this.findClass("scenario02.TestSuperClass");

        final MethodData testMethod = findMethod(testSuperClass, "test", "(J)V");
        assertEquals(0, testMethod.childMethods().size());

        final MethodData actualSuperTestMethod = testClass.method("test", MethodDescriptor.parse("(J)V"));
        assertNull(actualSuperTestMethod);
    }
}
