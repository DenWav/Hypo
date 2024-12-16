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

import dev.denwav.hypo.types.intern.Intern;
import dev.denwav.hypo.types.sig.ArrayTypeSignature;
import dev.denwav.hypo.types.sig.ClassTypeSignature;
import dev.denwav.hypo.types.sig.MethodSignature;
import dev.denwav.hypo.types.sig.ThrowsSignature;
import dev.denwav.hypo.types.sig.TypeSignature;
import dev.denwav.hypo.types.sig.param.TypeParameter;
import dev.denwav.hypo.types.sig.param.TypeVariable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MethodSignatureTest {

    @Test
    void testMethodSig() throws ExecutionException, InterruptedException {
        final var primitives = List.of(PrimitiveType.values());

        final List<TypeMapping<TypeSignature>> classTypes = List.of(
            TypeMapping.of(ClassTypeSignature.of("java/lang/Object")),
            TypeMapping.of(ClassTypeSignature.of("org/gradle/internal/execution/history/impl/SerializableFileCollectionFingerprint")),
            TypeMapping.of(
                TypeParameter.of("T"),
                ClassTypeSignature.of("java/util/List", List.of(TypeVariable.unbound("T")))
            ),
            TypeMapping.of(
                List.of(TypeParameter.of("T1", null, List.of(ClassTypeSignature.of("java/util/Collection"))), TypeParameter.of("T2", TypeVariable.unbound("T1"))),
                List.of(TypeVariable.unbound("T1"), TypeVariable.unbound("T2"))
            ),
            TypeMapping.of(
                List.of(TypeParameter.of("K", ClassTypeSignature.of("java/lang/Object")), TypeParameter.of("V", ClassTypeSignature.of("java/lang/CharSequence"))),
                ClassTypeSignature.of("java/util/Map", List.of(TypeVariable.unbound("K"), TypeVariable.unbound("V")))
            )
        );

        final List<TypeMapping<? extends TypeSignature>> parameters = Stream
            .concat(
                primitives.stream().map(TypeMapping::of),
                classTypes.stream()
            )
            .flatMap(t ->
                Stream.of(
                    t,
                    TypeMapping.of(t.params(), t.types().stream().map(t1 -> ArrayTypeSignature.of(1, t1)).toList()),
                    TypeMapping.of(t.params(), t.types().stream().map(t1 -> ArrayTypeSignature.of(2, t1)).toList())
                )
            )
            .toList();

        final List<TypeMapping<? extends TypeSignature>> returnTypes = Stream.concat(
                Stream
                    .concat(
                        primitives.stream().map(TypeMapping::of),
                        classTypes.stream().filter(t -> t.types().size() == 1)
                    ),
                Stream.of(TypeMapping.of(VoidType.INSTANCE))
            )
            .flatMap(t -> {
                if (t.types().getFirst() == VoidType.INSTANCE) {
                    return Stream.of(t);
                }
                return Stream.of(
                    t,
                    TypeMapping.of(t.params(), t.types().stream().map(t1 -> ArrayTypeSignature.of(1, t1)).toList()),
                    TypeMapping.of(t.params(), t.types().stream().map(t1 -> ArrayTypeSignature.of(2, t1)).toList())
                );
            })
            .toList();

        final List<TypeMapping<ThrowsSignature>> throwsTypes = List.of(
            TypeMapping.of(ClassTypeSignature.of("java/lang/Exception")),
            TypeMapping.of(
                TypeParameter.of("X", ClassTypeSignature.of("java/lang/RuntimeException")),
                TypeVariable.unbound("X")
            ),
            TypeMapping.of(
                TypeParameter.of("X", ClassTypeSignature.of("java/lang/RuntimeException")),
                List.of(ClassTypeSignature.of("java/sql/SqlException"), TypeVariable.unbound("X"))
            )
        );

        final AtomicLong counter = new AtomicLong(0);

        try (final ExecutorService pool = Executors.newWorkStealingPool()) {
            final ArrayList<Future<?>> tasks = new ArrayList<>();

            for (final TypeMapping<? extends TypeSignature> param1 : parameters) {
                tasks.add(pool.submit(() -> {
                    for (final TypeMapping<? extends TypeSignature> param2 : parameters) {
                        for (final TypeMapping<? extends TypeSignature> param3 : parameters) {
                            final var paramTypes = Stream.of(param1, param2, param3)
                                .flatMap(t -> t.types().stream())
                                .toList();

                            for (final TypeMapping<? extends TypeSignature> returnType : returnTypes) {
                                for (final TypeMapping<ThrowsSignature> throwsType : throwsTypes) {
                                    final var typeParams = Stream.of(param1, param2, param3, returnType, throwsType)
                                        .flatMap(t -> t.params().stream())
                                        .toList();

                                    final MethodSignature sig = MethodSignature.of(typeParams, paramTypes, returnType.types().getFirst(), throwsType.types());
                                    Assertions.assertEquals(sig, MethodSignature.parse(sig.asInternal()), "Internal " + sig.asInternal());

                                    final long count = counter.incrementAndGet();
                                    if (count % 1_000_000L == 0L) {
                                        System.out.printf("Tested %,d permutations...%n", count);
                                        System.out.printf("MethodSignature internment: %,d %n", Intern.internmentSize(MethodSignature.class));
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

    @SuppressWarnings("unused")
    private record TypeMapping<T>(
        @NotNull List<TypeParameter> params,
        @NotNull List<T> types
    ) {
        public static <T> TypeMapping<T> of(final @NotNull List<TypeParameter> params, final @NotNull List<T> types) {
            return new TypeMapping<>(params, types);
        }

        public static <T> TypeMapping<T> of(final @NotNull TypeParameter param, final @NotNull List<T> types) {
            return new TypeMapping<>(List.of(param), types);
        }

        public static <T> TypeMapping<T> of(final @NotNull List<T> types) {
            return new TypeMapping<>(List.of(), types);
        }

        public static <T> TypeMapping<T> of(final @NotNull List<TypeParameter> params, final @NotNull T type) {
            return new TypeMapping<>(params, List.of(type));
        }

        public static <T> TypeMapping<T> of(final @NotNull TypeParameter param, final @NotNull T type) {
            return new TypeMapping<>(List.of(param), List.of(type));
        }

        public static <T> TypeMapping<T> of(final @NotNull T type) {
            return new TypeMapping<>(List.of(), List.of(type));
        }
    }
}
