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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package dev.denwav.hypo.types.pattern;

import dev.denwav.hypo.types.PrimitiveType;
import dev.denwav.hypo.types.TypeRepresentable;
import dev.denwav.hypo.types.desc.MethodDescriptor;
import dev.denwav.hypo.types.parsing.JvmTypeParseFailureException;
import dev.denwav.hypo.types.sig.ClassSignature;
import dev.denwav.hypo.types.sig.ClassTypeSignature;
import dev.denwav.hypo.types.sig.MethodSignature;
import dev.denwav.hypo.types.sig.param.BoundedTypeArgument;
import dev.denwav.hypo.types.sig.param.TypeParameter;
import dev.denwav.hypo.types.sig.param.TypeVariable;
import dev.denwav.hypo.types.sig.param.WildcardArgument;
import dev.denwav.hypo.types.sig.param.WildcardBound;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("[types] Pattern Helper Tests")
class PatternHelpersTest {

    private static DynamicTest matches(final String label, final TypePattern pattern, final TypeRepresentable type) {
        return DynamicTest.dynamicTest(label + " - matches", () ->
            assertTrue(pattern.match(type).matches(), label + " should match " + type));
    }

    private static DynamicTest notMatches(final String label, final TypePattern pattern, final TypeRepresentable type) {
        return DynamicTest.dynamicTest(label + " - does not match", () ->
            assertFalse(pattern.match(type).matches(), label + " should not match " + type));
    }

    // -------------------------------------------------------------------------
    // ClassSignaturePatterns
    // -------------------------------------------------------------------------

    @TestFactory
    @DisplayName("ClassSignaturePatterns - isClassSignature, hasTypeParameters, hasSuperClass, hasSuperInterfaces")
    Stream<DynamicNode> classSignaturePatterns() throws JvmTypeParseFailureException {
        final ClassSignature withParams = ClassSignature.parse(
            "<T:Ljava/lang/Object;>Ljava/lang/Object;Ljava/io/Serializable;");
        final ClassSignature withoutParams = ClassSignature.parse("Ljava/lang/Object;");

        return Stream.of(
            DynamicContainer.dynamicContainer("isClassSignature", Stream.of(
                matches("isClassSignature()",          ClassSignaturePatterns.isClassSignature(), withParams),
                notMatches("isClassSignature()",       ClassSignaturePatterns.isClassSignature(), PrimitiveType.INT)
            )),
            DynamicContainer.dynamicContainer("hasTypeParameters / hasNoTypeParameters", Stream.of(
                matches("hasTypeParameters()",         ClassSignaturePatterns.hasTypeParameters(), withParams),
                notMatches("hasTypeParameters()",      ClassSignaturePatterns.hasTypeParameters(), withoutParams),
                matches("hasNoTypeParameters()",       ClassSignaturePatterns.hasNoTypeParameters(), withoutParams),
                notMatches("hasNoTypeParameters()",    ClassSignaturePatterns.hasNoTypeParameters(), withParams)
            )),
            DynamicContainer.dynamicContainer("hasSuperClass", Stream.of(
                matches("hasSuperClass(isClassNamed)",
                    ClassSignaturePatterns.hasSuperClass(TypePatterns.isClassNamed("java/lang/Object")),
                    withParams),
                notMatches("hasSuperClass(isClassNamed) - wrong name",
                    ClassSignaturePatterns.hasSuperClass(TypePatterns.isClassNamed("java/lang/String")),
                    withParams)
            )),
            DynamicContainer.dynamicContainer("hasSuperInterfaces / hasNoSuperInterfaces", Stream.of(
                matches("hasSuperInterfaces()",        ClassSignaturePatterns.hasSuperInterfaces(), withParams),
                notMatches("hasSuperInterfaces()",     ClassSignaturePatterns.hasSuperInterfaces(), withoutParams),
                matches("hasNoSuperInterfaces()",      ClassSignaturePatterns.hasNoSuperInterfaces(), withoutParams),
                notMatches("hasNoSuperInterfaces()",   ClassSignaturePatterns.hasNoSuperInterfaces(), withParams),
                matches("hasSuperInterfaces(pattern)", ClassSignaturePatterns.hasSuperInterfaces(
                    TypePatterns.isClassNamed("java/io/Serializable")), withParams)
            ))
        );
    }

    // -------------------------------------------------------------------------
    // MethodPatterns (instance methods)
    // -------------------------------------------------------------------------

