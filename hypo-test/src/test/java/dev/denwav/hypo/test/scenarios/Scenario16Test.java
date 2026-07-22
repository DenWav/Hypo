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
import dev.denwav.hypo.hydrate.generic.HypoHydration;
import dev.denwav.hypo.hydrate.generic.SuperCall;
import dev.denwav.hypo.mappings.contributors.ChangeContributor;
import dev.denwav.hypo.mappings.contributors.CopyMappingsDown;
import dev.denwav.hypo.model.data.ClassData;
import dev.denwav.hypo.model.data.MethodData;
import dev.denwav.hypo.test.framework.TestScenarioBase;
import java.util.List;
import org.cadixdev.lorenz.MappingSet;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("[integration] Scenario 16 - this() Constructor Delegation")
public class Scenario16Test extends TestScenarioBase {

    @Override
    public @NotNull Env env() {
        return new Env() {
            @Override
            public @NotNull String forContext() {
                return "scenario-16";
            }

            @Override
            public @NotNull Iterable<HydrationProvider<?>> hydration() {
                return List.of(SuperConstructorHydrator.create());
            }
        };
    }

    @Test
    @DisplayName("Mapping name propagates through a single this() delegation chain")
    void testThisCallParamPropagation() throws Exception {
        this.runTest(new TestCase() {
            @Override
            public @NotNull Iterable<ChangeContributor> changeContributors() {
                return List.of(CopyMappingsDown.create());
            }

            @Override
            public @NotNull MappingSet startMappings() {
                return parseTiny("""
                    c    scenario16/ParentClass    scenario16/ParentClass
                        m    (I)V    <init>    <init>
                            p    1        value
                    """);
            }

            @Override
            public @NotNull MappingSet finishMappings() {
                return parseTiny("""
                    c    scenario16/ParentClass    scenario16/ParentClass
                        m    (I)V    <init>    <init>
                            p    1        value
                    c    scenario16/DelegatingClass    scenario16/DelegatingClass
                        m    (I)V    <init>    <init>
                            p    1        value
                        m    (Ljava/lang/String;I)V    <init>    <init>
                            p    2        value
                    c    scenario16/ChainedDelegatingClass    scenario16/ChainedDelegatingClass
                        m    (I)V    <init>    <init>
                            p    1        value
                        m    (Ljava/lang/String;I)V    <init>    <init>
                            p    2        value
                    """);
            }
        });
    }

    @Test
    @DisplayName("Mapping name propagates through a three-level this() delegation chain")
    void testChainedThisCallParamPropagation() throws Exception {
        this.runTest(new TestCase() {
            @Override
            public @NotNull Iterable<ChangeContributor> changeContributors() {
                return List.of(CopyMappingsDown.create());
            }

            @Override
            public @NotNull MappingSet startMappings() {
                return parseTiny("""
                    c    scenario16/ParentClass    scenario16/ParentClass
                        m    (I)V    <init>    <init>
                            p    1        value
                    """);
            }

            @Override
            public @NotNull MappingSet finishMappings() {
                return parseTiny("""
                    c    scenario16/ParentClass    scenario16/ParentClass
                        m    (I)V    <init>    <init>
                            p    1        value
                    c    scenario16/DelegatingClass    scenario16/DelegatingClass
                        m    (I)V    <init>    <init>
                            p    1        value
                        m    (Ljava/lang/String;I)V    <init>    <init>
                            p    2        value
                    c    scenario16/ChainedDelegatingClass    scenario16/ChainedDelegatingClass
                        m    (I)V    <init>    <init>
                            p    1        value
                        m    (Ljava/lang/String;I)V    <init>    <init>
                            p    2        value
                    """);
            }
        });
    }

    @Test
    @DisplayName("SuperConstructorHydrator sets SUPER_CALL_TARGET on delegating constructors with correct param mappings")
    void testSuperCallTargetHydration() throws Exception {
        this.setup();

        @SuppressWarnings("resource") final ClassData delegating = this.context().getContextProvider()
            .findClass("scenario16/DelegatingClass");
        assertNotNull(delegating, "DelegatingClass should be present in the context");

        final MethodData stringIntCtor = findMethod(delegating, "<init>", "(Ljava/lang/String;I)V");
        final SuperCall superCall = stringIntCtor.get(HypoHydration.SUPER_CALL_TARGET);
        assertNotNull(superCall,
            "DelegatingClass(String,int) should have SUPER_CALL_TARGET set by SuperConstructorHydrator");

        assertEquals("scenario16/DelegatingClass",
            superCall.superConstructor().parentClass().name(),
            "Super call target class should be DelegatingClass");
        assertEquals("(I)V",
            superCall.superConstructor().descriptorText(),
            "Super call target descriptor should be (I)V");

        final List<SuperCall.SuperCallParameter> params = superCall.params();
        assertEquals(1, params.size(),
            "Exactly one parameter should be mapped through the this() call");
        assertEquals(2, params.getFirst().thisIndex(),
            "thisIndex should be 2 (the int value occupies LVT slot 2 in (String, int))");
        assertEquals(1, params.getFirst().superIndex(),
            "superIndex should be 1 (the int value occupies LVT slot 1 in (int))");
    }
}
