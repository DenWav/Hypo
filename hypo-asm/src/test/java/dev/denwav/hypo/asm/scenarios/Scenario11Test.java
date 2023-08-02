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

import dev.denwav.hypo.asm.hydrate.BridgeMethodHydrator;
import dev.denwav.hypo.hydrate.HydrationProvider;
import dev.denwav.hypo.hydrate.generic.HypoHydration;
import dev.denwav.hypo.model.data.ClassData;
import dev.denwav.hypo.model.data.MethodData;
import dev.denwav.hypo.test.framework.TestScenarioBase;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("[asm] - Abstract enum bridge method detection (Java 17)")
public class Scenario11Test extends TestScenarioBase {

    @Override
    public @NotNull Env env() {
        return new Env() {
            @Override
            public @NotNull String forContext() {
                return "scenario-11";
            }

            @Override
            public @NotNull Iterable<HydrationProvider<?>> hydration() {
                return List.of(BridgeMethodHydrator.create());
            }
        };
    }

    @Test
    @DisplayName("Test bridge method hydration on generic abstract enums")
    public void testBridgeMethod() {
        final ClassData enumData = this.findClass("scenario11/TestEnum");
        final ClassData oneImpl = this.findClass("scenario11/TestEnum$1");
        final ClassData twoImpl = this.findClass("scenario11/TestEnum$2");

        final MethodData baseTest = findMethod(enumData, "test", "(Ljava/lang/String;)Z");

        final MethodData synthTest = findMethod(enumData, "test", "(Ljava/lang/Object;)Z");
        final MethodData oneTest = findMethod(oneImpl, "test");
        final MethodData twoTest = findMethod(twoImpl, "test");

        //noinspection deprecation
        assertNotNull(baseTest.require(HypoHydration.SYNTHETIC_SOURCE));

        final Set<MethodData> sources = baseTest.require(HypoHydration.SYNTHETIC_SOURCES);
        assertEquals(Set.of(synthTest, oneTest, twoTest), sources);

        assertEquals(baseTest, synthTest.require(HypoHydration.SYNTHETIC_TARGET));
        assertEquals(baseTest, oneTest.require(HypoHydration.SYNTHETIC_TARGET));
        assertEquals(baseTest, twoTest.require(HypoHydration.SYNTHETIC_TARGET));
    }
}