    @TestFactory
    @DisplayName("MethodPatterns - hasParams, hasReturnType, hasThrows, hasTypeParameters")
    Stream<DynamicNode> methodPatterns() throws JvmTypeParseFailureException {
        final MethodPatterns mp = new MethodPatterns();
        final MethodDescriptor noParams  = MethodDescriptor.parse("()V");
        final MethodDescriptor oneParam  = MethodDescriptor.parse("(I)I");
        final MethodSignature  withThrows = MethodSignature.parse("()V^Ljava/lang/Exception;");
        final MethodSignature  noThrows   = MethodSignature.parse("()V");
        final MethodSignature  withTParam = MethodSignature.parse("<T:Ljava/lang/Object;>()V");

        return Stream.of(
            DynamicContainer.dynamicContainer("hasParams / hasNoParams", Stream.of(
                matches("hasParams()",    mp.hasParams(), oneParam),
                notMatches("hasParams()", mp.hasParams(), noParams),
                matches("hasNoParams()",  mp.hasNoParams(), noParams),
                notMatches("hasNoParams()", mp.hasNoParams(), oneParam),
                matches("hasParams(TypePattern...)", mp.hasParams(TypePatterns.isPrimitive()), oneParam),
                notMatches("hasParams(TypePattern...) - wrong count", mp.hasParams(TypePatterns.isPrimitive(), TypePatterns.isPrimitive()), oneParam)
            )),
            DynamicContainer.dynamicContainer("hasReturnType", Stream.of(
                matches("hasReturnType(isVoid)",    mp.hasReturnType(TypePatterns.isVoid()), noParams),
                notMatches("hasReturnType(isVoid)", mp.hasReturnType(TypePatterns.isVoid()), oneParam)
            )),
            DynamicContainer.dynamicContainer("hasThrows / hasNoThrows", Stream.of(
                matches("hasThrows()",             mp.hasThrows(), withThrows),
                notMatches("hasThrows()",          mp.hasThrows(), noThrows),
                matches("hasNoThrows()",           mp.hasNoThrows(), noThrows),
                notMatches("hasNoThrows()",        mp.hasNoThrows(), withThrows),
                matches("hasThrows(TypePattern...)", mp.hasThrows(TypePatterns.isClassNamed("java/lang/Exception")), withThrows)
            )),
            DynamicContainer.dynamicContainer("hasTypeParameters (static)", Stream.of(
                matches("hasTypeParameters()",       MethodPatterns.hasTypeParameters(), withTParam),
                notMatches("hasTypeParameters()",    MethodPatterns.hasTypeParameters(), noThrows),
                matches("hasNoTypeParameters()",     MethodPatterns.hasNoTypeParameters(), noThrows),
                notMatches("hasNoTypeParameters()",  MethodPatterns.hasNoTypeParameters(), withTParam)
            ))
        );
    }

    // -------------------------------------------------------------------------
    // TypeArgumentPatterns
    // -------------------------------------------------------------------------

    @TestFactory
    @DisplayName("TypeArgumentPatterns - isTypeArgument, isWildcard, isBoundedArgument, hasUpperBound, hasLowerBound")
    Stream<DynamicNode> typeArgumentPatterns() {
        final BoundedTypeArgument upperBound = BoundedTypeArgument.of(
            WildcardBound.UPPER, ClassTypeSignature.of("java/lang/Number"));
        final BoundedTypeArgument lowerBound = BoundedTypeArgument.of(
            WildcardBound.LOWER, ClassTypeSignature.of("java/lang/Number"));

        return Stream.of(
            DynamicContainer.dynamicContainer("isTypeArgument", Stream.of(
                matches("isTypeArgument() - wildcard",  TypeArgumentPatterns.isTypeArgument(), WildcardArgument.INSTANCE),
                matches("isTypeArgument() - bounded",   TypeArgumentPatterns.isTypeArgument(), upperBound),
                notMatches("isTypeArgument() - primitive", TypeArgumentPatterns.isTypeArgument(), PrimitiveType.INT)
            )),
            DynamicContainer.dynamicContainer("isWildcard / isBoundedArgument", Stream.of(
                matches("isWildcard()",            TypeArgumentPatterns.isWildcard(), WildcardArgument.INSTANCE),
                notMatches("isWildcard()",         TypeArgumentPatterns.isWildcard(), upperBound),
                matches("isBoundedArgument()",     TypeArgumentPatterns.isBoundedArgument(), upperBound),
                notMatches("isBoundedArgument()",  TypeArgumentPatterns.isBoundedArgument(), WildcardArgument.INSTANCE)
            )),
            DynamicContainer.dynamicContainer("hasUpperBound / hasLowerBound", Stream.of(
                matches("hasUpperBound()",         TypeArgumentPatterns.hasUpperBound(), upperBound),
                notMatches("hasUpperBound()",      TypeArgumentPatterns.hasUpperBound(), lowerBound),
                matches("hasLowerBound()",         TypeArgumentPatterns.hasLowerBound(), lowerBound),
                notMatches("hasLowerBound()",      TypeArgumentPatterns.hasLowerBound(), upperBound),
                matches("hasBounds(pattern)",      TypeArgumentPatterns.hasBounds(TypePatterns.isClassNamed("java/lang/Number")), upperBound)
            ))
        );
    }

