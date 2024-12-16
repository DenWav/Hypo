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
import dev.denwav.hypo.types.desc.MethodDescriptor;
import dev.denwav.hypo.types.desc.TypeDescriptor;
import dev.denwav.hypo.types.intern.Intern;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("MethodDescriptor Tests")
class MethodDescriptorTest {

    @Test
    @DisplayName("Test MethodDescriptor.parse() and MethodDescriptor.asInternal()")
    void testParse() throws ExecutionException, InterruptedException {
        final TypeDescriptor[] testTypes = Arrays.stream(new TypeDescriptor[]{
            PrimitiveType.CHAR,
            PrimitiveType.BYTE,
            PrimitiveType.SHORT,
            PrimitiveType.INT,
            PrimitiveType.LONG,
            PrimitiveType.FLOAT,
            PrimitiveType.DOUBLE,
            PrimitiveType.BOOLEAN,
            ClassTypeDescriptor.of("java/lang/Object"),
            ClassTypeDescriptor.of("java/lang/String"),
        }).flatMap(t -> {
            return Stream.of(t, ArrayTypeDescriptor.of(1, t), ArrayTypeDescriptor.of(2, t));
        }).toArray(TypeDescriptor[]::new);

        final ArrayList<TypeDescriptor> returnTypeList = new ArrayList<>(Arrays.asList(testTypes));
        returnTypeList.add(VoidType.INSTANCE);
        final TypeDescriptor[] returnTypes = returnTypeList.toArray(new TypeDescriptor[0]);

        final TypeDescriptor[] params = new TypeDescriptor[4];
        final List<TypeDescriptor> paramList = Arrays.asList(params);

        final AtomicLong counter = new AtomicLong(0);

        try (final ExecutorService pool = Executors.newWorkStealingPool()) {
            final ArrayList<Future<?>> tasks = new ArrayList<>();

            for (final TypeDescriptor param0 : testTypes) {
                tasks.add(pool.submit(() -> {
                    for (final TypeDescriptor param1 : testTypes) {
                        for (final TypeDescriptor param2 : testTypes) {
                            for (final TypeDescriptor param3 : testTypes) {
                                params[0] = param0;
                                params[1] = param1;
                                params[2] = param2;
                                params[3] = param3;

                                for (final TypeDescriptor returnType : returnTypes) {
                                    final MethodDescriptor dec = MethodDescriptor.of(paramList, returnType);
                                    Assertions.assertEquals(dec, MethodDescriptor.parse(dec.asInternal()));

                                    final long count = counter.incrementAndGet();
                                    if (count % 1_000_000L == 0L) {
                                        System.out.printf("Tested %,d permutations...%n", count);
                                        System.out.printf("MethodDescriptor internment: %,d %n", Intern.internmentSize(MethodDescriptor.class));
                                    }
                                }
                            }
                        }
                    }
                }));
            }

            for (final Future<?> task : tasks) {
                task.get();
            }
        }
    }
}
