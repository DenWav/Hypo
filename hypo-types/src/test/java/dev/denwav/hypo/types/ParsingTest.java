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

import dev.denwav.hypo.types.desc.ArrayTypeDescriptor;
import dev.denwav.hypo.types.desc.ClassTypeDescriptor;
import dev.denwav.hypo.types.desc.TypeDescriptor;
import dev.denwav.hypo.types.sig.ArrayTypeSignature;
import dev.denwav.hypo.types.sig.ClassTypeSignature;
import dev.denwav.hypo.types.sig.TypeSignature;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ParsingTest {

    @Test
    void testTypeDescriptor() {
        Assertions.assertEquals(ClassTypeDescriptor.of("java/lang/Object"), TypeDescriptor.parse("Ljava/lang/Object;"));
        Assertions.assertEquals(ArrayTypeDescriptor.of(2, ClassTypeDescriptor.of("java/lang/Object")), TypeDescriptor.parse("[[Ljava/lang/Object;"));
        Assertions.assertEquals(VoidType.INSTANCE, TypeDescriptor.parse("V"));
        Assertions.assertEquals(PrimitiveType.CHAR, TypeDescriptor.parse("C"));
        Assertions.assertEquals(PrimitiveType.BYTE, TypeDescriptor.parse("B"));
        Assertions.assertEquals(PrimitiveType.SHORT, TypeDescriptor.parse("S"));
        Assertions.assertEquals(PrimitiveType.INT, TypeDescriptor.parse("I"));
        Assertions.assertEquals(PrimitiveType.LONG, TypeDescriptor.parse("J"));
        Assertions.assertEquals(PrimitiveType.FLOAT, TypeDescriptor.parse("F"));
        Assertions.assertEquals(PrimitiveType.DOUBLE, TypeDescriptor.parse("D"));
        Assertions.assertEquals(PrimitiveType.BOOLEAN, TypeDescriptor.parse("Z"));
    }

    @Test
    void testTypeSignature() {
        Assertions.assertEquals(ClassTypeSignature.of(null, "java/lang/Object", null), TypeSignature.parse("Ljava/lang/Object;"));
        Assertions.assertEquals(ArrayTypeSignature.of(2, ClassTypeSignature.of(null, "java/lang/Object", null)), TypeSignature.parse("[[Ljava/lang/Object;"));
        Assertions.assertEquals(VoidType.INSTANCE, TypeSignature.parse("V"));
        Assertions.assertEquals(PrimitiveType.CHAR, TypeSignature.parse("C"));
        Assertions.assertEquals(PrimitiveType.BYTE, TypeSignature.parse("B"));
        Assertions.assertEquals(PrimitiveType.SHORT, TypeSignature.parse("S"));
        Assertions.assertEquals(PrimitiveType.INT, TypeSignature.parse("I"));
        Assertions.assertEquals(PrimitiveType.LONG, TypeSignature.parse("J"));
        Assertions.assertEquals(PrimitiveType.FLOAT, TypeSignature.parse("F"));
        Assertions.assertEquals(PrimitiveType.DOUBLE, TypeSignature.parse("D"));
        Assertions.assertEquals(PrimitiveType.BOOLEAN, TypeSignature.parse("Z"));

        Assertions.assertEquals(
            ClassTypeSignature.of(
                null,
                "net/minecraft/world/entity/ai/memory/MemoryModuleType",
                List.of(ClassTypeSignature.of(null, "net/minecraft/world/entity/LivingEntity", null))
            ),
            TypeSignature.parse(
                "Lnet/minecraft/world/entity/ai/memory/MemoryModuleType<Lnet/minecraft/world/entity/LivingEntity;>;"
            )
        );
    }
}
