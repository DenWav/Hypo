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

import dev.denwav.hypo.asm.hydrate.LocalClassHydrator;
import dev.denwav.hypo.hydrate.HydrationProvider;
import dev.denwav.hypo.hydrate.generic.HypoHydration;
import dev.denwav.hypo.hydrate.generic.LocalClassClosure;
import dev.denwav.hypo.test.framework.TestScenarioBase;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("[asm] Scenario 10 - Local class static detection (Java 17)")
public class Scenario10Test extends TestScenarioBase {

    @Override
    public @NotNull Env env() {
        return new Env() {
            @Override
            public @NotNull String forContext() {
                return "scenario-10";
            }

            @Override
            public @NotNull Iterable<HydrationProvider<?>> hydration() {
                return List.of(LocalClassHydrator.create());
            }
        };
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
    @DisplayName("Test isStaticInnerClass() in static method")
    public void testIsStaticInStaticMethod() {
        final var testClass = this.findClass("scenario10/TestClass$2LocalClass");
        final var testRecord = this.findClass("scenario10/TestClass$2LocalRecord");
        final var testEnum = this.findClass("scenario10/TestClass$2LocalEnum");

        assertTrue(testClass.isStaticInnerClass());
        assertTrue(testRecord.isStaticInnerClass());
        assertTrue(testEnum.isStaticInnerClass());
    }

    @Test
    @DisplayName("Test LocalClassHydrator field collection")
    public void testLocalClassHydratorFields() {
        final var testClass = this.findClass("scenario10/TestClass$1LocalClass");
        final List<LocalClassClosure> localClasses = testClass.require(HypoHydration.LOCAL_CLASSES);
        assertEquals(1, localClasses.size());
        final LocalClassClosure localClass = localClasses.get(0);

        assertArrayEquals(new int[] { 2, 3 }, localClass.getParamLvtIndices());
    }
}
