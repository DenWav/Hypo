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
import dev.denwav.hypo.types.sig.MethodSignature;
import dev.denwav.hypo.types.sig.TypeSignature;
import dev.denwav.hypo.types.sig.param.TypeParameter;
import dev.denwav.hypo.types.sig.param.TypeVariable;
import dev.denwav.hypo.types.sig.param.WildcardArgument;
import dev.denwav.hypo.types.visitor.TraversingTypeVisitor;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("[types] TypeBindable Tests")
class TypeBindableTest {

    private final TypeVariableBinder dummyBinder = name -> null;

    @Test
    @DisplayName("Verify that primitive and void types are always bound")
    void testPrimitiveAndVoidTypes() {
        final PrimitiveType intType = PrimitiveType.INT;
        assertFalse(intType.isUnbound());
        assertSame(intType, intType.bind(this.dummyBinder));

        final VoidType voidType = VoidType.INSTANCE;
        assertFalse(voidType.isUnbound());
        assertSame(voidType, voidType.bind(this.dummyBinder));
    }

    @Test
    @DisplayName("Verify that wildcard arguments are always bound")
    void testWildcardArgument() {
        final WildcardArgument wildcard = WildcardArgument.INSTANCE;
        assertFalse(wildcard.isUnbound());
        assertSame(wildcard, wildcard.bind(this.dummyBinder));
    }

    @Test
    @DisplayName("Verify that bound type variables are always bound")
    void testBoundTypeVariable() {
        final TypeParameter param = TypeParameter.of("T");
        final TypeVariable boundVar = TypeVariable.of(param);
        assertFalse(boundVar.isUnbound());
        assertSame(boundVar, boundVar.bind(this.dummyBinder));
    }

    @Test
    @DisplayName("Verify that unbound type variables throw on missing binding and resolve on correct binding")
    void testUnboundTypeVariable() {
        final TypeVariable.Unbound unboundVar = TypeVariable.unbound("T");
        assertTrue(unboundVar.isUnbound());

        assertThrows(IllegalStateException.class, () -> {
            unboundVar.bind(this.dummyBinder).getDefinition();
        });

        final TypeParameter param = TypeParameter.of("T");
        final TypeVariableBinder mappingBinder = name -> {
            if (name.equals("T")) {
                return param;
            }
            return null;
        };

        final TypeVariable resolved = unboundVar.bind(mappingBinder);
        assertFalse(resolved.isUnbound());
        assertEquals("T", resolved.getDefinition().getName());
    }

    @Test
    @DisplayName("Test default null handling and delegation methods of TypeVariableBinder")
    void testTypeVariableBinderDefaultMethods() {
        final TypeParameter param = TypeParameter.of("T");
        final TypeVariableBinder mappingBinder = name -> {
            if (name.equals("T")) {
                return param;
            }
            return null;
        };

        assertNull(mappingBinder.bind(null));

        final TypeVariable.Unbound unboundVar = TypeVariable.unbound("T");
        final TypeVariable boundVar = (TypeVariable) mappingBinder.bind((TypeBindable) unboundVar);
        assertNotNull(boundVar);
        assertFalse(boundVar.isUnbound());
        assertEquals("T", boundVar.getDefinition().getName());
    }

    @Test
    @DisplayName("Verify basic fictional binding provided by TypeVariableBinder.object()")
    void testTypeVariableBinderObjectFactory() {
        final TypeVariableBinder objectBinder = TypeVariableBinder.object();
        assertNotNull(objectBinder);

        final TypeParameter paramX = objectBinder.bindingFor("X");
        assertNotNull(paramX);
        assertEquals("X", paramX.getName());

        final TypeVariable.Unbound unboundVar = TypeVariable.unbound("X");
        final TypeVariable boundVar = (TypeVariable) objectBinder.bind((TypeBindable) unboundVar);
        assertNotNull(boundVar);
        assertFalse(boundVar.isUnbound());
        assertEquals("X", boundVar.getDefinition().getName());
    }

