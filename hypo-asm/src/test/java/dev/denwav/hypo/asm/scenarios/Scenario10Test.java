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
import dev.denwav.hypo.model.data.MethodData;
import dev.denwav.hypo.model.data.MethodDescriptor;
import dev.denwav.hypo.test.framework.TestScenarioBase;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("[asm] Scenario 10 - BridgeMethodHydrator")
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
                return List.of(BridgeMethodHydrator.create());
            }
        };
    }

    @Test
    public void testBridgeMethodHydrator() {
        final var testClass = this.findClass("scenario10/TestClass");

        final var methods = testClass.methods("compareTo");
        Assertions.assertEquals(2, methods.size(), "expected 2 compareTo methods");
        final MethodData concrete = methods.get(0);
        final MethodData synthetic = methods.get(1);
        Assertions.assertFalse(concrete.isSynthetic(), concrete + " is marked as synthetic");
        Assertions.assertTrue(synthetic.isSynthetic(), synthetic + " isn't marked as synthetic");

        final List<MethodData> sources = concrete.get(HypoHydration.SYNTHETIC_SOURCE);
        Assertions.assertNotNull(sources);
        Assertions.assertTrue(sources.contains(synthetic), "methods don't link to each other");
        Assertions.assertEquals(concrete, synthetic.get(HypoHydration.SYNTHETIC_TARGET), "methods don't link to each other");
    }

    @Test
    public void testSubclassBridgeMethodHydrator() {
        final var testClass = this.findClass("scenario10/TestClass");
        final var testSubClass = this.findClass("scenario10/TestClass$SubClass");

        final var methods = testSubClass.methods("compareTo");
        Assertions.assertEquals(1, methods.size(), "expected 1 compareTo methods");
        Assertions.assertEquals("(Ljava/lang/Object;)I", methods.get(0).descriptorText());

        final var concrete = testClass.method("compareTo", MethodDescriptor.parseDescriptor("(Lscenario10/TestClass;)I"));
        Assertions.assertNotNull(concrete);
        // final MethodData concrete = methods.get(0);
        final MethodData synthetic = methods.get(0);
        Assertions.assertFalse(concrete.isSynthetic(), concrete + " is marked as synthetic");
        Assertions.assertTrue(synthetic.isSynthetic(), synthetic + " isn't marked as synthetic");

        final List<MethodData> sources = concrete.get(HypoHydration.SYNTHETIC_SOURCE);
        Assertions.assertNotNull(sources);
        Assertions.assertTrue(sources.contains(synthetic), "methods don't link to each other");
        Assertions.assertEquals(concrete, synthetic.get(HypoHydration.SYNTHETIC_TARGET), "methods don't link to each other");    }
}
