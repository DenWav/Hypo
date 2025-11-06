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

import dev.denwav.hypo.types.sig.ClassSignature;
import dev.denwav.hypo.types.sig.ClassTypeSignature;
import dev.denwav.hypo.types.sig.param.BoundedTypeArgument;
import dev.denwav.hypo.types.sig.param.TypeParameter;
import dev.denwav.hypo.types.sig.param.TypeVariable;
import dev.denwav.hypo.types.sig.param.WildcardArgument;
import dev.denwav.hypo.types.sig.param.WildcardBound;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("unused")
class ClassSignatureTest {

    @Test
    void testCollectionGeneric() {
        final String s = "<E:Ljava/lang/Object;>Ljava/lang/Object;Ljava/lang/Iterable<TE;>;";

        final var expected = ClassSignature.of(
            List.of(
                TypeParameter.of("E")
            ),
            ClassTypeSignature.of("java/lang/Object"),
            List.of(
                ClassTypeSignature.of("java/lang/Iterable", List.of(TypeVariable.unbound("E")))
            )
        );

        final var actual = ClassSignature.parse(s);
        assertEquals(expected, actual);
        assertEquals(s, actual.asInternal());
    }

    @Test
    void testDoubleGeneric() {
        final String s = "<K:Ljava/lang/Object;V:Ljava/lang/Object;>Ljava/util/Collections$CheckedSortedMap<TK;TV;>;Ljava/util/NavigableMap<TK;TV;>;Ljava/io/Serializable;";

        final var expected = ClassSignature.of(
            List.of(
                TypeParameter.of("K"),
                TypeParameter.of("V")
            ),
            ClassTypeSignature.of("java/util/Collections$CheckedSortedMap", List.of(TypeVariable.unbound("K"), TypeVariable.unbound("V"))),
            List.of(
                ClassTypeSignature.of("java/util/NavigableMap", List.of(TypeVariable.unbound("K"), TypeVariable.unbound("V"))),
                ClassTypeSignature.of("java/io/Serializable")
            )
        );

        final var actual = ClassSignature.parse(s);
        assertEquals(expected, actual);
        assertEquals(s, actual.asInternal());
    }

    @Test
    void testTypeParamInterfaceBound() {
        final String s = "<D::Ljava/time/chrono/ChronoLocalDate;>Ljava/lang/Object;Ljava/time/chrono/ChronoLocalDate;Ljava/time/temporal/Temporal;Ljava/time/temporal/TemporalAdjuster;Ljava/io/Serializable;";

        final var expected = ClassSignature.of(
            List.of(
                TypeParameter.of("D", null, List.of(ClassTypeSignature.of("java/time/chrono/ChronoLocalDate")))
            ),
            ClassTypeSignature.of("java/lang/Object"),
            List.of(
                ClassTypeSignature.of("java/time/chrono/ChronoLocalDate"),
                ClassTypeSignature.of("java/time/temporal/Temporal"),
                ClassTypeSignature.of("java/time/temporal/TemporalAdjuster"),
                ClassTypeSignature.of("java/io/Serializable")
            )
        );

        final var actual = ClassSignature.parse(s);
        assertEquals(expected, actual);
        assertEquals(s, actual.asInternal());
    }

    @Test
    void testNestedArgument() {
        final String s = "Ljava/lang/Object;Lsun/util/locale/provider/LocaleServiceProviderPool$LocalizedObjectGetter<Ljava/util/spi/CalendarNameProvider;Ljava/util/Map<Ljava/lang/String;Ljava/lang/Integer;>;>;";

        final var expected = ClassSignature.of(
            List.of(),
            ClassTypeSignature.of("java/lang/Object"),
            List.of(
                ClassTypeSignature.of(
                    "sun/util/locale/provider/LocaleServiceProviderPool$LocalizedObjectGetter",
                    List.of(
                        ClassTypeSignature.of("java/util/spi/CalendarNameProvider"),
                        ClassTypeSignature.of(
                            "java/util/Map",
                            List.of(ClassTypeSignature.of("java/lang/String"),
                                ClassTypeSignature.of("java/lang/Integer"))
                        )
                    )
                )
            )
        );

        final var actual = ClassSignature.parse(s);
        assertEquals(expected, actual);
        assertEquals(s, actual.asInternal());
    }

