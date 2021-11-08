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

package dev.denwav.hypo.asm.scenarios;

import dev.denwav.hypo.model.data.FieldData;
import dev.denwav.hypo.model.data.MethodData;
import dev.denwav.hypo.model.data.MethodDescriptor;
import dev.denwav.hypo.model.data.types.ClassType;
import dev.denwav.hypo.model.data.types.PrimitiveType;
import dev.denwav.hypo.test.framework.TestScenarioBase;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("[asm] Scenario 03 - Synthetic members (Java 16)")
public class Scenario03 extends TestScenarioBase {

    @Override
    public @NotNull Env env() {
        return () -> "scenario-03";
    }

    @Test
    @DisplayName("Test expected synthetic members are synthetic")
    public void testSyntheticMembers() throws Exception {
        final var testClass = this.context().getProvider().findClass("scenario03/TestClass");
        final var inner = this.context().getProvider().findClass("scenario03/TestClass$Inner");

        final MethodData intSupplierSynth = testClass.method("lambda$intSupplier$0", MethodDescriptor.parseDescriptor("(Lscenario03/TestClass$Inner;)I"));
        assertTrue(intSupplierSynth.isSynthetic());

        final FieldData innerSyntheticOuterThisField = inner.field("this$0", new ClassType("scenario03.TestClass"));
        assertTrue(innerSyntheticOuterThisField.isSynthetic());
    }

    @Test
    @DisplayName("Test declared members are not synthetic")
    public void testNonSyntheticMembers() throws Exception {
        final var testClass = this.context().getProvider().findClass("scenario03/TestClass");
        final var inner = this.context().getProvider().findClass("scenario03/TestClass$Inner");

        final MethodData intSupplier = testClass.method("intSupplier", MethodDescriptor.parseDescriptor("(Lscenario03/TestClass$Inner;)Ljava/util/function/IntSupplier;"));
        assertFalse(intSupplier.isSynthetic());

        final FieldData innerDeclaredField = inner.field("notSynthetic", PrimitiveType.INT);
        assertFalse(innerDeclaredField.isSynthetic());
    }
}
