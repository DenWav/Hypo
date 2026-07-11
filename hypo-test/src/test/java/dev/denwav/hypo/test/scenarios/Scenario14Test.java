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
import dev.denwav.hypo.mappings.contributors.PropagateMappingsUp;
import dev.denwav.hypo.test.framework.TestScenarioBase;
import java.util.List;
import org.cadixdev.lorenz.MappingSet;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("[integration] Scenario 14 - Flexible Constructor Bodies")
public class Scenario14Test extends TestScenarioBase {

    @Override
    public @NotNull Env env() {
        return new Env() {
            @Override
            public @NotNull String forContext() {
                return "scenario-14";
            }

            @Override
            public @NotNull Iterable<HydrationProvider<?>> hydration() {
                return List.of(SuperConstructorHydrator.create());
            }
        };
    }

    @Test
    @DisplayName("Test super() call param propagation past a preamble")
    void testPreamble() throws Exception {
        this.runTest(new TestCase() {
            @Override
            public @NotNull Iterable<ChangeContributor> changeContributors() {
                return List.of(CopyMappingsDown.create());
            }

            @Override
            public @NotNull MappingSet startMappings() {
                return parseTiny("""
                    c    scenario14/ParentClass    scenario14/ParentClass
                        m    (I)V    <init>    <init>
                            p    1        value
                    """);
            }

            @Override
            public @NotNull MappingSet finishMappings() {
                return parseTiny("""
                    c    scenario14/ParentClass    scenario14/ParentClass
                        m    (I)V    <init>    <init>
                            p    1        value
                    c    scenario14/LoopPreambleChild    scenario14/LoopPreambleChild
                        m    (I)V    <init>    <init>
                            p    1        value
                    c    scenario14/NewInPreambleChild    scenario14/NewInPreambleChild
                        m    (I)V    <init>    <init>
                            p    1        value
                    c    scenario14/IncDecPreambleChild    scenario14/IncDecPreambleChild
                        m    (I)V    <init>    <init>
                            p    1        value
                    c    scenario14/SimplePreambleChild    scenario14/SimplePreambleChild
                        m    (I)V    <init>    <init>
                            p    1        value
                    c    scenario14/SwitchPreambleChild    scenario14/SwitchPreambleChild
                        m    (I)V    <init>    <init>
                            p    1        value
                    c    scenario14/TryCatchPreambleChild    scenario14/TryCatchPreambleChild
                        m    (I)V    <init>    <init>
                            p    1        value
                    c    scenario14/MixedControlFlowPreambleChild    scenario14/MixedControlFlowPreambleChild
                        m    (I)V    <init>    <init>
                            p    1        value
                    """);
            }
        });
    }

    @Test
    @DisplayName("Test super() call param propagation past a preamble in a non-static inner class")
    void testPreambleInNonStaticInnerClass() throws Exception {
        this.runTest(new TestCase() {
            @Override
            public @NotNull Iterable<ChangeContributor> changeContributors() {
                return List.of(PropagateMappingsUp.create(), CopyMappingsDown.create());
            }

            @Override
            public @NotNull MappingSet startMappings() {
                return parseTiny("""
                    c    scenario14/BaseClass    scenario14/BaseClass
                        m    (I)V    <init>    <init>
                            p    1        value
                    """);
            }

            @Override
            public @NotNull MappingSet finishMappings() {
                return parseTiny("""
                    c    scenario14/BaseClass    scenario14/BaseClass
                        m    (I)V    <init>    <init>
                            p    1        value
                    c    scenario14/OuterClass$InnerChild    scenario14/OuterClass$InnerChild
                        m    (Lscenario14/OuterClass;I)V    <init>    <init>
                            p    2        value
                    """);
            }
        });
    }
}
