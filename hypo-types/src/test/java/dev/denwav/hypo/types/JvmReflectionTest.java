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

import com.google.common.reflect.TypeToken;
import dev.denwav.hypo.types.desc.ClassTypeDescriptor;
import dev.denwav.hypo.types.desc.MethodDescriptor;
import dev.denwav.hypo.types.desc.TypeDescriptor;
import dev.denwav.hypo.types.sig.ClassTypeSignature;
import dev.denwav.hypo.types.sig.MethodSignature;
import dev.denwav.hypo.types.sig.TypeSignature;
import dev.denwav.hypo.types.sig.param.TypeParameter;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("all")
class JvmReflectionTest {

    @Test
    void testTypeDescriptor() {
        assertEquals(ClassTypeDescriptor.of("java/lang/String"), TypeDescriptor.of(String.class));
    }

    private static final String someMethodSig = "<T::Ldev/denwav/hypo/types/JvmReflectionTest$OneInterface<*>;:Ldev/denwav/hypo/types/JvmReflectionTest$TwoInterface;>(Ljava/lang/Object;Ljava/util/Map<-Ljava/lang/String;Ljava/util/function/Function<Ljava/lang/Integer;+Ljava/lang/String;>;>;[I[[Ljava/lang/String;[Ljava/util/List<Ljava/lang/String;>;TT;)Ljava/util/List<Ljava/lang/String;>;";
    @SuppressWarnings("unused")
    private <T extends OneInterface<?> & TwoInterface> List<String> someMethod(
        Object a,
        Map<? super String, Function<Integer, ? extends String>> func,
        int[] i,
        String[][] s,
        List<String>[] l,
        T intersection
    ) {
        return null;
    }
    private static Method someMethod() {
        try {
            return JvmReflectionTest.class.getDeclaredMethod("someMethod", Object.class, Map.class, int[].class, String[][].class, List[].class, OneInterface.class);
        } catch (final NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
    private static @NotNull MethodSignature someMethodSig() {
        final MethodSignature sig = MethodSignature.parse(someMethodSig);
        // TODO replace with proper binder when implemented
        return sig.bind(var -> {
            for (final TypeParameter param : sig.getTypeParameters()) {
                if (param.getName().equals(var)) {
                    return param;
                }
            }
            return null;
        });
    }

    @SuppressWarnings("unused")
    interface OneInterface<T> {}
    interface TwoInterface {}

    @SuppressWarnings("unused")
    private static class TypeHolder implements OneInterface<String> {
    }

    @Test
    void testMethodDescriptor() {
        final Method someMethod = someMethod();
        final MethodDescriptor expected = someMethodSig().asDescriptor();

        assertEquals(expected, MethodDescriptor.of(someMethod));
    }

    @Test
    void testTypeSignatureSimple() {
        assertEquals(ClassTypeSignature.of("java/lang/String"), TypeSignature.of(String.class));
        assertEquals(
            TypeSignature.parse("Ljava/util/List<TE;>;").bind(TypeVariableBinder.object()),
            TypeSignature.of(List.class)
        );
    }

    @Test
    void testTypeSignatureGeneric() {
        final TypeSignature expected = TypeSignature.parse("Ljava/util/List<Ljava/lang/String;>;");
        final TypeSignature actual = TypeSignature.of(new TypeToken<List<String>>() {}.getType());

        assertEquals(expected, actual);
    }

    @Test
    void testTypeSignatureAnnotatedGeneric() {
    }
}
