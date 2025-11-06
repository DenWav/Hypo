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
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("[hydrate] Scenario 01 - Base Override (Java 21)")
class Scenario01Test extends TestScenarioBase {

    @Override
    public @NotNull Env env() {
        return () -> "scenario-01";
    }

    @Test
    @DisplayName("Test simple override")
    void hydrationTest() {
        final ClassData testClass = this.findClass("scenario01.TestClass");
        final ClassData testSuperClass = this.findClass("scenario01.TestSuperClass");

        final MethodData testMethod = findMethod(testClass, "test");
        final MethodData expectedTestSuperMethod = findMethod(testSuperClass, "test");

        final MethodData actualSuperTestMethod = testMethod.superMethod();
        assertNotNull(actualSuperTestMethod);

        assertEquals(expectedTestSuperMethod, actualSuperTestMethod);
    }
}
