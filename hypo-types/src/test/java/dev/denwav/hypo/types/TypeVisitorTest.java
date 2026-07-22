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
import dev.denwav.hypo.types.desc.Descriptor;
import dev.denwav.hypo.types.desc.MethodDescriptor;
import dev.denwav.hypo.types.desc.TypeDescriptor;
import dev.denwav.hypo.types.kind.ArrayType;
import dev.denwav.hypo.types.kind.ClassType;
import dev.denwav.hypo.types.kind.MethodType;
import dev.denwav.hypo.types.kind.ValueType;
import dev.denwav.hypo.types.parsing.JvmTypeParseFailureException;
import dev.denwav.hypo.types.sig.ArrayTypeSignature;
import dev.denwav.hypo.types.sig.ClassSignature;
import dev.denwav.hypo.types.sig.ClassTypeSignature;
import dev.denwav.hypo.types.sig.MethodSignature;
import dev.denwav.hypo.types.sig.ReferenceTypeSignature;
import dev.denwav.hypo.types.sig.Signature;
import dev.denwav.hypo.types.sig.ThrowsSignature;
import dev.denwav.hypo.types.sig.TypeParameterHolder;
import dev.denwav.hypo.types.sig.TypeSignature;
import dev.denwav.hypo.types.sig.param.BoundedTypeArgument;
import dev.denwav.hypo.types.sig.param.TypeArgument;
import dev.denwav.hypo.types.sig.param.TypeParameter;
import dev.denwav.hypo.types.sig.param.TypeVariable;
import dev.denwav.hypo.types.sig.param.WildcardArgument;
import dev.denwav.hypo.types.sig.param.WildcardBound;
import dev.denwav.hypo.types.visitor.TraversingTypeVisitor;
import dev.denwav.hypo.types.visitor.TypeVisitor;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("[types] TypeVisitor Tests")
class TypeVisitorTest {

    // -------------------------------------------------------------------------
    // RecordingVisitor - records the simple name of every visit() parameter type
    // -------------------------------------------------------------------------

    private static final class RecordingVisitor implements TypeVisitor {
        final List<String> calls = new ArrayList<>();

