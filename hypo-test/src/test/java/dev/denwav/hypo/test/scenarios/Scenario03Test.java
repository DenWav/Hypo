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

import dev.denwav.hypo.asm.hydrate.BridgeMethodHydrator;
import dev.denwav.hypo.hydrate.HydrationProvider;
import dev.denwav.hypo.mappings.contributors.ChangeContributor;
import dev.denwav.hypo.mappings.contributors.CopyMappingsDown;
import dev.denwav.hypo.mappings.contributors.PropagateMappingsUp;
import dev.denwav.hypo.test.framework.TestScenarioBase;
import java.util.List;
import org.cadixdev.lorenz.MappingSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("[integration] Scenario 03 - Simple Synthetic Methods")
public class Scenario03Test extends TestScenarioBase {
    @Override
    public @NotNull Env env() {
        return new Env() {
            @Override
            public @NotNull String forContext() {
                return "scenario-03";
            }

            @Override
            public @NotNull Iterable<HydrationProvider<?>> hydration() {
                return List.of(BridgeMethodHydrator.create());
            }
        };
    }

    @Test
    @DisplayName("Test handling covariant return types")
    void testOverloads() throws Exception {
        this.runTest(new TestCase() {
            @Override
            public @NotNull Iterable<ChangeContributor> changeContributors() {
                return List.of(PropagateMappingsUp.create(), CopyMappingsDown.create());
            }

            @Override
            public @NotNull MappingSet startMappings() {
                return parseTsrg("""
                    scenario03/ParentClass scenario03/ParentClass
                        method ()Ljava/lang/Object; getObject
                    scenario03/ChildClassString scenario03/ChildClassString
                        method ()Ljava/lang/String; getString
                    scenario03/ChildClassByte scenario03/ChildClassByte
                        method ()Ljava/lang/Byte; getByte
                    """);
            }

            @Override
            @SuppressWarnings("MultipleNullnessAnnotations")
            public @NotNull MappingSet @Nullable [] stageMappings() {
                return new MappingSet[] {
                    // First stage, propagate up
                    parseTsrg("""
                        scenario03/ParentClass scenario03/ParentClass
                            method ()Ljava/lang/Object; getObject
                        """),
                    // Second stage, copy down
                    parseTsrg("""
                        scenario03/ParentClass scenario03/ParentClass
                            method ()Ljava/lang/Object; getObject
                        scenario03/ChildClassString scenario03/ChildClassString
                            method ()Ljava/lang/Object; getObject
                            method ()Ljava/lang/String; getObject
                        scenario03/ChildClassByte scenario03/ChildClassByte
                            method ()Ljava/lang/Object; getObject
                            method ()Ljava/lang/Byte; getObject
                        scenario03/GrandChildClassString scenario03/GrandChildClassString
                            method ()Ljava/lang/Object; getObject
                            method ()Ljava/lang/String; getObject
                        """),
                };
            }
        });
    }
}
