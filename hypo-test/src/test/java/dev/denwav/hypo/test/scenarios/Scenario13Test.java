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

import dev.denwav.hypo.asm.hydrate.LambdaCallHydrator;
import dev.denwav.hypo.hydrate.HydrationProvider;
import dev.denwav.hypo.mappings.contributors.ChangeContributor;
import dev.denwav.hypo.mappings.contributors.CopyLambdaParametersDown;
import dev.denwav.hypo.test.framework.TestScenarioBase;
import java.util.List;
import org.cadixdev.lorenz.MappingSet;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("[integration] Scenario 13 - Inherited lambda paramter name mappings")
public class Scenario13Test extends TestScenarioBase {

    @Override
    public @NotNull Env env() {
        return new Env() {
            @Override
            public @NotNull String forContext() {
                return "scenario-13";
            }

            @Override
            public @NotNull Iterable<HydrationProvider<?>> hydration() {
                return List.of(LambdaCallHydrator.create());
            }
        };
    }

    @Test
    @DisplayName("Test lambda parameter mapping inheriting from interface method mapping")
    void testLambdaParameterMapping() throws Exception {
        this.runTest(new TestCase() {
            @Override
            public @NotNull Iterable<ChangeContributor> changeContributors() {
                return List.of(CopyLambdaParametersDown.create());
            }

            @Override
            public @NotNull MappingSet startMappings() {
                return parseTiny("""
                    c    scenario13/TestInterface    scenario13/TestInterface
                        m    (Ljava/lang/Object;Ljava/lang/Object;)V    apply    apply
                            p    1        param1
                            p    2        param2
                    """);
            }

            @Override
            public @NotNull MappingSet finishMappings() {
                return parseTiny("""
                    c    scenario13/TestClass    scenario13/TestClass
                        m    (Ljava/lang/String;Ljava/lang/String;)V    lambda$test$0    lambda$test$0
                            p    0        param1
                            p    1        param2
                        m    (Ljava/lang/Integer;Ljava/lang/Integer;)V    lambda$test$1    lambda$test$1
                            p    0        param1
                            p    1        param2
                        m    (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Double;)V    lambda$test$2    lambda$test$2
                            p    2        param1
                            p    3        param2
                        m    (Ljava/lang/String;Ljava/lang/Class;)V    lambda$test$3    lambda$test$3
                            p    1        param1
                            p    2        param2
                        m    (Ljava/lang/String;Ljava/lang/String;Ljava/lang/Class;)V    lambda$test$4    lambda$test$4
                            p    2        param1
                            p    3        param2
                        m    (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Class;)V	lambda$test$5	lambda$test$5
                            p    3        param1
                            p    4        param2
                    c    scenario13/TestInterface    scenario13/TestInterface
                        m    (Ljava/lang/Object;Ljava/lang/Object;)V    apply    apply
                            p    1        param1
                            p    2        param2
                    """);
            }
        });
    }

    @Test
    @DisplayName("Test lambda parameter mapping inheriting from interface method mapping expecting overwrite")
    void testLambdaParameterMappingExpectingOverwrite() throws Exception {
        this.runTest(new TestCase() {
            @Override
            public @NotNull Iterable<ChangeContributor> changeContributors() {
                return List.of(CopyLambdaParametersDown.create());
            }

            @Override
            public @NotNull MappingSet startMappings() {
                return parseTiny("""
                    c    scenario13/TestClass    scenario13/TestClass
                        m    (Ljava/lang/String;Ljava/lang/String;)V    lambda$test$0    lambda$test$0
                            p    0        string1
                            p    1        string2
                        m    (Ljava/lang/String;Ljava/lang/Class;)V    lambda$test$3    lambda$test$3
                            p    1        string1
                            p    2        clazz2
                    c    scenario13/TestInterface    scenario13/TestInterface
                        m    (Ljava/lang/Object;Ljava/lang/Object;)V    apply    apply
                            p    1        param1
                            p    2        param2
                    """);
            }

            @Override
            public @NotNull MappingSet finishMappings() {
                return parseTiny("""
                    c    scenario13/TestClass    scenario13/TestClass
                        m    (Ljava/lang/String;Ljava/lang/String;)V    lambda$test$0    lambda$test$0
                            p    0        param1
                            p    1        param2
                        m    (Ljava/lang/Integer;Ljava/lang/Integer;)V    lambda$test$1    lambda$test$1
                            p    0        param1
                            p    1        param2
                        m    (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Double;)V    lambda$test$2    lambda$test$2
                            p    2        param1
                            p    3        param2
                        m    (Ljava/lang/String;Ljava/lang/Class;)V    lambda$test$3    lambda$test$3
                            p    1        param1
                            p    2        param2
                        m    (Ljava/lang/String;Ljava/lang/String;Ljava/lang/Class;)V    lambda$test$4    lambda$test$4
                            p    2        param1
                            p    3        param2
                        m    (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Class;)V	lambda$test$5	lambda$test$5
                            p    3        param1
                            p    4        param2
                    c    scenario13/TestInterface    scenario13/TestInterface
                        m    (Ljava/lang/Object;Ljava/lang/Object;)V    apply    apply
                            p    1        param1
                            p    2        param2
                    """);
            }
        });
    }


    @Test
    @DisplayName("Test lambda parameter mapping inheriting from interface method mapping without overwrite")
    void testLambdaParameterMappingWithoutOverwrite() throws Exception {
        this.runTest(new TestCase() {
            @Override
            public @NotNull Iterable<ChangeContributor> changeContributors() {
                return List.of(CopyLambdaParametersDown.createWithoutOverwrite());
            }

            @Override
            public @NotNull MappingSet startMappings() {
                return parseTiny("""
                    c    scenario13/TestClass    scenario13/TestClass
                        m    (Ljava/lang/String;Ljava/lang/String;)V    lambda$test$0    lambda$test$0
                            p    0        string1
                            p    1        string2
                        m    (Ljava/lang/String;Ljava/lang/Class;)V    lambda$test$3    lambda$test$3
                            p    1        string1
                            p    2        clazz2
                    c    scenario13/TestInterface    scenario13/TestInterface
                        m    (Ljava/lang/Object;Ljava/lang/Object;)V    apply    apply
                            p    1        param1
                            p    2        param2
                    """);
            }

            @Override
            public @NotNull MappingSet finishMappings() {
                return parseTiny("""
                    c    scenario13/TestClass    scenario13/TestClass
                        m    (Ljava/lang/String;Ljava/lang/String;)V    lambda$test$0    lambda$test$0
                            p    0        string1
                            p    1        string2
                        m    (Ljava/lang/Integer;Ljava/lang/Integer;)V    lambda$test$1    lambda$test$1
                            p    0        param1
                            p    1        param2
                        m    (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Double;)V    lambda$test$2    lambda$test$2
                            p    2        param1
                            p    3        param2
                        m    (Ljava/lang/String;Ljava/lang/Class;)V    lambda$test$3    lambda$test$3
                            p    1        string1
                            p    2        clazz2
                        m    (Ljava/lang/String;Ljava/lang/String;Ljava/lang/Class;)V    lambda$test$4    lambda$test$4
                            p    2        param1
                            p    3        param2
                        m    (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Class;)V	lambda$test$5	lambda$test$5
                            p    3        param1
                            p    4        param2
                    c    scenario13/TestInterface    scenario13/TestInterface
                        m    (Ljava/lang/Object;Ljava/lang/Object;)V    apply    apply
                            p    1        param1
                            p    2        param2
                    """);
            }
        });
    }
}
