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

package dev.denwav.hypo.types.pattern;

import dev.denwav.hypo.types.PrimitiveType;
import dev.denwav.hypo.types.TypeRepresentable;
import dev.denwav.hypo.types.VoidType;
import dev.denwav.hypo.types.desc.ArrayTypeDescriptor;
import dev.denwav.hypo.types.desc.ClassTypeDescriptor;
import dev.denwav.hypo.types.desc.MethodDescriptor;
import dev.denwav.hypo.types.desc.TypeDescriptor;
import dev.denwav.hypo.types.parsing.JvmTypeParser;
import dev.denwav.hypo.types.sig.ArrayTypeSignature;
import dev.denwav.hypo.types.sig.ClassSignature;
import dev.denwav.hypo.types.sig.ClassTypeSignature;
import dev.denwav.hypo.types.sig.MethodSignature;
import dev.denwav.hypo.types.sig.TypeSignature;
import dev.denwav.hypo.types.sig.param.BoundedTypeArgument;
import dev.denwav.hypo.types.sig.param.TypeParameter;
import dev.denwav.hypo.types.sig.param.TypeVariable;
import dev.denwav.hypo.types.sig.param.WildcardArgument;
import dev.denwav.hypo.types.sig.param.WildcardBound;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TypePatternTest {

    public static final String STRING = "java/lang/String";
    public static final String LIST = "java/util/List";
    public static final ClassTypeDescriptor STRING_DESC = ClassTypeDescriptor.of(STRING);
    public static final ClassTypeSignature STRING_SIG = ClassTypeSignature.of(STRING);
    public static final ClassTypeSignature LIST_SIG = ClassTypeSignature.of(LIST, List.of(ClassTypeSignature.of(STRING)));

    public static final TypeSignature NESTED_SIG = JvmTypeParser.parseTypeSignature("Ldev/denwav/hypo/types/pattern/A<TT;>.B<TR;>;", 0);

    public static final ArrayTypeDescriptor ARRAY_DESC = ArrayTypeDescriptor.of(1, PrimitiveType.BYTE);
    public static final ArrayTypeSignature ARRAY_SIG = ArrayTypeSignature.of(1, PrimitiveType.BYTE);

    public static final TypeParameter TYPE_PARAM = TypeParameter.of("T");

    public static final TypeVariable TYPE_VAR = TypeVariable.of(TYPE_PARAM);
    public static final TypeVariable.Unbound TYPE_VAR_UNBOUND = TypeVariable.unbound("T");

    public static Stream<TypeRepresentable> assignableTypes() {
        return Stream.of(
            PrimitiveType.INT,
            STRING_DESC,
            ARRAY_DESC,
            STRING_SIG,
            LIST_SIG,
            NESTED_SIG,
            ARRAY_SIG,
            TYPE_VAR,
            TYPE_VAR_UNBOUND
        );
    }

    public static Stream<TypeRepresentable> returnableTypes() {
        return Stream.concat(assignableTypes(), Stream.of(VoidType.INSTANCE));
    }

    public static Stream<TypeRepresentable> methodTypes() {
        return Stream.of(
            MethodDescriptor.of(List.of(), VoidType.INSTANCE),
            MethodSignature.of(List.of(), List.of(), VoidType.INSTANCE, List.of())
        );
    }

    public static Stream<TypeRepresentable> otherTypes() {
        return Stream.of(
            TYPE_PARAM,
            WildcardArgument.INSTANCE,
            BoundedTypeArgument.of(WildcardBound.UPPER, STRING_SIG),
            ClassSignature.of(List.of(), STRING_SIG, List.of())
        );
    }

    public static Stream<TypeRepresentable> allTypes() {
        return Stream.of(returnableTypes(), methodTypes(), otherTypes()).flatMap(Function.identity());
    }

    private final List<TestBuilder> tests = new ArrayList<>();
    private Stream<DynamicNode> tests() {
        return this.tests.stream().map(TestBuilder::build);
    }

    private TestBuilder test(final TypePattern pattern) {
        final TestBuilder test = new TestBuilder(pattern);
        this.tests.add(test);
        return test;
    }

    @TestFactory
    @DisplayName("Basic Type Pattern")
    Stream<DynamicNode> basicTypes() {
        this.test(TypePatterns.isType())
            .matches(returnableTypes());

        return this.tests();
    }

    @TestFactory
    @DisplayName("Primitive Type Patterns")
    Stream<DynamicNode> primitivePatterns() {
        this.test(TypePatterns.isBoolean()).matches(PrimitiveType.BOOLEAN);
        this.test(TypePatterns.isChar()).matches(PrimitiveType.CHAR);
        this.test(TypePatterns.isByte()).matches(PrimitiveType.BYTE);
        this.test(TypePatterns.isShort()).matches(PrimitiveType.SHORT);
        this.test(TypePatterns.isInt()).matches(PrimitiveType.INT);
        this.test(TypePatterns.isLong()).matches(PrimitiveType.LONG);
        this.test(TypePatterns.isFloat()).matches(PrimitiveType.FLOAT);
        this.test(TypePatterns.isDouble()).matches(PrimitiveType.DOUBLE);
        this.test(TypePatterns.isVoid()).matches(VoidType.INSTANCE);

        this.test(TypePatterns.isPrimitive()).matches(PrimitiveType.values());

        this.test(TypePatterns.isIntegerType())
            .matches(PrimitiveType.BYTE, PrimitiveType.SHORT, PrimitiveType.INT, PrimitiveType.LONG);
        this.test(TypePatterns.isFloatingPointType())
            .matches(PrimitiveType.FLOAT, PrimitiveType.DOUBLE);

        this.test(TypePatterns.isWide())
            .matches(PrimitiveType.LONG, PrimitiveType.DOUBLE);

        return this.tests();
    }

    @TestFactory
    @DisplayName("Type Assignability Patterns")
    Stream<DynamicNode> testAssignability() {
        this.test(TypePatterns.isAssignable())
            .matches(assignableTypes())
            .notMatches(VoidType.INSTANCE)
            .notMatches(Stream.concat(methodTypes(), otherTypes()));

        this.test(TypePatterns.isReturnable())
            .matches(returnableTypes())
            .notMatches(Stream.concat(methodTypes(), otherTypes()));

        return this.tests();
    }

    @TestFactory
    @DisplayName("Class Patterns")
    Stream<DynamicNode> testClasses() {
        this.test(TypePatterns.isClass())
            .matches(STRING_DESC, STRING_SIG, LIST_SIG, NESTED_SIG);

        this.test(TypePatterns.isClassNamed(STRING))
            .matches(STRING_DESC, STRING_SIG);

        this.test(TypePatterns.isClassNamed(LIST))
            .matches(LIST_SIG);

        this.test(TypePatterns.isClassNamed(LIST::equals))
            .matches(LIST_SIG);

        this.test(TypePatterns.isReferenceType())
            .matches(assignableTypes().filter(isNot(PrimitiveType.class)));

        return this.tests();
    }

    @TestFactory
    @DisplayName("Array Patterns")
    Stream<DynamicNode> testArray() {
        this.test(TypePatterns.isArray()).matches(ARRAY_DESC, ARRAY_SIG);
        this.test(TypePatterns.isArray(1)).matches(ARRAY_DESC, ARRAY_SIG);
        this.test(TypePatterns.isArray(TypePatterns.isByte())).matches(ARRAY_DESC, ARRAY_SIG);
        this.test(TypePatterns.isArray(1, TypePatterns.isByte())).matches(ARRAY_DESC, ARRAY_SIG);
        this.test(TypePatterns.isArray(1, TypePatterns.isClass())).notMatches(ARRAY_DESC, ARRAY_SIG);

        return this.tests();
    }

    @TestFactory
    @DisplayName("TypeDescriptor Pattern")
    Stream<DynamicNode> testTypeDesc() {
        this.test(TypePatterns.isTypeDescriptor())
            .matches(STRING_DESC, ARRAY_DESC)
            .matches(PrimitiveType.values())
            .matches(VoidType.INSTANCE);

        return this.tests();
    }

    @TestFactory
    @DisplayName("TypeSignature Pattern")
    Stream<DynamicNode> testTypeSig() {
        this.test(TypePatterns.isTypeSignature())
            .matches(STRING_SIG, LIST_SIG, NESTED_SIG, ARRAY_SIG, TYPE_VAR, TYPE_VAR_UNBOUND)
            .matches(PrimitiveType.values())
            .matches(VoidType.INSTANCE);

        this.test(TypePatterns.isTypeSignature(s -> s.asInternal().equals("L" + STRING + ";")))
            .matches(STRING_SIG);

        return this.tests();
    }

    @TestFactory
    @DisplayName("TypeSignature::TypeArgument Patterns")
    Stream<DynamicNode> testTypeArgs() {
        this.test(TypePatterns.hasTypeArguments())
            .matches(LIST_SIG, NESTED_SIG);

        this.test(TypePatterns.hasNoTypeArguments())
            .matches(STRING_DESC, STRING_SIG);

        this.test(TypePatterns.hasTypeArguments(TypePatterns.isClass()))
            .matches(LIST_SIG);
        this.test(TypePatterns.hasTypeArguments(TypePatterns.isClassNamed(STRING)))
            .matches(LIST_SIG);
        this.test(TypePatterns.hasTypeArguments(TypePatterns.isClassNamed(LIST)))
            .notMatches(allTypes());

        return this.tests();
    }

    @TestFactory
    @DisplayName("TypeSignature::Owner Pattern")
    Stream<DynamicNode> testOwnerIs() {
        this.test(TypePatterns.ownerIs(TypePattern.any()))
            .matches(NESTED_SIG);

        return this.tests();
    }

    private static Predicate<TypeRepresentable> is(final Class<? extends TypeRepresentable> clazz) {
        return t -> clazz.isAssignableFrom(t.getClass());
    }
    private static Predicate<TypeRepresentable> isNot(final Class<? extends TypeRepresentable> clazz) {
        return is(clazz).negate();
    }

    static class TestBuilder {
        private final TypePattern pattern;

        private final List<TypeRepresentable> ensureSafe = new ArrayList<>();
        private final List<TypeRepresentable> testMatches = new ArrayList<>();
        private final List<TypeRepresentable> testNotMatches = new ArrayList<>();

        TestBuilder(final TypePattern pattern) {
            this.pattern = pattern;
        }

        TestBuilder safe(final TypeRepresentable... test) {
            return this.safe(Arrays.asList(test));
        }
        TestBuilder safe(final Stream<? extends TypeRepresentable> test) {
            return this.safe(test.toList());
        }
        TestBuilder safe(final Iterable<? extends TypeRepresentable> test) {
            for (final TypeRepresentable t : test) {
                this.ensureSafe.add(t);
            }
            return this;
        }

        TestBuilder matches(final TypeRepresentable... test) {
            return this.matches(Arrays.asList(test));
        }
        TestBuilder matches(final Stream<? extends TypeRepresentable> test) {
            return this.matches(test.toList());
        }
        TestBuilder matches(final Iterable<? extends TypeRepresentable> test) {
            for (final TypeRepresentable t : test) {
                this.testMatches.add(t);
            }
            return this;
        }

        TestBuilder notMatches(final TypeRepresentable... test) {
            return this.notMatches(Arrays.asList(test));
        }
        TestBuilder notMatches(final Stream<? extends TypeRepresentable> test) {
            return this.notMatches(test.toList());
        }
        TestBuilder notMatches(final Iterable<? extends TypeRepresentable> test) {
            for (final TypeRepresentable t : test) {
                this.testNotMatches.add(t);
            }
            return this;
        }

        enum Expect {
            MATCH,
            NOT_MATCH,
            IGNORE,
        }
        private Stream<DynamicNode> test(final List<TypeRepresentable> tests, final Expect expect) {
            return tests.stream()
                .map(t -> {
                    return DynamicTest.dynamicTest(t.asReadable(), () -> {
                        switch (expect) {
                            case MATCH -> assertTrue(this.pattern.match(t).matches(), "Pattern should match: " + t);
                            case NOT_MATCH -> assertFalse(this.pattern.match(t).matches(), "Pattern should not match: " + t);
                            case IGNORE -> assertDoesNotThrow(() -> this.pattern.match(t), "Pattern should not fail: " + t);
                        }
                    });
                });
        }

        DynamicNode build() {
            this.safe(allTypes());
            this.notMatches(allTypes()
                .filter(Predicate.not(this.testMatches::contains))
                .toList());

            return DynamicContainer.dynamicContainer(this.getPatternName(), Stream.of(
                DynamicContainer.dynamicContainer("Safe", this.test(this.ensureSafe, Expect.IGNORE)),
                DynamicContainer.dynamicContainer("Should Match", this.test(this.testMatches, Expect.MATCH)),
                DynamicContainer.dynamicContainer("Should Not Match", this.test(this.testNotMatches, Expect.NOT_MATCH))
            ));
        }

        private String getPatternName() {
            try {
                final Method writeReplace = this.pattern.getClass().getDeclaredMethod("writeReplace");
                writeReplace.setAccessible(true);
                final SerializedLambda sl = (SerializedLambda) writeReplace.invoke(this.pattern);

                final String ownedClass = sl.getImplClass();
                final String simpleName = ownedClass.substring(ownedClass.lastIndexOf('/') + 1);

                final String fullMethodName = sl.getImplMethodName();
                final String methodName = fullMethodName.split("\\$", 3)[1];

                final MethodDescriptor methodImpl = MethodDescriptor.parse(sl.getImplMethodSignature());
                final int captured = methodImpl.getParameters().size() - 2;
                final ArrayList<TypeDescriptor> capturedTypes = new ArrayList<>();
                for (int i = 0; i < captured; i++) {
                    capturedTypes.add(methodImpl.getParameters().get(i));
                }

                final var capturedParams = Stream.of(Class.forName(ownedClass.replace('/', '.')).getMethods())
                    .map(MethodDescriptor::of)
                    .map(MethodDescriptor::getParameters)
                    .filter(capturedTypes::equals)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Could not find matching method"));

                if (capturedParams.isEmpty()) {
                    // zero-width space `\u200B` is needed for IntelliJ to render the () in the test output window
                    return simpleName + "." + methodName + "(\u200B)";
                } else {
                    final List<String> p = capturedParams.stream()
                        .map(TypeRepresentable::asReadable)
                        .map(s -> s.substring(s.lastIndexOf('.') + 1))
                        .toList();
                    final String paramList = String.join(", ", p);
                    return simpleName + "." + methodName + "(" + paramList + ")";
                }
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
