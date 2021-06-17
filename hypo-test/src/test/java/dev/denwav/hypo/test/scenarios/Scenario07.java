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

import dev.denwav.hypo.asm.hydrate.SuperConstructorHydrator;
import dev.denwav.hypo.hydrate.HydrationProvider;
import dev.denwav.hypo.mappings.contributors.ChangeContributor;
import dev.denwav.hypo.mappings.contributors.CopyMappingsDown;
import dev.denwav.hypo.test.framework.TestScenarioBase;
import java.util.List;
import org.cadixdev.lorenz.MappingSet;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("[integration] Scenario 07 - Complex Constructors")
public class Scenario07 extends TestScenarioBase {

    @Override
    public @NotNull Env env() {
        return new Env() {
            @Override
            public @NotNull String forContext() {
                return "scenario-07";
            }

            @Override
            public @NotNull Iterable<HydrationProvider<?>> hydration() {
                return List.of(SuperConstructorHydrator.create());
            }
        };
    }

    @Test
    @DisplayName("Test overly complex super() call parameter propagation")
    void testComplexSuperParamPropagation() throws Exception {
        this.runTest(new TestCase() {
            @Override
            public @NotNull Iterable<ChangeContributor> changeContributors() {
                return List.of(CopyMappingsDown.create());
            }

            @Override
            public @NotNull MappingSet startMappings() {
                return parseTiny("""
                    c    scenario07/ParentClass    scenario07/ParentClass
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
                    c    scenario07/ParentClass    scenario07/ParentClass
                        m    (IIJLjava/lang/String;Ljava/util/List;Ljava/lang/Object;)V    <init>    <init>
                            p    1        left
                            p    2        right
                            p    3        up
                            p    5        down
                            p    6        forward
                            p    7        back
                    c    scenario07/ChildClass    scenario07/ChildClass
                        m    (IILjava/lang/String;JLjava/lang/Object;Ljava/util/List;)V    <init>    <init>
                            p    1        right
                            p    2        left
                            p    3        down
                            p    4        up
                            p    6        back
                            p    7        forward
                    c    scenario07/GrandChildClass    scenario07/GrandChildClass
                        m    (Ljava/lang/Object;Ljava/util/List;Ljava/lang/String;JII)V    <init>    <init>
                            p    1        back
                            p    2        forward
                            p    3        down
                            p    4        up
                            p    6        right
                            p    7        left
                    c    scenario07/GreatGrandChildClass    scenario07/GreatGrandChildClass
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