        @Override public boolean visit(final @NotNull PrimitiveType t)          { this.calls.add("PrimitiveType");          return TypeVisitor.super.visit(t); }
        @Override public boolean visit(final @NotNull VoidType t)               { this.calls.add("VoidType");               return TypeVisitor.super.visit(t); }
        @Override public boolean visit(final @NotNull ValueType t)              { this.calls.add("ValueType");              return TypeVisitor.super.visit(t); }
        @Override public boolean visit(final @NotNull ClassType t)              { this.calls.add("ClassType");              return TypeVisitor.super.visit(t); }
        @Override public boolean visit(final @NotNull ArrayType t)              { this.calls.add("ArrayType");              return TypeVisitor.super.visit(t); }
        @Override public boolean visit(final @NotNull MethodType t)             { this.calls.add("MethodType");             return TypeVisitor.super.visit(t); }
        @Override public boolean visit(final @NotNull Descriptor t)             { this.calls.add("Descriptor");             return TypeVisitor.super.visit(t); }
        @Override public boolean visit(final @NotNull TypeDescriptor t)         { this.calls.add("TypeDescriptor");         return TypeVisitor.super.visit(t); }
        @Override public boolean visit(final @NotNull ClassTypeDescriptor t)    { this.calls.add("ClassTypeDescriptor");    return TypeVisitor.super.visit(t); }
        @Override public boolean visit(final @NotNull ArrayTypeDescriptor t)    { this.calls.add("ArrayTypeDescriptor");    return TypeVisitor.super.visit(t); }
        @Override public boolean visit(final @NotNull MethodDescriptor t)       { this.calls.add("MethodDescriptor");       return TypeVisitor.super.visit(t); }
        @Override public boolean visit(final @NotNull Signature t)              { this.calls.add("Signature");              return TypeVisitor.super.visit(t); }
        @Override public boolean visit(final @NotNull TypeSignature t)          { this.calls.add("TypeSignature");          return TypeVisitor.super.visit(t); }
        @Override public boolean visit(final @NotNull ReferenceTypeSignature t) { this.calls.add("ReferenceTypeSignature"); return TypeVisitor.super.visit(t); }
        @Override public boolean visit(final @NotNull ClassTypeSignature t)     { this.calls.add("ClassTypeSignature");     return TypeVisitor.super.visit(t); }
        @Override public boolean visit(final @NotNull ArrayTypeSignature t)     { this.calls.add("ArrayTypeSignature");     return TypeVisitor.super.visit(t); }
        @Override public boolean visit(final @NotNull TypeVariable t)           { this.calls.add("TypeVariable");           return TypeVisitor.super.visit(t); }
        @Override public boolean visit(final @NotNull TypeVariable.Unbound t)   { this.calls.add("TypeVariable.Unbound");   return TypeVisitor.super.visit(t); }
        @Override public boolean visit(final @NotNull MethodSignature t)        { this.calls.add("MethodSignature");        return TypeVisitor.super.visit(t); }
        @Override public boolean visit(final @NotNull ClassSignature t)         { this.calls.add("ClassSignature");         return TypeVisitor.super.visit(t); }
        @Override public boolean visit(final @NotNull ThrowsSignature t)        { this.calls.add("ThrowsSignature");        return TypeVisitor.super.visit(t); }
        @Override public boolean visit(final @NotNull TypeArgument t)           { this.calls.add("TypeArgument");           return TypeVisitor.super.visit(t); }
        @Override public boolean visit(final @NotNull TypeParameter t)          { this.calls.add("TypeParameter");          return TypeVisitor.super.visit(t); }
        @Override public boolean visit(final @NotNull BoundedTypeArgument t)    { this.calls.add("BoundedTypeArgument");    return TypeVisitor.super.visit(t); }
        @Override public boolean visit(final @NotNull WildcardArgument t)       { this.calls.add("WildcardArgument");       return TypeVisitor.super.visit(t); }
        @Override public boolean visit(final @NotNull WildcardBound t)          { this.calls.add("WildcardBound");          return TypeVisitor.super.visit(t); }
        @Override public boolean visit(final @NotNull TypeBindable t)           { this.calls.add("TypeBindable");           return TypeVisitor.super.visit(t); }
        @Override public boolean visit(final @NotNull TypeParameterHolder t)    { this.calls.add("TypeParameterHolder");    return TypeVisitor.super.visit(t); }
    }

    // -------------------------------------------------------------------------
    // Dispatch correctness
    // -------------------------------------------------------------------------

    record DispatchCase(String label, TypeRepresentable input, List<String> expectedCalls) {}

    private static Stream<DispatchCase> dispatchCases() throws JvmTypeParseFailureException {
        final TypeParameter tParam = TypeParameter.of("T");
        return Stream.of(
            new DispatchCase("PrimitiveType",
                PrimitiveType.INT,
                List.of("Descriptor", "TypeDescriptor", "Signature", "TypeSignature", "TypeBindable", "ValueType", "PrimitiveType")),
            new DispatchCase("VoidType",
                VoidType.INSTANCE,
                List.of("Descriptor", "TypeDescriptor", "Signature", "TypeSignature", "TypeBindable", "ValueType", "VoidType")),
            new DispatchCase("ClassTypeDescriptor",
                ClassTypeDescriptor.of("java/lang/Object"),
                List.of("Descriptor", "ValueType", "TypeDescriptor", "ClassType", "ClassTypeDescriptor")),
            new DispatchCase("ArrayTypeDescriptor",
                ArrayTypeDescriptor.of(1, PrimitiveType.INT),
                List.of("Descriptor", "ValueType", "TypeDescriptor", "ArrayType", "ArrayTypeDescriptor")),
            new DispatchCase("MethodDescriptor",
                MethodDescriptor.parse("(I)V"),
                List.of("Descriptor", "MethodType", "MethodDescriptor")),
            new DispatchCase("ClassTypeSignature",
                ClassTypeSignature.of("java/lang/Object"),
                List.of("Signature", "TypeBindable", "ValueType", "TypeSignature", "TypeArgument", "ReferenceTypeSignature", "ClassType", "ThrowsSignature", "ClassTypeSignature")),
            new DispatchCase("ArrayTypeSignature",
                ArrayTypeSignature.of(1, PrimitiveType.INT),
                List.of("Signature", "TypeBindable", "ValueType", "TypeSignature", "TypeArgument", "ReferenceTypeSignature", "ArrayType", "ArrayTypeSignature")),
            new DispatchCase("TypeVariable (bound)",
                TypeVariable.of(tParam),
                List.of("Signature", "TypeBindable", "ValueType", "TypeSignature", "TypeArgument", "ReferenceTypeSignature", "ThrowsSignature", "TypeVariable")),
            new DispatchCase("TypeVariable.Unbound",
                TypeVariable.unbound("T"),
                List.of("Signature", "TypeBindable", "ValueType", "TypeSignature", "TypeArgument", "ReferenceTypeSignature", "ThrowsSignature", "TypeVariable.Unbound")),
            new DispatchCase("MethodSignature",
                MethodSignature.parse("()V"),
                List.of("Signature", "TypeBindable", "MethodType", "TypeParameterHolder", "MethodSignature")),
            new DispatchCase("ClassSignature",
                ClassSignature.parse("Ljava/lang/Object;"),
                List.of("Signature", "TypeBindable", "TypeParameterHolder", "ClassSignature")),
            new DispatchCase("TypeParameter",
                TypeParameter.of("T"),
                List.of("TypeBindable", "TypeParameter")),
            new DispatchCase("BoundedTypeArgument (extends)",
                BoundedTypeArgument.of(WildcardBound.UPPER, ClassTypeSignature.of("java/lang/Number")),
                List.of("TypeArgument", "TypeBindable", "BoundedTypeArgument")),
            new DispatchCase("WildcardArgument",
                WildcardArgument.INSTANCE,
                List.of("TypeArgument", "TypeBindable", "WildcardArgument")),
            new DispatchCase("WildcardBound",
                WildcardBound.UPPER,
                List.of("WildcardBound"))
        );
    }

