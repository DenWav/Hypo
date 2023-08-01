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
import dev.denwav.hypo.core.HypoConfig;
import dev.denwav.hypo.hydrate.HydrationProvider;
import dev.denwav.hypo.mappings.contributors.ChangeContributor;
import dev.denwav.hypo.mappings.contributors.CopyMappingsDown;
import dev.denwav.hypo.mappings.contributors.PropagateMappingsUp;
import dev.denwav.hypo.test.framework.TestScenarioBase;
import java.util.List;
import org.cadixdev.lorenz.MappingSet;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("[integration] Scenario 12 - Super Constructs from Inner Classes")
public class Scenario12 extends TestScenarioBase {

    @Override
    public @NotNull Env env() {
        return new Env() {
            @Override
            public @NotNull String forContext() {
                return "scenario-12";
            }

            @Override
            public @NotNull Iterable<HydrationProvider<?>> hydration() {
                return List.of(SuperConstructorHydrator.create());
            }

            @Override
            public @NotNull HypoConfig.Builder config() {
                return HypoConfig.builder().withParallelism(1);
            }
        };
    }

    @Test
    @DisplayName("Test super constructor param mappings from inner class")
    void testSuperConstructorFromInnerClass() throws Exception {
        this.runTest(new TestCase() {
            @Override
            public @NotNull Iterable<ChangeContributor> changeContributors() {
                return List.of(PropagateMappingsUp.create(), CopyMappingsDown.create());
            }

            @Override
            public @NotNull MappingSet startMappings() {
                return parseTiny("""
                    c    scenario12/BaseClass    scenario12/BaseClass
                        m    (III)V    <init>    <init>
                            p    1        i
                            p    2        j
                            p    3        k
                    """);
            }

            @Override
            public @NotNull MappingSet finishMappings() {
                return parseTiny("""
                    c    scenario12/BaseClass    scenario12/BaseClass
                        m    (III)V    <init>    <init>
                            p    1        i
                            p    2        j
                            p    3        k
                    c    scenario12/TestClass$InnerClass    scenario12/TestClass$InnerClass
                        m    (III)V    <init>    <init>
                            p    1        i
                            p    2        j
                            p    3        k
                    c    scenario12/ChildClass    scenario12/ChildClass
                        m    (III)V    <init>    <init>
                            p    1        i
                            p    2        j
                            p    3        k
                    """);
            }
        });
    }
}
