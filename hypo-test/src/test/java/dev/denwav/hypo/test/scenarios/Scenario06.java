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

import dev.denwav.hypo.asm.hydrate.SuperConstructorHydrator;
import dev.denwav.hypo.hydrate.HydrationProvider;
import dev.denwav.hypo.mappings.contributors.ChangeContributor;
import dev.denwav.hypo.mappings.contributors.CopyMappingsDown;
import dev.denwav.hypo.mappings.contributors.RemoveUnusedMappings;
import dev.denwav.hypo.test.framework.TestScenarioBase;
import java.util.List;
import org.cadixdev.lorenz.MappingSet;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("[integration] Scenario 06 - Constructors")
public class Scenario06 extends TestScenarioBase {

    @Override
    public @NotNull Env env() {
        return new Env() {
            @Override
            public @NotNull String forContext() {
                return "scenario-06";
            }

            @Override
            public @NotNull Iterable<HydrationProvider<?>> hydration() {
                return List.of(SuperConstructorHydrator.create());
            }
        };
    }

    @Test
    @DisplayName("Test simple super() call parameter propagation")
    void testSuperParamPropagation() throws Exception {
        this.runTest(new TestCase() {
            @Override
            public @NotNull Iterable<ChangeContributor> changeContributors() {
                return List.of(CopyMappingsDown.create());
            }

            @Override
            public @NotNull MappingSet startMappings() {
                return parseTiny("""
                    c    scenario06/ParentClass    scenario06/ParentClass
                        m    (IIJLjava/lang/String;Ljava/util/List;Ljava/lang/Object;)V    <init>    <init>
                            p    1        left
                            p    2        right
                            p    3        up
                            p    5        down
                            p    6        forward
                            p    7        back
                    """);
            }

            @Override
            public @NotNull MappingSet finishMappings() {
                return parseTiny("""
                    c    scenario06/ParentClass    scenario06/ParentClass
                        m    (IIJLjava/lang/String;Ljava/util/List;Ljava/lang/Object;)V    <init>    <init>
                            p    1        left
                            p    2        right
                            p    3        up
                            p    5        down
                            p    6        forward
                            p    7        back
                    c    scenario06/ChildClass    scenario06/ChildClass
                        m    (IIJLjava/lang/String;Ljava/util/List;Ljava/lang/Object;)V    <init>    <init>
                            p    1        left
                            p    2        right
                            p    3        up
                            p    5        down
                            p    6        forward
                            p    7        back
                    c    scenario06/GrandChildClass    scenario06/GrandChildClass
                        m    (IIJLjava/lang/String;Ljava/util/List;Ljava/lang/Object;)V    <init>    <init>
                            p    1        left
                            p    2        right
                            p    3        up
                            p    5        down
                            p    6        forward
                            p    7        back
                    c    scenario06/GreatGrandChildClass    scenario06/GreatGrandChildClass
                        m    (IIJLjava/lang/String;Ljava/util/List;Ljava/lang/Object;)V    <init>    <init>
                            p    1        left
                            p    2        right
                            p    3        up
                            p    5        down
                            p    6        forward
                            p    7        back
                    """);
            }
        });
    }

    @Test
    @DisplayName("Test super() call parameter propagation stopping")
    void testSuperCallParameterStopping() throws Exception {
        this.runTest(new TestCase() {
            @Override
            public @NotNull Iterable<ChangeContributor> changeContributors() {
                return List.of(CopyMappingsDown.create());
            }

            @Override
            public @NotNull MappingSet startMappings() {
                return parseTiny("""
                    c    scenario06/ParentClass    scenario06/ParentClass
                        m    (IIJLjava/lang/String;Ljava/util/List;Ljava/lang/Object;)V    <init>    <init>
                            p    1        left
                            p    2        right
                            p    3        up
                            p    5        down
                            p    6        forward
                            p    7        back
                    c    scenario06/GrandChildClass    scenario06/GrandChildClass
                        m    (IIJLjava/lang/String;Ljava/util/List;Ljava/lang/Object;)V    <init>    <init>
                            p    1        blue
                            p    2        green
                            p    3        red
                            p    5        purple
                            p    6        yellow
                            p    7        pink
                    """);
            }

            @Override
            public @NotNull MappingSet finishMappings() {
                return parseTiny("""
                    c    scenario06/ParentClass    scenario06/ParentClass
                        m    (IIJLjava/lang/String;Ljava/util/List;Ljava/lang/Object;)V    <init>    <init>
                            p    1        left
                            p    2        right
                            p    3        up
                            p    5        down
                            p    6        forward
                            p    7        back
                    c    scenario06/ChildClass    scenario06/ChildClass
                        m    (IIJLjava/lang/String;Ljava/util/List;Ljava/lang/Object;)V    <init>    <init>
                            p    1        left
                            p    2        right
                            p    3        up
                            p    5        down
                            p    6        forward
                            p    7        back
                    c    scenario06/GrandChildClass    scenario06/GrandChildClass
                        m    (IIJLjava/lang/String;Ljava/util/List;Ljava/lang/Object;)V    <init>    <init>
                            p    1        blue
                            p    2        green
                            p    3        red
                            p    5        purple
                            p    6        yellow
                            p    7        pink
                    c    scenario06/GreatGrandChildClass    scenario06/GreatGrandChildClass
                        m    (IIJLjava/lang/String;Ljava/util/List;Ljava/lang/Object;)V    <init>    <init>
                            p    1        blue
                            p    2        green
                            p    3        red
                            p    5        purple
                            p    6        yellow
                            p    7        pink
                    """);
            }
        });
    }

    @Test
    @DisplayName("Test removing unused param mappings complex")
    void testRemoveUnusedParamsComplex() throws Exception {
        this.runTest(new TestCase() {
            @Override
            public @NotNull Iterable<ChangeContributor> changeContributors() {
                return List.of(RemoveUnusedMappings.create());
            }

            @Override
            public @NotNull MappingSet startMappings() {
                return parseTiny("""
                    c    scenario06/ParentClass    scenario06/ParentClass
                        m    (IIJLjava/lang/String;Ljava/util/List;Ljava/lang/Object;)V    <init>    <init>
                            p    1        left
                            p    2        right
                            p    3        up
                            p    4        notExist
                            p    5        down
                            p    6        forward
                            p    7        back
                            p    8        notExist2
                    """);
            }

            @Override
            public @NotNull MappingSet finishMappings() {
                return parseTiny("""
                    c    scenario06/ParentClass    scenario06/ParentClass
                        m    (IIJLjava/lang/String;Ljava/util/List;Ljava/lang/Object;)V    <init>    <init>
                            p    1        left
                            p    2        right
                            p    3        up
                            p    5        down
                            p    6        forward
                            p    7        back
                    """);
            }
        });
    }
}
