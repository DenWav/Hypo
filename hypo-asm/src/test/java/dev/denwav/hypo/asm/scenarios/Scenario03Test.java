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
import dev.denwav.hypo.model.data.MethodDescriptor;
import dev.denwav.hypo.model.data.types.ClassType;
import dev.denwav.hypo.model.data.types.PrimitiveType;
import dev.denwav.hypo.test.framework.TestScenarioBase;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("resource")
@DisplayName("[asm] Scenario 03 - Synthetic classes and members (Java 17)")
public class Scenario03Test extends TestScenarioBase {

    @Override
    public @NotNull Env env() {
        return () -> "scenario-03";
    }

    @Test
    @DisplayName("Test expected synthetic members are synthetic")
    public void testSyntheticMembers() throws Exception {
        final var testClass = this.context().getProvider().findClass("scenario03/TestClass");
        assertNotNull(testClass);
        final var inner = this.context().getProvider().findClass("scenario03/TestClass$Inner");
        assertNotNull(inner);

        final MethodData intSupplierSynth = testClass.method("lambda$intSupplier$0", MethodDescriptor.parseDescriptor("(Lscenario03/TestClass$Inner;)I"));
        assertNotNull(intSupplierSynth, "Did not find expected lambda$intSupplier$0 synthetic member in TestClass");
        assertTrue(intSupplierSynth.isSynthetic());

        final FieldData innerSyntheticOuterThisField = inner.field("this$0", new ClassType("scenario03.TestClass"));
        assertNotNull(innerSyntheticOuterThisField, "Did not find expected this$0 synthetic member in TestClass$Inner");
        assertTrue(innerSyntheticOuterThisField.isSynthetic());
    }

    @Test
    @DisplayName("Test declared members are not synthetic")
    public void testNonSyntheticMembers() throws Exception {
        final var testClass = this.context().getProvider().findClass("scenario03/TestClass");
        assertNotNull(testClass);
        final var inner = this.context().getProvider().findClass("scenario03/TestClass$Inner");
        assertNotNull(inner);

        final MethodData intSupplier = testClass.method("intSupplier", MethodDescriptor.parseDescriptor("(Lscenario03/TestClass$Inner;)Ljava/util/function/IntSupplier;"));
        assertNotNull(intSupplier, "Did not find expected method intSupplier in TestClass");
        assertFalse(intSupplier.isSynthetic());

        final FieldData innerDeclaredField = inner.field("notSynthetic", PrimitiveType.INT);
        assertNotNull(innerDeclaredField, "Did not find expected field notSynthetic in TestClass$Inner");
        assertFalse(innerDeclaredField.isSynthetic());
    }

    @Test
    @DisplayName("Test declared classes are not synthetic")
    public void testNonSyntheticClasses() throws Exception {
        final var testClass = this.context().getProvider().findClass("scenario03/TestClass");
        assertNotNull(testClass);
        assertFalse(testClass.isSynthetic());

        final var inner = this.context().getProvider().findClass("scenario03/TestClass$Inner");
        assertNotNull(inner);
        assertFalse(inner.isSynthetic());
    }

    @Test
    @DisplayName("Test expected synthetic classes are synthetic")
    public void testSyntheticClasses() throws Exception {
        final var syntheticInner = this.context().getProvider().findClass("scenario03/TestClass$1");
        assertNotNull(syntheticInner, "Did not find expected TestClass$1 synthetic inner class");
        assertTrue(syntheticInner.isSynthetic());
    }
}
