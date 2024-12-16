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

@DisplayName("[asm] Scenario 06 - Local class scopes (Java 21)")
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
    public void testLocalClasses() {
        final var testClass = this.findClass("scenario06/TestClass");
        assertNotNull(testClass);

        final MethodData testMethod = testClass.method("test", MethodDescriptor.parse("()V"));
        assertNotNull(testMethod);

        final List<LocalClassClosure> localClasses = testMethod.get(HypoHydration.LOCAL_CLASSES);
        assertNotNull(localClasses);
        assertEquals(3, localClasses.size());

        final LocalClassClosure firstAnon = localClasses.getFirst();
        assertNotNull(firstAnon);
        assertEquals(testMethod, firstAnon.getContainingMethod());
        assertEquals("scenario06/TestClass$1", firstAnon.getLocalClass().name());
        assertEquals(0, firstAnon.getParamLvtIndices().length);

        final LocalClassClosure secondAnon = localClasses.get(1);
        assertNotNull(secondAnon);
        assertEquals(testMethod, secondAnon.getContainingMethod());
        assertEquals("scenario06/TestClass$2", secondAnon.getLocalClass().name());
        assertArrayEquals(new int [] { 1, 2 }, secondAnon.getParamLvtIndices());

        final LocalClassClosure firstLocal = localClasses.get(2);
        assertNotNull(firstLocal);
        assertEquals(testMethod, firstLocal.getContainingMethod());
        assertEquals("scenario06/TestClass$1LocalClass", firstLocal.getLocalClass().name());
        assertArrayEquals(new int [] { 1, 2 }, firstLocal.getParamLvtIndices());
    }
}