    @TestFactory
    @DisplayName("accept() dispatches to the correct sequence of visit() overloads for each concrete type")
    Stream<DynamicNode> dispatchCorrectness() throws JvmTypeParseFailureException {
        return dispatchCases().map(c -> DynamicTest.dynamicTest(c.label(), () -> {
            final RecordingVisitor visitor = new RecordingVisitor();
            c.input().accept(visitor);
            assertEquals(c.expectedCalls(), visitor.calls,
                "Dispatch sequence for " + c.label() + " did not match expected visitor call order");
        }));
    }

    // -------------------------------------------------------------------------
    // Cancellation - returning false stops further dispatch
    // -------------------------------------------------------------------------

    @TestFactory
    @DisplayName("Returning false from a visit() method stops all subsequent visit() calls")
    Stream<DynamicNode> cancellation() {
        return Stream.of(
            DynamicTest.dynamicTest("Returning false from visit(TypeBindable) while accepting ClassTypeSignature", () -> {
                final List<String> calls = new ArrayList<>();
                @SuppressWarnings("MissingSuperCall")
                final TypeVisitor cancelOnTypeBindable = new TypeVisitor() {
                    @Override public boolean visit(final @NotNull TypeBindable t) {
                        calls.add("TypeBindable");
                        return false;
                    }
                    @Override public boolean visit(final @NotNull ClassTypeSignature t) {
                        calls.add("ClassTypeSignature");
                        return false;
                    }
                };
                ClassTypeSignature.of("java/lang/Object").accept(cancelOnTypeBindable);
                assertFalse(calls.contains("ClassTypeSignature"),
                    "ClassTypeSignature visit should not have been reached after TypeBindable returned false");
            }),
            DynamicTest.dynamicTest("Returning false from visit(Descriptor) while accepting ClassTypeDescriptor", () -> {
                final List<String> calls = new ArrayList<>();
                @SuppressWarnings("MissingSuperCall")
                final TypeVisitor cancelOnDescriptor = new TypeVisitor() {
                    @Override public boolean visit(final @NotNull Descriptor t) {
                        calls.add("Descriptor");
                        return false;
                    }
                    @Override public boolean visit(final @NotNull ClassTypeDescriptor t) {
                        calls.add("ClassTypeDescriptor");
                        return false;
                    }
                };
                ClassTypeDescriptor.of("java/lang/Object").accept(cancelOnDescriptor);
                assertFalse(calls.contains("ClassTypeDescriptor"),
                    "ClassTypeDescriptor visit should not have been reached after Descriptor returned false");
            }),
            DynamicTest.dynamicTest("Returning false from visit(TypeArgument) while accepting BoundedTypeArgument", () -> {
                final List<String> calls = new ArrayList<>();
                @SuppressWarnings("MissingSuperCall")
                final TypeVisitor cancelOnTypeArgument = new TypeVisitor() {
                    @Override public boolean visit(final @NotNull TypeArgument t) {
                        calls.add("TypeArgument");
                        return false;
                    }
                    @Override public boolean visit(final @NotNull BoundedTypeArgument t) {
                        calls.add("BoundedTypeArgument");
                        return false;
                    }
                };
                BoundedTypeArgument.of(WildcardBound.UPPER, ClassTypeSignature.of("java/lang/Number"))
                    .accept(cancelOnTypeArgument);
                assertFalse(calls.contains("BoundedTypeArgument"),
                    "BoundedTypeArgument visit should not have been reached after TypeArgument returned false");
            })
        );
    }

