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

@DisplayName("[integration] Scenario 05 - Duplicate Name Synthetics")
public class Scenario05 extends TestScenarioBase {

    @Override
    public @NotNull Env env() {
        return new Env() {
            @Override
            public @NotNull String forContext() {
                return "scenario-05";
            }

            @Override
            public @NotNull Iterable<HydrationProvider<?>> hydration() {
                return List.of(BridgeMethodHydrator.create());
            }

            @Override
            public @NotNull Map<String, Map<String, String>> renames() {
                return Map.of(
                    "scenario05/ParentClass", Map.of("b()Ljava/lang/Object;", "a"),
                    "scenario05/ChildClassString", Map.of("b()Ljava/lang/Object;", "a"),
                    "scenario05/ChildClassByte", Map.of("b()Ljava/lang/Object;", "a")
                );
            }
        };
    }

    @Test
    @DisplayName("Test synthetic methods with conflicting names")
    void testSynthConflictingNames() throws Exception {
        this.runTest(new TestCase() {
            @Override
            public @NotNull Iterable<ChangeContributor> changeContributors() {
                return List.of(PropagateMappingsUp.create(), CopyMappingsDown.create());
            }

            @Override
            public @NotNull MappingSet startMappings() {
                return parseTsrg("""
                    scenario05/ParentClass scenario05/ParentClass
                        a ()Ljava/lang/Object; getObject
                    scenario05/Str scenario05/Str
                        a ()Ljava/lang/String; getString
                    """);
            }

            @Override
            public @NotNull MappingSet finishMappings() {
                return parseTsrg("""
                    scenario05/ParentClass scenario05/ParentClass
                        a ()Ljava/lang/Object; getObject
                    scenario05/ChildClassString scenario05/ChildClassString
                        a ()Ljava/lang/Object; getObject
                        b ()Ljava/lang/String; getObject
                        a ()Ljava/lang/String; getString
                    scenario05/ChildClassByte scenario05/ChildClassByte
                        a ()Ljava/lang/Object; getObject
                        b ()Ljava/lang/Byte; getObject
                        a ()Ljava/lang/String; getString
                    scenario05/Str scenario05/Str
                        a ()Ljava/lang/String; getString
                    """);
            }
        });
    }

    @Test
    @DisplayName("Test synthetic methods with conflicting names & bad starting mappings")
    void badStartingMappingWithSynthConflictingNames() throws Exception {
        this.runTest(new TestCase() {
            @Override
            public @NotNull Iterable<ChangeContributor> changeContributors() {
                return List.of(PropagateMappingsUp.create(), CopyMappingsDown.create());
            }

            @Override
            public @NotNull MappingSet startMappings() {
                return parseTsrg("""
                    scenario05/ParentClass scenario05/ParentClass
                        a ()Ljava/lang/Object; getObject
                    scenario05/Str scenario05/Str
                        a ()Ljava/lang/String; getString
                    scenario05/ChildClassString scenario05/ChildClassString
                        b ()Ljava/lang/String; getString # incorrect mapping
                    """);
            }

            @Override
            public @NotNull MappingSet finishMappings() {
                return parseTsrg("""
                    scenario05/ParentClass scenario05/ParentClass
                        a ()Ljava/lang/Object; getObject
                    scenario05/ChildClassString scenario05/ChildClassString
                        a ()Ljava/lang/Object; getObject
                        b ()Ljava/lang/String; getObject
                        a ()Ljava/lang/String; getString
                    scenario05/ChildClassByte scenario05/ChildClassByte
                        a ()Ljava/lang/Object; getObject
                        b ()Ljava/lang/Byte; getObject
                        a ()Ljava/lang/String; getString
                    scenario05/Str scenario05/Str
                        a ()Ljava/lang/String; getString
                    """);
            }
        });
    }
}