    @Test
    void testWildcard() {
        final String s = "<T:Ljava/lang/Object;>Ljava/lang/Object;Ljava/io/Serializable;Ljava/lang/reflect/GenericDeclaration;Ljava/lang/reflect/Type;Ljava/lang/reflect/AnnotatedElement;Ljava/lang/invoke/TypeDescriptor$OfField<Ljava/lang/Class<*>;>;Ljava/lang/constant/Constable;";

        final var expected = ClassSignature.of(
            List.of(
                TypeParameter.of("T")
            ),
            ClassTypeSignature.of("java/lang/Object"),
            List.of(
                ClassTypeSignature.of("java/io/Serializable"),
                ClassTypeSignature.of("java/lang/reflect/GenericDeclaration"),
                ClassTypeSignature.of("java/lang/reflect/Type"),
                ClassTypeSignature.of("java/lang/reflect/AnnotatedElement"),
                ClassTypeSignature.of(
                    "java/lang/invoke/TypeDescriptor$OfField",
                    List.of(ClassTypeSignature.of("java/lang/Class", List.of(WildcardArgument.INSTANCE)))
                ),
                ClassTypeSignature.of("java/lang/constant/Constable")
            )
        );

        final var actual = ClassSignature.parse(s);
        assertEquals(expected, actual);
        assertEquals(s, actual.asInternal());
    }

    @Test
    void testPlus() {
        final String s = "Lcom/sun/source/util/SimpleDocTreeVisitor<Ljava/util/List<+Lcom/sun/source/doctree/DocTree;>;Ljava/lang/Void;>;";

        final var expected = ClassSignature.of(
            List.of(),
            ClassTypeSignature.of(
                "com/sun/source/util/SimpleDocTreeVisitor",
                List.of(
                    ClassTypeSignature.of(
                        "java/util/List",
                        List.of(BoundedTypeArgument.of(WildcardBound.UPPER, ClassTypeSignature.of("com/sun/source/doctree/DocTree")))
                    ),
                    ClassTypeSignature.of("java/lang/Void")
                )
            ),
            List.of()
        );

        final var actual = ClassSignature.parse(s);
        assertEquals(expected, actual);
        assertEquals(s, actual.asInternal());
    }

    @Test
    void testMinus() {
        final String s = "<S::Ljava/util/concurrent/Flow$Subscriber<-Ljava/util/List<Ljava/nio/ByteBuffer;>;>;R:Ljava/lang/Object;>Ljava/lang/Object;Ljdk/internal/net/http/ResponseSubscribers$TrustedSubscriber<TR;>;";

        final var expected = ClassSignature.of(
            List.of(
                TypeParameter.of(
                    "S",
                    null,
                    List.of(
                        ClassTypeSignature.of(
                            "java/util/concurrent/Flow$Subscriber",
                            List.of(
                                BoundedTypeArgument.of(
                                    WildcardBound.LOWER,
                                    ClassTypeSignature.of("java/util/List", List.of(ClassTypeSignature.of("java/nio/ByteBuffer")))
                                )
                            )
                        )
                    )
                ),
                TypeParameter.of("R")
            ),
            ClassTypeSignature.of("java/lang/Object"),
            List.of(
                ClassTypeSignature.of("jdk/internal/net/http/ResponseSubscribers$TrustedSubscriber", List.of(TypeVariable.unbound("R")))
            )
        );

        final var actual = ClassSignature.parse(s);
        assertEquals(expected, actual);
        assertEquals(s, actual.asInternal());
    }
}