    // -------------------------------------------------------------------------
    // TraversingTypeVisitor - deep traversal
    // -------------------------------------------------------------------------

    @TestFactory
    @DisplayName("TraversingTypeVisitor traverses all nested components of complex type signatures")
    Stream<DynamicNode> traversingDeepTraversal() throws JvmTypeParseFailureException {
        return Stream.of(
            DynamicContainer.dynamicContainer("MethodSignature traversal", Stream.of(
                DynamicTest.dynamicTest("All nested types in a complex MethodSignature are visited", () -> {
                    final MethodSignature complex = MethodSignature.parse(
                        "<T:Ljava/lang/Comparable<TT;>;>(TT;Ljava/util/List<TT;>;)Ljava/lang/String;^Ljava/lang/RuntimeException;"
                    );
                    assertNotNull(complex, "Failed to parse test MethodSignature");

                    final List<TypeRepresentable> visited = new ArrayList<>();
                    final TraversingTypeVisitor traverser = new TraversingTypeVisitor() {
                        @Override
                        public boolean accept(final @NotNull TypeRepresentable t) {
                            visited.add(t);
                            return TraversingTypeVisitor.super.accept(t);
                        }
                    };
                    complex.accept(traverser);

                    final boolean hasComparable = visited.stream().anyMatch(
                        t -> t instanceof final ClassTypeSignature c && c.getName().equals("java/lang/Comparable"));
                    final boolean hasString = visited.stream().anyMatch(
                        t -> t instanceof final ClassTypeSignature c && c.getName().equals("java/lang/String"));
                    final boolean hasList = visited.stream().anyMatch(
                        t -> t instanceof final ClassTypeSignature c && c.getName().equals("java/util/List"));
                    final boolean hasException = visited.stream().anyMatch(
                        t -> t instanceof final ClassTypeSignature c && c.getName().equals("java/lang/RuntimeException"));

                    assertFalse(visited.isEmpty(), "TraversingTypeVisitor should have visited at least one type");
                    assertTrue(hasComparable,
                        "Traversal should have visited the TypeParameter class bound Comparable");
                    assertTrue(hasString,
                        "Traversal should have visited the return type String");
                    assertTrue(hasList,
                        "Traversal should have visited the parameter type List");
                    assertTrue(hasException,
                        "Traversal should have visited the throws clause RuntimeException");
                })
            )),
            DynamicContainer.dynamicContainer("TypeVariable anti-loop contract", Stream.of(
                DynamicTest.dynamicTest("TraversingTypeVisitor does not call getDefinition() on a resolved TypeVariable", () -> {
                    final AtomicBoolean getDefinitionCalled = new AtomicBoolean(false);
                    final TypeVariable lazy = TypeVariable.ofLazy("T", () -> {
                        getDefinitionCalled.set(true);
                        return TypeParameter.of("T");
                    });

                    final TraversingTypeVisitor traverser = new TraversingTypeVisitor() {
                        // Inherits default behavior
                    };
                    lazy.accept(traverser);

                    assertFalse(getDefinitionCalled.get(),
                        "TraversingTypeVisitor must not call getDefinition() on a TypeVariable - " +
                        "this would risk infinite recursion if the definition contains the same TypeVariable");
                })
            ))
        );
    }
}
