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
import dev.denwav.hypo.mappings.contributors.PropagateMappingsUp;
import dev.denwav.hypo.test.framework.TestScenarioBase;
import java.util.List;
import java.util.Map;
import org.cadixdev.lorenz.MappingSet;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("[integration] Scenario 04 - Conflicting Name Synthetics")
public class Scenario04 extends TestScenarioBase {

    @Override
    public @NotNull Env env() {
        return new Env() {
            @Override
            public @NotNull String forContext() {
                return "scenario-04";
            }

            @Override
            public @NotNull Iterable<HydrationProvider<?>> hydration() {
                return List.of(BridgeMethodHydrator.create());
            }

            @Override
            public @NotNull Map<String, Map<String, String>> renames() {
                return Map.of(
                    "scenario04/ParentClass", Map.of("a()Ljava/lang/Object;", "b"),
                    "scenario04/ChildClassString", Map.of("a()Ljava/lang/Object;", "b"),
                    "scenario04/ChildClassByte", Map.of("a()Ljava/lang/Object;", "b")
                );
            }
        };
    }

    @Test
    @DisplayName("Test synthetic methods with different names")
    void testSynthRenames() throws Exception {
        this.runTest(new TestCase() {
            @Override
            public @NotNull Iterable<ChangeContributor> changeContributors() {
                return List.of(PropagateMappingsUp.create(), CopyMappingsDown.create());
            }

            @Override
            public @NotNull MappingSet startMappings() {
                return parseTsrg("""
                    scenario04/ParentClass scenario04/ParentClass
                        b ()Ljava/lang/Object; getObject
                    """);
            }

            @Override
            public @NotNull MappingSet finishMappings() {
                return parseTsrg("""
                    scenario04/ParentClass scenario04/ParentClass
                        b ()Ljava/lang/Object; getObject
                    scenario04/ChildClassString scenario04/ChildClassString
                        b ()Ljava/lang/Object; getObject
                        a ()Ljava/lang/String; getObject
                    scenario04/ChildClassByte scenario04/ChildClassByte
                        b ()Ljava/lang/Object; getObject
                        a ()Ljava/lang/Byte; getObject
                    """);
            }
        });
    }

    @Test
    @DisplayName("Test synthetic methods with different names & bad starting mappings")
    void messySynthRenamesTest() throws Exception {
        this.runTest(new TestCase() {
            @Override
            public @NotNull Iterable<ChangeContributor> changeContributors() {
                return List.of(PropagateMappingsUp.create(), CopyMappingsDown.create());
            }

            @Override
            public @NotNull MappingSet startMappings() {
                return parseTsrg("""
                    scenario04/ParentClass scenario04/ParentClass
                        b ()Ljava/lang/Object; getObject
                    scenario04/ChildClassString scenario04/ChildClassString
                        a ()Ljava/lang/String; getString # incorrect mapping
                    """);
            }

            @Override
            public @NotNull MappingSet finishMappings() {
                return parseTsrg("""
                    scenario04/ParentClass scenario04/ParentClass
                        b ()Ljava/lang/Object; getObject
                    scenario04/ChildClassString scenario04/ChildClassString
                        b ()Ljava/lang/Object; getObject
                        a ()Ljava/lang/String; getObject
                    scenario04/ChildClassByte scenario04/ChildClassByte
                        b ()Ljava/lang/Object; getObject
                        a ()Ljava/lang/Byte; getObject
                    """);
            }
        });
    }
}
