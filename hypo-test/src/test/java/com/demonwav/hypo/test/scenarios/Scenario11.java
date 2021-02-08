/*
 * Hypo, an extensible and pluggable Java bytecode analytical model.
 *
 * Copyright (C) 2021  Kyle Wood (DemonWav)
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

package com.demonwav.hypo.test.scenarios;

import com.demonwav.hypo.asm.hydrate.BridgeMethodHydrator;
import com.demonwav.hypo.hydrate.HydrationProvider;
import com.demonwav.hypo.mappings.contributors.ChangeContributor;
import com.demonwav.hypo.mappings.contributors.CopyMappingsDown;
import com.demonwav.hypo.test.framework.TestScenarioBase;
import java.util.List;
import org.cadixdev.lorenz.MappingSet;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Scenario 11 - Complex Generics")
public class Scenario11 extends TestScenarioBase {

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
    @DisplayName("Test multiple levels of complex generic classes")
    void testComplexGenerics() throws Exception {
        this.runTest(new TestCase() {
            @Override
            public @NotNull Iterable<ChangeContributor> changeContributors() {
                return List.of(CopyMappingsDown.create());
            }

            @Override
            public @NotNull MappingSet startMappings() {
                return parseTsrg("""
                    scenario11/ParentClass scenario11/ParentClass
                        getWith (Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object; getObjectWith
                    """);
            }

            @Override
            public @NotNull MappingSet finishMappings() {
                return parseTsrg("""
                    scenario11/ParentClass scenario11/ParentClass
                        getWith (Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object; getObjectWith
                    scenario11/ChildClass scenario11/ChildClass
                        getWith (Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object; getObjectWith
                        getWith (Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/String; getObjectWith
                    scenario11/GrandChildClass scenario11/GrandChildClass
                        getWith (Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object; getObjectWith
                        getWith (Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/String; getObjectWith
                        getWith (Ljava/lang/Integer;Ljava/lang/Object;)Ljava/lang/String; getObjectWith
                    """);
            }
        });
    }
}
