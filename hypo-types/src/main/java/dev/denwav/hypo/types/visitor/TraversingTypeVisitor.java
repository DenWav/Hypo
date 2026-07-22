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

package dev.denwav.hypo.types.visitor;

import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;
import dev.denwav.hypo.types.TypeRepresentable;
import dev.denwav.hypo.types.desc.ArrayTypeDescriptor;
import dev.denwav.hypo.types.desc.MethodDescriptor;
import dev.denwav.hypo.types.desc.TypeDescriptor;
import dev.denwav.hypo.types.sig.ArrayTypeSignature;
import dev.denwav.hypo.types.sig.ClassSignature;
import dev.denwav.hypo.types.sig.ClassTypeSignature;
import dev.denwav.hypo.types.sig.MethodSignature;
import dev.denwav.hypo.types.sig.ReferenceTypeSignature;
import dev.denwav.hypo.types.sig.ThrowsSignature;
import dev.denwav.hypo.types.sig.TypeSignature;
import dev.denwav.hypo.types.sig.param.BoundedTypeArgument;
import dev.denwav.hypo.types.sig.param.TypeArgument;
import dev.denwav.hypo.types.sig.param.TypeParameter;
import dev.denwav.hypo.types.sig.param.TypeVariable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link TypeVisitor} that recursively traverses into encapsulated child types and components.
 *
 * <p>While {@link TypeVisitor} inspects only the type hierarchy graph of a single object, {@code TraversingTypeVisitor}
 * re-enters {@link #accept(TypeRepresentable)} for all encapsulated values, such as method parameters, return types,
 * array base types, and generic type arguments.
 *
 * <p>For example, visiting a {@link dev.denwav.hypo.types.sig.MethodSignature MethodSignature} with this visitor will
 * automatically visit the method's type parameters, parameter types, return type, and declared exceptions:
 *
 * <pre><code>
 *     final TraversingTypeVisitor visitor = new TraversingTypeVisitor() {
 *         &#64;Override
 *         public boolean visit(final &#64;NotNull ClassTypeSignature sig) {
 *             System.out.println("Found class reference: " + sig.getName());
 *             return TraversingTypeVisitor.super.visit(sig);
 *         }
 *     };
 *     visitor.accept(methodSignature);
 * </code></pre>
 *
 * <p>When overriding {@code visit} methods in this interface, call {@code super.visit} to ensure that child
 * elements continue to be traversed.
 */
public interface TraversingTypeVisitor extends TypeVisitor {

    /**
     * Visit an {@link ArrayTypeDescriptor} and recursively visit its {@link ArrayTypeDescriptor#getBaseType() base type}.
     *
     * @param desc The array type descriptor being visited.
     * @return {@code true} to continue traversal, or {@code false} to cancel immediately.
     */
    @Override
    @ApiStatus.OverrideOnly
    default boolean visit(final @NotNull ArrayTypeDescriptor desc) {
        return TypeVisitor.super.visit(desc)
            && this.accept(desc.getBaseType());
    }

    /**
     * Visit a {@link MethodDescriptor} and recursively visit all parameters and the return type.
     *
     * @param desc The method descriptor being visited.
     * @return {@code true} to continue traversal, or {@code false} to cancel immediately.
     */
    @Override
    @ApiStatus.OverrideOnly
    @OverridingMethodsMustInvokeSuper
    default boolean visit(final @NotNull MethodDescriptor desc) {
        if (!TypeVisitor.super.visit(desc)) {
            return false;
        }
        for (final TypeDescriptor parameter : desc.getParameters()) {
            if (!this.accept(parameter)) {
                return false;
            }
        }
        return this.accept(desc.getReturnType());
    }

    /**
     * Visit a {@link ClassTypeSignature} and recursively visit its owner class and type arguments.
     *
     * @param sig The class type signature being visited.
     * @return {@code true} to continue traversal, or {@code false} to cancel immediately.
     */
    @Override
    @ApiStatus.OverrideOnly
    @OverridingMethodsMustInvokeSuper
    default boolean visit(final @NotNull ClassTypeSignature sig) {
        if (!TypeVisitor.super.visit(sig)) {
            return false;
        }
        if (sig.getOwnerClass() != null) {
            if (!this.accept(sig.getOwnerClass())) {
                return false;
            }
        }
        for (final TypeArgument typeArgument : sig.getTypeArguments()) {
            if (!this.accept(typeArgument)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Visit an {@link ArrayTypeSignature} and recursively visit its {@link ArrayTypeSignature#getBaseType() base type}.
     *
     * @param sig The array type signature being visited.
     * @return {@code true} to continue traversal, or {@code false} to cancel immediately.
     */
    @Override
    @ApiStatus.OverrideOnly
    @OverridingMethodsMustInvokeSuper
    default boolean visit(final @NotNull ArrayTypeSignature sig) {
        return TypeVisitor.super.visit(sig)
            && this.accept(sig.getBaseType());
    }

    /**
     * Visit a {@link TypeVariable}.
     *
     * <p>This method intentionally does not traverse into {@link TypeVariable#getDefinition()}, because type variable
     * definitions can be infinitely recursive when bounds reference themselves (for example, {@code <T extends Comparable<T>>}).
     *
     * @param var The type variable being visited.
     * @return {@code true} to continue traversal, or {@code false} to cancel immediately.
     */
    @Override
    @ApiStatus.OverrideOnly
    @OverridingMethodsMustInvokeSuper
    default boolean visit(final @NotNull TypeVariable var) {
        // Intentionally does not traverse into TypeVariable.getDefinition(). TypeVariable definitions are potentially
        // infinitely recursive, as a TypeParameter's bounds may include itself (e.g. <T extends Comparable<T>>).
        return TypeVisitor.super.visit(var);
    }

    /**
     * Visit a {@link MethodSignature} and recursively visit its type parameters, parameter types, return type, and throws signatures.
     *
     * @param sig The method signature being visited.
     * @return {@code true} to continue traversal, or {@code false} to cancel immediately.
     */
    @Override
    @ApiStatus.OverrideOnly
    @OverridingMethodsMustInvokeSuper
    default boolean visit(final @NotNull MethodSignature sig) {
        if (!TypeVisitor.super.visit(sig)) {
            return false;
        }
        for (final TypeParameter typeParameter : sig.getTypeParameters()) {
            if (!this.accept(typeParameter)) {
                return false;
            }
        }
        for (final TypeSignature parameter : sig.getParameters()) {
            if (!this.accept(parameter)) {
                return false;
            }
        }
        if (!this.accept(sig.getReturnType())) {
            return false;
        }
        for (final ThrowsSignature throwsSignature : sig.getThrowsSignatures()) {
            if (!this.accept(throwsSignature)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Visit a {@link ClassSignature} and recursively visit its type parameters, super class, and super interfaces.
     *
     * @param sig The class signature being visited.
     * @return {@code true} to continue traversal, or {@code false} to cancel immediately.
     */
    @Override
    @ApiStatus.OverrideOnly
    @OverridingMethodsMustInvokeSuper
    default boolean visit(final @NotNull ClassSignature sig) {
        if (!TypeVisitor.super.visit(sig)) {
            return false;
        }
        for (final TypeParameter typeParameter : sig.getTypeParameters()) {
            if (!this.accept(typeParameter)) {
                return false;
            }
        }
        if (!this.accept(sig.getSuperClass())) {
            return false;
        }
        for (final ClassTypeSignature superInterface : sig.getSuperInterfaces()) {
            if (!this.accept(superInterface)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Visit a {@link TypeParameter} and recursively visit its class bound and interface bounds.
     *
     * @param param The type parameter being visited.
     * @return {@code true} to continue traversal, or {@code false} to cancel immediately.
     */
    @Override
    @ApiStatus.OverrideOnly
    @OverridingMethodsMustInvokeSuper
    default boolean visit(final @NotNull TypeParameter param) {
        if (!TypeVisitor.super.visit(param)) {
            return false;
        }
        if (param.getClassBound() != null) {
            if (!this.accept(param.getClassBound())) {
                return false;
            }
        }
        for (final ReferenceTypeSignature interfaceBound : param.getInterfaceBounds()) {
            if (!this.accept(interfaceBound)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Visit a {@link BoundedTypeArgument} and recursively visit its wildcard bound and signature.
     *
     * @param arg The bounded type argument being visited.
     * @return {@code true} to continue traversal, or {@code false} to cancel immediately.
     */
    @Override
    @ApiStatus.OverrideOnly
    @OverridingMethodsMustInvokeSuper
    default boolean visit(final @NotNull BoundedTypeArgument arg) {
        return TypeVisitor.super.visit(arg)
            && this.accept(arg.getBounds())
            && this.accept(arg.getSignature());
    }
}
