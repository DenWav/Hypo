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

package dev.denwav.hypo.test.scenarios;

import dev.denwav.hypo.mappings.contributors.ChangeContributor;
import dev.denwav.hypo.mappings.contributors.CopyMappingsDown;
import dev.denwav.hypo.mappings.contributors.PropagateMappingsUp;
import dev.denwav.hypo.mappings.contributors.RemoveUnusedMappings;
import dev.denwav.hypo.test.framework.TestScenarioBase;
import java.util.List;
import org.cadixdev.lorenz.MappingSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("[integration] Scenario 01 - Simple Hierarchy")
public class Scenario01Test extends TestScenarioBase {

    @Override
    public @NotNull Env env() {
        return () -> "scenario-01";
    }

    @Test
    @DisplayName("Test copying mappings down")
    void testTopDown() throws Exception {
        this.runTest(new TestCase() {
            @Override
            public @NotNull Iterable<ChangeContributor> changeContributors() {
                return List.of(CopyMappingsDown.create());
            }

            @Override
            public @NotNull MappingSet startMappings() {
                return parseTsrg("""
                    scenario01/ParentClass scenario01/ParentClass
                        method ()V methodNew
                    """);
            }

            @Override
            public @NotNull MappingSet finishMappings() {
                return parseTsrg("""
                    scenario01/ParentClass scenario01/ParentClass
                        method ()V methodNew
                    scenario01/ChildClass01 scenario01/ChildClass01
                        method ()V methodNew
                    scenario01/ChildClass02 scenario01/ChildClass02
                        method ()V methodNew
                    """);
            }
        });
    }

    @Test
    @DisplayName("Test copying mappings over bad mappings")
    void testBadSubMappings() throws Exception {
        this.runTest(new TestCase() {
            @Override
            public @NotNull Iterable<ChangeContributor> changeContributors() {
                return List.of(CopyMappingsDown.create());
            }

            @Override
            public @NotNull MappingSet startMappings() {
                return parseTsrg("""
                    scenario01/ParentClass scenario01/ParentClass
                        method ()V methodNew
                    scenario01/ChildClass01 scenario01/ChildClass01
                        method ()V otherMethodNew # bad mapping
                    scenario01/ChildClass02 scenario01/ChildClass02
                        method ()V thirdMethodNew # bad mapping
                    """);
            }

            @Override
            public @NotNull MappingSet finishMappings() {
                return parseTsrg("""
                    scenario01/ParentClass scenario01/ParentClass
                        method ()V methodNew
                    scenario01/ChildClass01 scenario01/ChildClass01
                        method ()V methodNew
                    scenario01/ChildClass02 scenario01/ChildClass02
                        method ()V methodNew
                    """);
            }
        });
    }

    @Test
    @DisplayName("Test propagating mappings up & back down")
    void treePropagation() throws Exception {
        this.runTest(new TestCase() {
            @Override
            public @NotNull Iterable<ChangeContributor> changeContributors() {
                return List.of(PropagateMappingsUp.create(), CopyMappingsDown.create());
            }

            @Override
            public @NotNull MappingSet startMappings() {
                return parseTsrg("""
                    scenario01/ChildClass01 scenario01/ChildClass01
                        method ()V methodNew
                    """);
            }

            @Override
            @SuppressWarnings("MultipleNullnessAnnotations")
            public @NotNull MappingSet @Nullable [] stageMappings() {
                return new MappingSet[] {
                    // First stage, propagate up
                    parseTsrg("""
                        scenario01/ParentClass scenario01/ParentClass
                            method ()V methodNew
                        """),
                    // Second stage, copy down
                    parseTsrg("""
                        scenario01/ParentClass scenario01/ParentClass
                            method ()V methodNew
                        scenario01/ChildClass01 scenario01/ChildClass01
                            method ()V methodNew
                        scenario01/ChildClass02 scenario01/ChildClass02
                            method ()V methodNew
                        """),
                };
            }
        });
    }

    @Test
    @DisplayName("Test removing unused mappings")
    void testRemovingUnused() throws Exception {
        this.runTest(new TestCase() {
            @Override
            public @NotNull Iterable<ChangeContributor> changeContributors() {
                return List.of(RemoveUnusedMappings.create(), CopyMappingsDown.create());
            }

            @Override
            public @NotNull MappingSet startMappings() {
                return parseTsrg("""
                    scenario01/ParentClass scenario01/ParentClass
                        method ()V methodNew
                        method2 ()V methodNew2 # does not exist
                    scenario01/ChildClass01 scenario01/ChildClass01
                        method ()I methodNew # does not exist
                    """);
            }

            @Override
            public @NotNull MappingSet finishMappings() {
                return parseTsrg("""
                    scenario01/ParentClass scenario01/ParentClass
                        method ()V methodNew
                    scenario01/ChildClass01 scenario01/ChildClass01
                        method ()V methodNew
                    scenario01/ChildClass02 scenario01/ChildClass02
                        method ()V methodNew
                    """);
            }
        });
    }

    @Test
    @DisplayName("Test removing unused class mappings")
    void testRemovingUnusedClassMappings() throws Exception {
        this.runTest(new TestCase() {
            @Override
            public @NotNull Iterable<ChangeContributor> changeContributors() {
                return List.of(RemoveUnusedMappings.create());
            }

            @Override
            public @NotNull MappingSet startMappings() {
                return parseTsrg("""
                    scenario01/NonExistClass scenario01/NewName
                        method ()V methodNew
                    scenario01/ChildClass01 scenario01/ChildClass01
                        method ()V methodNew
                    """);
            }

            @Override
            public @NotNull MappingSet finishMappings() {
                return parseTsrg("""
                    scenario01/ChildClass01 scenario01/ChildClass01
                        method ()V methodNew
                    """);
            }
        });
    }

    @Test
    @DisplayName("Test removing unused inner class mappings")
    void testRemovingUnusedInnerClassMappings() throws Exception {
        this.runTest(new TestCase() {
            @Override
            public @NotNull Iterable<ChangeContributor> changeContributors() {
                return List.of(RemoveUnusedMappings.create());
            }

            @Override
            public @NotNull MappingSet startMappings() {
                return parseTsrg("""
                    scenario01/ParentClass scenario01/NewParentName
                    scenario01/ParentClass$NotExists scenario01/NewParentName$NotExistsNewName
                    """);
            }

            @Override
            public @NotNull MappingSet finishMappings() {
                return parseTsrg("""
                    scenario01/ParentClass scenario01/NewParentName
                    """);
            }
        });
    }

    @Test
    @DisplayName("Test removing unused param mappings")
    void testRemovingParamMappings() throws Exception {
        this.runTest(new TestCase() {
            @Override
            public @NotNull Iterable<ChangeContributor> changeContributors() {
                return List.of(RemoveUnusedMappings.create());
            }

            @Override
            public @NotNull MappingSet startMappings() {
                return parseTiny("""
                    c    scenario01/ParentClass    scenario01/ParentClass
                        m    ()V    method    newName
                            p    1        notExist
                    """);
            }

            @Override
            public @NotNull MappingSet finishMappings() {
                return parseTiny("""
                    c    scenario01/ParentClass    scenario01/ParentClass
                        m    ()V    method    newName
                    """);
            }
        });
    }
}
