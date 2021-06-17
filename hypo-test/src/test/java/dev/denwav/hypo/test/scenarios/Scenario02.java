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

import dev.denwav.hypo.mappings.contributors.ChangeContributor;
import dev.denwav.hypo.mappings.contributors.CopyMappingsDown;
import dev.denwav.hypo.test.framework.TestScenarioBase;
import java.util.List;
import org.cadixdev.lorenz.MappingSet;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("[integration] Scenario 02 - Extended Hierarchy")
public class Scenario02 extends TestScenarioBase {

    @Override
    public @NotNull Env env() {
        return () -> "scenario-02";
    }

    @Test
    @DisplayName("Test copying mappings down extended hierarchy")
    void testTopDown() throws Exception {
        this.runTest(new TestCase() {
            @Override
            public @NotNull Iterable<ChangeContributor> changeContributors() {
                return List.of(CopyMappingsDown.create());
            }

            @Override
            public @NotNull MappingSet startMappings() {
                return parseTsrg("""
                    scenario02/ParentClass scenario02/ParentClass
                        method ()V methodNew
                    """);
            }

            @Override
            public @NotNull MappingSet finishMappings() {
                return parseTsrg("""
                    scenario02/ParentClass scenario02/ParentClass
                        method ()V methodNew
                    scenario02/ChildClass scenario02/ChildClass
                        method ()V methodNew
                    scenario02/GrandChildClass scenario02/GrandChildClass
                        method ()V methodNew
                    """);
            }
        });
    }

    @Test
    @DisplayName("Test copying mappings mid-hierarchy")
    void testMidDown() throws Exception {
        this.runTest(new TestCase() {
            @Override
            public @NotNull Iterable<ChangeContributor> changeContributors() {
                return List.of(CopyMappingsDown.create());
            }

            @Override
            public @NotNull MappingSet startMappings() {
                return parseTsrg("""
                    scenario02/ChildClass scenario02/ChildClass
                        method ()V methodNew
                    """);
            }

            @Override
            public @NotNull MappingSet finishMappings() {
                return parseTsrg("""
                    scenario02/ChildClass scenario02/ChildClass
                        method ()V methodNew
                    scenario02/GrandChildClass scenario02/GrandChildClass
                        method ()V methodNew
                    """);
            }
        });
    }
}
