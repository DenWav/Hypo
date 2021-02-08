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

import com.demonwav.hypo.asm.hydrate.SuperConstructorHydrator;
import com.demonwav.hypo.hydrate.HydrationProvider;
import com.demonwav.hypo.mappings.contributors.ChangeContributor;
import com.demonwav.hypo.mappings.contributors.CopyMappingsDown;
import com.demonwav.hypo.test.framework.TestScenarioBase;
import java.util.List;
import org.cadixdev.lorenz.MappingSet;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Scenario 09 - Mixture Constructor Calls")
public class Scenario09 extends TestScenarioBase {

    @Override
    public @NotNull Env env() {
        return new Env() {
            @Override
            public @NotNull String forContext() {
                return "scenario-09";
            }

            @Override
            public @NotNull Iterable<HydrationProvider<?>> hydration() {
                return List.of(SuperConstructorHydrator.create());
            }
        };
    }

    @Test
    @DisplayName("Test mixing this() & super() calls")
    void testMixingConstructorCalls() throws Exception {
        this.runTest(new TestCase() {
            @Override
            public @NotNull Iterable<ChangeContributor> changeContributors() {
                return List.of(CopyMappingsDown.create());
            }

            @Override
            public @NotNull MappingSet startMappings() {
                return parseTiny("""
                    c    scenario09/ParentClass    scenario09/ParentClass
                        m    (II)V    <init>    <init>
                            p    1        i
                            p    2        j
                    """);
            }

            @Override
            public @NotNull MappingSet finishMappings() {
                return parseTiny("""
                    c    scenario09/ParentClass    scenario09/ParentClass
                        m    (II)V    <init>    <init>
                            p    1        i
                            p    2        j
                        m    (JJ)V    <init>    <init>
                            p    1        i
                            p    3        j
                    c    scenario09/ChildClass    scenario09/ChildClass
                        m    (JJ)V    <init>    <init>
                            p    1        i
                            p    3        j
                        m    (DD)V    <init>    <init>
                            p    1        i
                            p    3        j
                    """);
            }
        });
    }
}
