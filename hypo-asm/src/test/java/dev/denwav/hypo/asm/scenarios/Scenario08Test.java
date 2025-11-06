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

import dev.denwav.hypo.model.data.ClassKind;
import dev.denwav.hypo.model.data.FieldData;
import dev.denwav.hypo.test.framework.TestScenarioBase;
import dev.denwav.hypo.types.PrimitiveType;
import dev.denwav.hypo.types.desc.TypeDescriptor;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("[asm] Scenario 08 - Java records tests (Java 21)")
public class Scenario08Test extends TestScenarioBase {

    @Override
    public @NotNull Env env() {
        return () -> "scenario-08";
    }

    @Test
    @DisplayName("Test records")
    public void testRecords() {
        final var testClass = this.findClass("scenario08/TestClass");
        assertNotNull(testClass);

        assertTrue(testClass.is(ClassKind.RECORD));

        final List<@NotNull FieldData> components = testClass.recordComponents();
        assertNotNull(components);

        assertEquals(4, components.size());

        assertEquals(testClass.field("first", TypeDescriptor.parse("Ljava/lang/String;")), components.get(0));
        assertEquals(testClass.field("second", PrimitiveType.INT), components.get(1));
        assertEquals(testClass.field("third", PrimitiveType.LONG), components.get(2));
        assertEquals(testClass.field("fourth", TypeDescriptor.parse("Ljava/lang/Object;")), components.get(3));
    }
}
