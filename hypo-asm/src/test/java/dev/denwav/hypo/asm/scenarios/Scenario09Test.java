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

import dev.denwav.hypo.model.data.ClassData;
import dev.denwav.hypo.model.data.ClassKind;
import dev.denwav.hypo.test.framework.TestScenarioBase;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("resource")
@DisplayName("[asm] Scenario 09 - Abstract enums test (Java 17)")
public class Scenario09Test extends TestScenarioBase {

    @Override
    public @NotNull Env env() {
        return () -> "scenario-09";
    }

    @Test
    @DisplayName("Test abstract enums")
    public void testAbstractEnums() throws Exception {
        final var testClass = this.context().getProvider().findClass("scenario09/TestClass");
        assertNotNull(testClass);

        // `true` is
        assertTrue(testClass.is(ClassKind.ENUM));
        assertTrue(testClass.is(ClassKind.ABSTRACT_CLASS));

        // `true` isAny
        assertTrue(testClass.isAny(ClassKind.ENUM, ClassKind.ABSTRACT_CLASS, ClassKind.INTERFACE));
        assertTrue(testClass.isAny(EnumSet.of(ClassKind.ENUM, ClassKind.ABSTRACT_CLASS, ClassKind.ANNOTATION)));

        // `true` isAll
        assertTrue(testClass.isAll(ClassKind.ENUM, ClassKind.ABSTRACT_CLASS));
        assertTrue(testClass.isAll(EnumSet.of(ClassKind.ENUM, ClassKind.ABSTRACT_CLASS)));

        // `true` isNot
        assertTrue(testClass.isNot(ClassKind.INTERFACE));
        assertTrue(testClass.isNot(ClassKind.RECORD));

        // `false` is
        assertFalse(testClass.is(ClassKind.INTERFACE));
        assertFalse(testClass.is(ClassKind.RECORD));

        // `false` isAny
        assertFalse(testClass.isAny(EnumSet.complementOf(EnumSet.of(ClassKind.ENUM, ClassKind.ABSTRACT_CLASS))));
        assertFalse(testClass.isAny(ClassKind.INTERFACE));
        assertFalse(testClass.isAny(EnumSet.of(ClassKind.INTERFACE)));

        // `false` isAll
        assertFalse(testClass.isAll(ClassKind.ENUM, ClassKind.ABSTRACT_CLASS, ClassKind.RECORD));
        assertFalse(testClass.isAll(EnumSet.of(ClassKind.ENUM, ClassKind.ABSTRACT_CLASS, ClassKind.ANNOTATION)));

        final Set<@NotNull ClassData> children = testClass.childClasses();
        assertEquals(3, children.size());

        final ClassData oneClass = children.stream().filter(c -> c.name().endsWith("1")).findFirst().orElse(null);
        assertNotNull(oneClass);
        final ClassData twoClass = children.stream().filter(c -> c.name().endsWith("2")).findFirst().orElse(null);
        assertNotNull(twoClass);
        final ClassData threeClass = children.stream().filter(c -> c.name().endsWith("3")).findFirst().orElse(null);
        assertNotNull(threeClass);

        for (final ClassData aClass : List.of(oneClass, twoClass, threeClass)) {
            assertTrue(aClass.is(ClassKind.ENUM));
            assertFalse(aClass.is(ClassKind.ABSTRACT_CLASS));
        }
    }
}