    @Test
    @DisplayName("Verify that binding propagates recursively to all nested type variables in type signatures")
    void testSignatureBindingPropagation() {
        final TypeSignature unboundSig = TypeSignature.parse("Ljava/util/Map<TT;TU;>;");
        assertTrue(unboundSig.isUnbound());

        final TypeParameter paramT = TypeParameter.of("T");
        final TypeParameter paramU = TypeParameter.of("U");
        final TypeVariableBinder mappingBinder = name -> {
            if (name.equals("T")) {
                return paramT;
            }
            if (name.equals("U")) {
                return paramU;
            }
            return null;
        };

        final TypeSignature boundSig = unboundSig.bind(mappingBinder);
        assertFalse(boundSig.isUnbound());
        boundSig.accept(new TraversingTypeVisitor() {
            @Override
            public boolean visit(final @NotNull TypeVariable var) {
                var.getDefinition();
                return TraversingTypeVisitor.super.visit(var);
            }
        });
        final ClassTypeSignature internedBoundSig = ((ClassTypeSignature) boundSig).intern();
        assertSame(internedBoundSig, internedBoundSig.bind(mappingBinder));
    }

    @Test
    @DisplayName("Test type parameter resolution in HierarchyTypeVariableBinder with a single level")
    void testHierarchySingleLevel() {
        final ClassSignature classSig = ClassSignature.parse("<T:Ljava/lang/Object;>Ljava/lang/Object;");
        final HierarchyTypeVariableBinder binder = HierarchyTypeVariableBinder.of(classSig);

        final TypeParameter resolved = binder.bindingFor("T");
        assertNotNull(resolved);
        assertEquals("T", resolved.getName());

        assertNull(binder.bindingFor("U"));
    }

    @Test
    @DisplayName("Test scoping and shadowing of type parameters in HierarchyTypeVariableBinder")
    void testHierarchyScopingAndShadowing() {
        final ClassSignature classSig = ClassSignature.parse("<T:Ljava/lang/String;>Ljava/lang/Object;");
        final MethodSignature methodSig = MethodSignature.parse("<T:Ljava/lang/Integer;>()VT;");

        final HierarchyTypeVariableBinder binder = HierarchyTypeVariableBinder.of(methodSig, classSig);

        final TypeParameter resolved = binder.bindingFor("T");
        assertNotNull(resolved);
        assertEquals("T", resolved.getName());
        
        // Assert shadowing: the method-level T shadows class-level T.
        assertNotNull(resolved.getClassBound());
        assertEquals("java.lang.Integer", resolved.getClassBound().asReadable());

        // Under class signature only, T should resolve to String
        final HierarchyTypeVariableBinder classOnlyBinder = HierarchyTypeVariableBinder.of(classSig);
        final TypeParameter resolvedClass = classOnlyBinder.bindingFor("T");
        assertNotNull(resolvedClass);
        assertNotNull(resolvedClass.getClassBound());
        assertEquals("java.lang.String", resolvedClass.getClassBound().asReadable());
    }

