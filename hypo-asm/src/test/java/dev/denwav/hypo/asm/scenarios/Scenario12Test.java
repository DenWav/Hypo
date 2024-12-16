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

package dev.denwav.hypo.asm.scenarios;

import dev.denwav.hypo.model.data.FieldData;
import dev.denwav.hypo.model.data.MethodData;
import dev.denwav.hypo.test.framework.TestScenarioBase;
import dev.denwav.hypo.types.PrimitiveType;
import dev.denwav.hypo.types.desc.ClassTypeDescriptor;
import dev.denwav.hypo.types.desc.MethodDescriptor;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("[asm] Scenario 12 - Synthetic classes and members (Java 21)")
public class Scenario12Test extends TestScenarioBase {

    @Override
    public @NotNull Env env() {
        return () -> "scenario-03";
    }

    @Test
    @DisplayName("Test expected synthetic members are synthetic")
    public void testSyntheticMembers() {
        final var testClass = this.findClass("scenario03/TestClass");
        assertNotNull(testClass);
        final var inner = this.findClass("scenario03/TestClass$Inner");
        assertNotNull(inner);

        final MethodData intSupplierSynth = testClass.method("lambda$intSupplier$0", MethodDescriptor.parse("(Lscenario03/TestClass$Inner;)I"));
        assertNotNull(intSupplierSynth, "Did not find expected lambda$intSupplier$0 synthetic member in TestClass");
        assertTrue(intSupplierSynth.isSynthetic());

        final FieldData innerSyntheticOuterThisField = inner.field("this$0", ClassTypeDescriptor.of("scenario03.TestClass"));
        assertNotNull(innerSyntheticOuterThisField, "Did not find expected this$0 synthetic member in TestClass$Inner");
        assertTrue(innerSyntheticOuterThisField.isSynthetic());
    }

    @Test
    @DisplayName("Test declared members are not synthetic")
    public void testNonSyntheticMembers() {
        final var testClass = this.findClass("scenario03/TestClass");
        assertNotNull(testClass);
        final var inner = this.findClass("scenario03/TestClass$Inner");
        assertNotNull(inner);

        final MethodData intSupplier = testClass.method("intSupplier", MethodDescriptor.parse("(Lscenario03/TestClass$Inner;)Ljava/util/function/IntSupplier;"));
        assertNotNull(intSupplier, "Did not find expected method intSupplier in TestClass");
        assertFalse(intSupplier.isSynthetic());

        final FieldData innerDeclaredField = inner.field("notSynthetic", PrimitiveType.INT);
        assertNotNull(innerDeclaredField, "Did not find expected field notSynthetic in TestClass$Inner");
        assertFalse(innerDeclaredField.isSynthetic());
    }

    @Test
    @DisplayName("Test declared classes are not synthetic")
    public void testNonSyntheticClasses() {
        final var testClass = this.findClass("scenario03/TestClass");
        assertNotNull(testClass);
        assertFalse(testClass.isSynthetic());

        final var inner = this.findClass("scenario03/TestClass$Inner");
        assertNotNull(inner);
        assertFalse(inner.isSynthetic());
    }

    @Test
    @DisplayName("Test expected synthetic classes are synthetic")
    public void testSyntheticClasses() throws IOException {
        @SuppressWarnings("resource") final var syntheticInner = this.context().getContextProvider().findClass("scenario03/TestClass$1");
        assertNotNull(syntheticInner, "Did not find expected TestClass$1 synthetic inner class");
        assertTrue(syntheticInner.isSynthetic());
    }
}
