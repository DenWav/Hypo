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

package dev.denwav.hypo.types;

import dev.denwav.hypo.types.desc.ClassTypeDescriptor;
import dev.denwav.hypo.types.desc.TypeDescriptor;
import dev.denwav.hypo.types.intern.Intern;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

class InternTest {

    private static final MethodHandle constructor;
    static {
        try {
            constructor = MethodHandles.privateLookupIn(ClassTypeDescriptor.class, MethodHandles.lookup())
                .findConstructor(ClassTypeDescriptor.class, MethodType.methodType(void.class, String.class));
        } catch (final NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testIntern() {
        final TypeDescriptor first = TypeDescriptor.parse("Ljava/lang/String;");
        final TypeDescriptor second = TypeDescriptor.parse("Ljava/lang/String;");
        final TypeDescriptor third = ClassTypeDescriptor.of("java/lang/String");

        Assertions.assertSame(first, second);
        Assertions.assertSame(first, third);
        Assertions.assertEquals(1, Intern.internmentSize(ClassTypeDescriptor.class));
    }

    @Test
    void testInternOnSeparate() throws Throwable {
        final ClassTypeDescriptor instance1 = (ClassTypeDescriptor) constructor.invoke("java/lang/String");
        final ClassTypeDescriptor instance2 = (ClassTypeDescriptor) constructor.invoke("java/lang/String");

        // Guaranteed - sanity check
        assertNotSame(instance1, instance2);

        assertSame(instance1.intern(), instance2.intern());
    }
}