    @Test
    @DisplayName("Test resolution of recursive type parameter bounds in HierarchyTypeVariableBinder")
    void testHierarchyRecursiveTypeParameter() {
        final ClassSignature classSig = ClassSignature.parse("<T:Ljava/lang/Comparable<TT;>;>Ljava/lang/Object;");
        assertTrue(classSig.isUnbound());

        final HierarchyTypeVariableBinder binder = HierarchyTypeVariableBinder.of(classSig);
        final ClassSignature boundSig = classSig.bind(binder);

        assertFalse(boundSig.isUnbound());
        //noinspection DataFlowIssue
        assertSame(
            boundSig.getTypeParameters().getFirst(),
            (
                (TypeVariable) (
                    (ClassTypeSignature) boundSig.getTypeParameters().getFirst().getClassBound()
                ).getTypeArguments().getFirst()
            ).getDefinition()
        );
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    @DisplayName("Verify that binding propagates recursively to all nested type variables in MethodSignature.bind")
    void testMethodSignatureBind() {
        final MethodSignature unboundSig = MethodSignature.parse("<U:Ljava/lang/Comparable<TU;>;V:TU;>(TT;TU;)TV;^TX;");
        assertTrue(unboundSig.isUnbound());

        final TypeParameter paramT = TypeParameter.of("T");
        final TypeParameter paramX = TypeParameter.of("X");
        final TypeVariableBinder mappingBinder = name -> {
            if (name.equals("T")) {
                return paramT;
            }
            if (name.equals("X")) {
                return paramX;
            }
            return null;
        };

        final MethodSignature boundSig = unboundSig.bind(mappingBinder);
        assertFalse(boundSig.isUnbound());

        // Verify U (first type parameter) binds to itself in its classBound: Ljava/lang/Comparable<TU;>;
        final TypeParameter boundU = boundSig.getTypeParameters().getFirst();
        assertEquals("U", boundU.getName());
        assertSame(
            boundU,
            ((TypeVariable) ((ClassTypeSignature) boundU.getClassBound()).getTypeArguments().getFirst()).getDefinition()
        );

        // Verify V (second type parameter) binds to U in its classBound: TU;
        final TypeParameter boundV = boundSig.getTypeParameters().get(1);
        assertEquals("V", boundV.getName());
        assertSame(
            boundU,
            ((TypeVariable) boundV.getClassBound()).getDefinition()
        );

        // Verify first parameter (TT;) binds to T
        assertSame(
            paramT,
            ((TypeVariable) boundSig.getParameters().get(0)).getDefinition()
        );

        // Verify second parameter (TU;) binds to U
        assertSame(
            boundU,
            ((TypeVariable) boundSig.getParameters().get(1)).getDefinition()
        );

        // Verify return type (TV;) binds to V
        assertSame(
            boundV,
            ((TypeVariable) boundSig.getReturnType()).getDefinition()
        );

        // Verify throws (TX;) binds to X
        assertSame(
            paramX,
            ((TypeVariable) boundSig.getThrowsSignatures().getFirst()).getDefinition()
        );
    }

    @Test
    @DisplayName("Verify binding of a MethodSignature where its type variables reference a parent ClassSignature")
    void testMethodInClassHierarchyBinding() {
        // Class Signature: <T:Ljava/lang/Object;>Ljava/lang/Object;
        final ClassSignature classSig = ClassSignature.parse("<T:Ljava/lang/Object;>Ljava/lang/Object;");
        final ClassSignature boundClassSig = classSig.bind(HierarchyTypeVariableBinder.of(classSig));
        final TypeParameter boundT = boundClassSig.getTypeParameters().getFirst();

        // Method Signature: <U:Ljava/lang/Object;>(TT;)TU;
        final MethodSignature methodSig = MethodSignature.parse("<U:Ljava/lang/Object;>(TT;)TU;");

        // The method signature uses TT; which is defined in classSig.
        // We create a binder that knows about both, starting with methodSig as innermost, boundClassSig as outermost.
        final HierarchyTypeVariableBinder binder = HierarchyTypeVariableBinder.of(methodSig, boundClassSig);

        // Bind the method signature
        final MethodSignature boundMethodSig = methodSig.bind(binder);

        assertFalse(boundMethodSig.isUnbound());

        // The first parameter of boundMethodSig is (TT;) which should resolve to T from classSig.
        assertSame(
            boundT,
            ((TypeVariable) boundMethodSig.getParameters().getFirst()).getDefinition()
        );

        // The return type is TU; which should resolve to U from boundMethodSig.
        final TypeParameter boundU = boundMethodSig.getTypeParameters().getFirst();
        assertSame(
            boundU,
            ((TypeVariable) boundMethodSig.getReturnType()).getDefinition()
        );
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    @DisplayName("Test deeply nested hierarchy of Class and Method signatures with shadowing")
    void testNestedHierarchyScopingAndShadowing() {
        // ClassSignature (Level 1, outermost): <T:Ljava/lang/Object;>Ljava/lang/Object;
        final ClassSignature outerClassSig = ClassSignature.parse("<T:Ljava/lang/Object;>Ljava/lang/Object;");
        final ClassSignature boundOuter = outerClassSig.bind(HierarchyTypeVariableBinder.of(outerClassSig));
        final TypeParameter paramT1 = boundOuter.getTypeParameters().getFirst();

        // ClassSignature (Level 2): <U:TT;>Ljava/lang/Object;
        final ClassSignature innerClassSig = ClassSignature.parse("<U:TT;>Ljava/lang/Object;");
        final ClassSignature boundInner = innerClassSig.bind(HierarchyTypeVariableBinder.of(innerClassSig, boundOuter));
        final TypeParameter paramU2 = boundInner.getTypeParameters().getFirst();

        // Verify U's bound is resolved to T from Level 1
        assertSame(
            paramT1,
            ((TypeVariable) paramU2.getClassBound()).getDefinition()
        );

        // MethodSignature (Level 3): <T:Ljava/lang/Number;V:TU;>()TT;
        // Here, T is shadowed (Level 3 T shadows Level 1 T), and V references U from Level 2.
        final MethodSignature methodSig = MethodSignature.parse("<T:Ljava/lang/Number;V:TU;>()TT;");
        final MethodSignature boundMethod = methodSig.bind(HierarchyTypeVariableBinder.of(methodSig, boundInner, boundOuter));
        final TypeParameter paramT3 = boundMethod.getTypeParameters().get(0);
        final TypeParameter paramV3 = boundMethod.getTypeParameters().get(1);

        // Verify V's class bound is resolved to U from Level 2
        assertSame(
            paramU2,
            ((TypeVariable) paramV3.getClassBound()).getDefinition()
        );

        // Verify return type TT; resolves to Level 3 T (Number), not Level 1 T (Object) -> Shadowing Check
        assertSame(
            paramT3,
            ((TypeVariable) boundMethod.getReturnType()).getDefinition()
        );

        // ClassSignature (Level 4, innermost): <W:TV;>Ljava/util/List<TT;>;
        // Here, W references V from Level 3.
        // And TT; references the active T (Level 3 T, which shadows Level 1 T).
        final ClassSignature innermostClassSig = ClassSignature.parse("<W:TV;>Ljava/util/List<TT;>;");
        final ClassSignature boundInnermost = innermostClassSig.bind(
            HierarchyTypeVariableBinder.of(innermostClassSig, boundMethod, boundInner, boundOuter)
        );
        final TypeParameter paramW4 = boundInnermost.getTypeParameters().getFirst();

        // Verify W's class bound resolves to V from Level 3
        assertSame(
            paramV3,
            ((TypeVariable) paramW4.getClassBound()).getDefinition()
        );

        // Verify the superclass type argument List<T> resolves to Level 3 T (shadowing check)
        assertSame(
            paramT3,
            ((TypeVariable) boundInnermost.getSuperClass().getTypeArguments().getFirst()).getDefinition()
        );
    }

    @Test
    @DisplayName("Verify that empty or non-parameter hierarchies return null for binding queries")
    void testHierarchyEmptyOrTypeSignature() {
        final MethodSignature stringSig = MethodSignature.parse("()V)");
        final HierarchyTypeVariableBinder emptyBinder = HierarchyTypeVariableBinder.of();
        assertNull(emptyBinder.bindingFor("T"));

        final HierarchyTypeVariableBinder nonParamBinder = HierarchyTypeVariableBinder.of(stringSig);
        assertNull(nonParamBinder.bindingFor("T"));
    }

    @Test
    @DisplayName("Verify that Intern.intern() does not intern when isUnbound() is true")
    void testInternSkipsUnbound() {
        final ClassSignature unbound1 = ClassSignature.parse("<T:Ljava/lang/Object;>Ldev/denwav/hypo/UnboundTest<TT;>;");
        final ClassSignature unbound2 = ClassSignature.parse("<T:Ljava/lang/Object;>Ldev/denwav/hypo/UnboundTest<TT;>;");
        assertTrue(unbound1.isUnbound());
        assertTrue(unbound2.isUnbound());
        // Since both are unbound, calling intern() should return this immediately (not sharing instances from Intern cache)
        assertNotSame(unbound1.intern(), unbound2.intern());
    }
}