    // -------------------------------------------------------------------------
    // TypeParameterPatterns
    // -------------------------------------------------------------------------

    @TestFactory
    @DisplayName("TypeParameterPatterns - isTypeParameter, hasName, hasClassBound, hasInterfaceBounds")
    Stream<DynamicNode> typeParameterPatterns() {
        final TypeParameter tParam = TypeParameter.of("T",
            ClassTypeSignature.of("java/lang/Comparable"),
            List.of(ClassTypeSignature.of("java/io/Serializable")));
        final TypeParameter simple = TypeParameter.of("E");
        final TypeParameter interfaceOnly = TypeParameter.of("I", null, List.of(ClassTypeSignature.of("java/io/Serializable")));

        return Stream.of(
            DynamicContainer.dynamicContainer("isTypeParameter", Stream.of(
                matches("isTypeParameter()",        TypeParameterPatterns.isTypeParameter(), tParam),
                notMatches("isTypeParameter()",     TypeParameterPatterns.isTypeParameter(), PrimitiveType.INT)
            )),
            DynamicContainer.dynamicContainer("hasName", Stream.of(
                matches("hasName(\"T\")",           TypeParameterPatterns.hasName("T"), tParam),
                notMatches("hasName(\"E\")",        TypeParameterPatterns.hasName("E"), tParam),
                matches("hasName(predicate)",       TypeParameterPatterns.hasName(n -> n.startsWith("T")), tParam)
            )),
            DynamicContainer.dynamicContainer("hasClassBound / hasInterfaceBounds", Stream.of(
                matches("hasClassBound()",          TypeParameterPatterns.hasClassBound(), tParam),
                notMatches("hasClassBound()",       TypeParameterPatterns.hasClassBound(), interfaceOnly),
                matches("hasClassBound(pattern)",   TypeParameterPatterns.hasClassBound(
                    TypePatterns.isClassNamed("java/lang/Comparable")), tParam),
                matches("hasInterfaceBounds()",     TypeParameterPatterns.hasInterfaceBounds(), tParam),
                notMatches("hasInterfaceBounds()",  TypeParameterPatterns.hasInterfaceBounds(), simple),
                matches("hasNoInterfaceBounds()",   TypeParameterPatterns.hasNoInterfaceBounds(), simple),
                notMatches("hasNoInterfaceBounds()", TypeParameterPatterns.hasNoInterfaceBounds(), tParam),
                matches("hasInterfaceBounds(pattern)", TypeParameterPatterns.hasInterfaceBounds(
                    TypePatterns.isClassNamed("java/io/Serializable")), tParam)
            ))
        );
    }

    // -------------------------------------------------------------------------
    // TypeVariablePatterns
    // -------------------------------------------------------------------------

    @TestFactory
    @DisplayName("TypeVariablePatterns - isTypeVariable, isBound, isUnbound")
    Stream<DynamicNode> typeVariablePatterns() {
        final TypeParameter tParam = TypeParameter.of("T");
        final TypeVariable bound = TypeVariable.of(tParam);
        final TypeVariable.Unbound unbound = TypeVariable.unbound("T");

        return Stream.of(
            DynamicContainer.dynamicContainer("isTypeVariable", Stream.of(
                matches("isTypeVariable() - bound",    TypeVariablePatterns.isTypeVariable(), bound),
                notMatches("isTypeVariable() - int",   TypeVariablePatterns.isTypeVariable(), PrimitiveType.INT)
            )),
            DynamicContainer.dynamicContainer("isBound / isUnbound", Stream.of(
                matches("isBound()",                   TypeVariablePatterns.isBound(), bound),
                notMatches("isBound() - unbound",      TypeVariablePatterns.isBound(), unbound),
                matches("isUnbound()",                 TypeVariablePatterns.isUnbound(), unbound),
                notMatches("isUnbound() - bound",      TypeVariablePatterns.isUnbound(), bound),
                matches("isBound(predicate)",          TypeVariablePatterns.isBound(p -> p.getName().equals("T")), bound),
                matches("isBound(TypePattern)",        TypeVariablePatterns.isBound(TypeParameterPatterns.hasName("T")), bound),
                matches("isUnbound(predicate)",        TypeVariablePatterns.isUnbound(u -> u.getName().equals("T")), unbound)
            ))
        );
    }
}
