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

import dev.denwav.hypo.test.framework.TestScenarioBase;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("resource")
@DisplayName("[asm] Scenario 04 - Sealed classes (Java 17)")
public class Scenario04Test extends TestScenarioBase {

    @Override
    public @NotNull Env env() {
        return () -> "scenario-04";
    }

    @Test
    @DisplayName("Test isSealed()")
    public void testIsSealed() throws Exception {
        final var testClass = this.context().getProvider().findClass("scenario04/TestClass");
        assertNotNull(testClass);
        final var testSubClass = this.context().getProvider().findClass("scenario04/TestSubClass");
        assertNotNull(testSubClass);
        final var testSealedSubClass = this.context().getProvider().findClass("scenario04/TestSealedSubClass");
        assertNotNull(testSealedSubClass);
        final var testSubSubClass = this.context().getProvider().findClass("scenario04/TestSubSubClass");
        assertNotNull(testSubSubClass);

        assertTrue(testClass.isSealed());
        assertFalse(testSubClass.isSealed());
        assertTrue(testSealedSubClass.isSealed());
        assertFalse(testSubSubClass.isSealed());
    }

    @Test
    @DisplayName("Test correct permitted classes are returned")
    public void testPermitted() throws Exception {
        final var testClass = this.context().getProvider().findClass("scenario04/TestClass");
        assertNotNull(testClass);
        final var testSubClass = this.context().getProvider().findClass("scenario04/TestSubClass");
        assertNotNull(testSubClass);
        final var testSealedSubClass = this.context().getProvider().findClass("scenario04/TestSealedSubClass");
        assertNotNull(testSealedSubClass);
        final var testSubSubClass = this.context().getProvider().findClass("scenario04/TestSubSubClass");
        assertNotNull(testSubSubClass);

        assertEquals(List.of(testSubClass, testSealedSubClass), testClass.permittedClasses());
        assertNull(testSubClass.permittedClasses());
        assertEquals(List.of(testSubSubClass), testSealedSubClass.permittedClasses());
        assertNull(testSubSubClass.permittedClasses());
    }
}
