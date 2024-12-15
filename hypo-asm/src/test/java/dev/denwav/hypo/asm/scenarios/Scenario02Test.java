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

import dev.denwav.hypo.model.data.ClassData;
import dev.denwav.hypo.test.framework.TestScenarioBase;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("[asm] Scenario 02 - Inner classes (Java 21)")
public class Scenario02Test extends TestScenarioBase {

    @Override
    public @NotNull Env env() {
        return () -> "scenario-02";
    }

    @Test
    @DisplayName("Test inner classes")
    void testInnerClasses() {
        final var testClass = this.findClass("scenario02.TestClass");
        Assertions.assertNotNull(testClass);

        final var innerClasses = testClass.innerClasses();

        final var innerNames = innerClasses.stream().map(ClassData::name).sorted().toList();
        final var expectedNames = List.of(
            "scenario02/TestClass$1",
            "scenario02/TestClass$1LocalClass",
            "scenario02/TestClass$InnerClass",
            "scenario02/TestClass$StaticInnerClass"
        );

        Assertions.assertEquals(expectedNames, innerNames);
    }

    @Test
    @DisplayName("Test nested inner classes")
    void testNestedInnerClasses() {
        final var testClass = this.findClass("scenario02.TestClass$InnerClass");
        Assertions.assertNotNull(testClass);

        final var innerClasses = testClass.innerClasses();

        final var innerNames = innerClasses.stream().map(ClassData::name).sorted().toList();
        final var expectedNames = List.of(
            "scenario02/TestClass$InnerClass$1",
            "scenario02/TestClass$InnerClass$1LocalClass",
            "scenario02/TestClass$InnerClass$NestedInnerClass"
        );

        Assertions.assertEquals(expectedNames, innerNames);
    }

    @Test
    @DisplayName("Test double nested inner classes")
    void testDoubleNestedInnerClasses() {
        final var testClass = this.findClass("scenario02.TestClass$InnerClass$NestedInnerClass");
        Assertions.assertNotNull(testClass);

        final var innerClasses = testClass.innerClasses();

        final var innerNames = innerClasses.stream().map(ClassData::name).sorted().toList();
        final var expectedNames = List.of(
            "scenario02/TestClass$InnerClass$NestedInnerClass$1",
            "scenario02/TestClass$InnerClass$NestedInnerClass$1LocalClass"
        );

        Assertions.assertEquals(expectedNames, innerNames);
    }

    @Test
    @DisplayName("Test static inner classes")
    void testStaticInnerClasses() {
        final var testClass = this.findClass("scenario02.TestClass$StaticInnerClass");
        Assertions.assertNotNull(testClass);

        final var innerClasses = testClass.innerClasses();

        final var innerNames = innerClasses.stream().map(ClassData::name).sorted().toList();
        final var expectedNames = List.of(
            "scenario02/TestClass$StaticInnerClass$1",
            "scenario02/TestClass$StaticInnerClass$1LocalClass"
        );

        Assertions.assertEquals(expectedNames, innerNames);
    }
}
