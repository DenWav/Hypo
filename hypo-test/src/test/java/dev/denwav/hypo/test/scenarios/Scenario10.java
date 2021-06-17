/*
 * Hypo, an extensible and pluggable Java bytecode analytical model.
 *
 * Copyright (C) 2021  Kyle Wood (DenWav)
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

package dev.denwav.hypo.test.scenarios;

import dev.denwav.hypo.asm.hydrate.BridgeMethodHydrator;
import dev.denwav.hypo.hydrate.HydrationProvider;
import dev.denwav.hypo.mappings.contributors.ChangeContributor;
import dev.denwav.hypo.mappings.contributors.CopyMappingsDown;
import dev.denwav.hypo.test.framework.TestScenarioBase;
import java.util.List;
import org.cadixdev.lorenz.MappingSet;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("[integration] Scenario 10 - Generics")
public class Scenario10 extends TestScenarioBase {

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
    @DisplayName("Test covariant return via generics")
    void testGenericMethod() throws Exception {
        this.runTest(new TestCase() {
            @Override
            public @NotNull Iterable<ChangeContributor> changeContributors() {
                return List.of(CopyMappingsDown.create());
            }

            @Override
            public @NotNull MappingSet startMappings() {
                return parseTsrg("""
                    scenario10/ParentClass scenario10/ParentClass
                        get ()Ljava/lang/Object; getObject
                    """);
            }

            @Override
            public @NotNull MappingSet finishMappings() {
                return parseTsrg("""
                    scenario10/ParentClass scenario10/ParentClass
                        get ()Ljava/lang/Object; getObject
                    scenario10/ChildClass scenario10/ChildClass
                        get ()Ljava/lang/Object; getObject
                        get ()Ljava/lang/String; getObject
                    """);
            }
        });
    }
}
