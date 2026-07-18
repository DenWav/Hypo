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
import dev.denwav.hypo.types.PrimitiveType;
import dev.denwav.hypo.types.TypeBindable;
import dev.denwav.hypo.types.TypeRepresentable;
import dev.denwav.hypo.types.VoidType;
import dev.denwav.hypo.types.desc.ArrayTypeDescriptor;
import dev.denwav.hypo.types.desc.ClassTypeDescriptor;
import dev.denwav.hypo.types.desc.Descriptor;
import dev.denwav.hypo.types.desc.MethodDescriptor;
import dev.denwav.hypo.types.desc.TypeDescriptor;
import dev.denwav.hypo.types.kind.ArrayType;
import dev.denwav.hypo.types.kind.ClassType;
import dev.denwav.hypo.types.kind.MethodType;
import dev.denwav.hypo.types.kind.ValueType;
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
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * A visitor for inspecting hierarchy and classification interfaces of any {@link TypeRepresentable} type.
 *
 * <p>Unlike a standard tree visitor, {@code TypeVisitor} dispatches along the type hierarchy graph. When a type is passed
 * to {@link #accept(TypeRepresentable)}, every supertype, marker interface, and grouping interface implemented by that
 * type is visited exactly once.
 *
 * <p>To inspect a type, pass it to {@link #accept(TypeRepresentable)} and override any relevant {@code visit} methods:
 *
 * <pre><code>
 *     final TypeVisitor visitor = new TypeVisitor() {
 *         &#64;Override
 *         public boolean visit(final &#64;NotNull ClassTypeSignature sig) {
 *             System.out.println("Encountered class: " + sig.getName());
 *             return true;
 *         }
 *     };
 *     visitor.accept(mySignature);
 * </code></pre>
 *
 * <p>By default, {@code TypeVisitor} only inspects the type hierarchy of the exact object given to {@link #accept(TypeRepresentable)}
 * without recursing into component types or type arguments. For deep recursive traversal into child elements, use
 * {@link TraversingTypeVisitor}.
 *
 * <p>Any {@code visit} method may return {@code false} to cancel the remaining traversal immediately.
 *
 * <p>When overriding {@code visit} methods in this interface, call {@code super.visit} to ensure that child
 * elements continue to be traversed.
 */
@SuppressWarnings("unused")
public interface TypeVisitor {

    /**
     * Dispatch the given {@link TypeRepresentable} to all relevant {@code visit} methods in the type hierarchy graph.
     *
     * <p>This method is the sole entry point for visiting any type. Callers should invoke this method rather than calling
     * {@code visit} methods directly.
     *
     * @param type The type to visit.
     * @return {@code true} if all hierarchy visits returned {@code true}, or {@code false} if any visit cancelled traversal.
     */
    default boolean accept(final @NotNull TypeRepresentable type) {
        return switch (type) {
            // PrimitiveType and VoidType are both descriptors and signatures at the same time.
            // To prevent duplicate visitor calls from the descriptor and signature hierarchies, they are manually
            // called here in order.
            case final PrimitiveType pt ->
                //noinspection DuplicatedCode
                this.visit((Descriptor) pt)
                    && this.visit((TypeDescriptor) pt)
                    && this.visit((Signature) pt)
                    && this.visit((TypeSignature) pt)
                    && this.visit((TypeBindable) pt)
                    && this.visit((ValueType) pt)
                    && this.visit(pt);
            case final VoidType vt ->
                //noinspection DuplicatedCode
                this.visit((Descriptor) vt)
                    && this.visit((TypeDescriptor) vt)
                    && this.visit((Signature) vt)
                    && this.visit((TypeSignature) vt)
                    && this.visit((TypeBindable) vt)
                    && this.visit((ValueType) vt)
                    && this.visit(vt);
            case final Descriptor desc -> this.visit(desc);
            case final Signature sig -> this.visit(sig);
            case final TypeParameter param ->
                this.visit((TypeBindable) param)
                    && this.visit(param);
            case final BoundedTypeArgument arg ->
                this.visit((TypeArgument) arg)
                    && this.visit((TypeBindable) arg)
                    && this.visit(arg);
            case final WildcardArgument arg ->
                this.visit((TypeArgument) arg)
                    && this.visit((TypeBindable) arg)
                    && this.visit(arg);
            case final WildcardBound bound -> this.visit(bound);
        };
    }

    /**
     * Visit a {@link PrimitiveType}.
     *
     * @param type The primitive type being visited.
     * @return {@code true} to continue traversal, or {@code false} to cancel immediately.
     */
    @ApiStatus.OverrideOnly
    @OverridingMethodsMustInvokeSuper
    default boolean visit(final @NotNull PrimitiveType type) {
        return true;
    }

    /**
     * Visit a {@link VoidType}.
     *
     * @param type The void type being visited.
     * @return {@code true} to continue traversal, or {@code false} to cancel immediately.
     */
    @ApiStatus.OverrideOnly
    @OverridingMethodsMustInvokeSuper
    default boolean visit(final @NotNull VoidType type) {
        return true;
    }

    /**
     * Visit any type that implements {@link ValueType}.
     *
     * @param type The value type being visited.
     * @return {@code true} to continue traversal, or {@code false} to cancel immediately.
     */
    @ApiStatus.OverrideOnly
    @OverridingMethodsMustInvokeSuper
    default boolean visit(final @NotNull ValueType type) {
        return true;
    }

    /**
     * Visit any type that implements {@link ClassType}.
     *
     * @param type The class type being visited.
     * @return {@code true} to continue traversal, or {@code false} to cancel immediately.
     */
    @ApiStatus.OverrideOnly
    @OverridingMethodsMustInvokeSuper
    default boolean visit(final @NotNull ClassType type) {
        return true;
    }

    /**
     * Visit any type that implements {@link ArrayType}.
     *
     * @param type The array type being visited.
     * @return {@code true} to continue traversal, or {@code false} to cancel immediately.
     */
    @ApiStatus.OverrideOnly
    @OverridingMethodsMustInvokeSuper
    default boolean visit(final @NotNull ArrayType type) {
        return true;
    }

    /**
     * Visit any type that implements {@link MethodType}.
     *
     * @param type The method type being visited.
     * @return {@code true} to continue traversal, or {@code false} to cancel immediately.
     */
    @ApiStatus.OverrideOnly
    @OverridingMethodsMustInvokeSuper
    default boolean visit(final @NotNull MethodType type) {
        return true;
    }

    // Descriptor
    /**
     * Visit a {@link Descriptor} and dispatch to its specific descriptor sub-category.
     *
     * @param desc The descriptor being visited.
     * @return {@code true} to continue traversal, or {@code false} to cancel immediately.
     */
    @ApiStatus.OverrideOnly
    @OverridingMethodsMustInvokeSuper
    default boolean visit(final @NotNull Descriptor desc) {
        return switch (desc) {
            case final PrimitiveType ignored -> true;
            case final VoidType ignored -> true;
            case final TypeDescriptor td ->
                this.visit((ValueType) td)
                    && this.visit(td);
            case final MethodDescriptor mtd ->
                this.visit((MethodType) mtd)
                    && this.visit(mtd);
        };
    }

    /**
     * Visit a {@link TypeDescriptor} and dispatch to its specific descriptor sub-category.
     *
     * @param desc The type descriptor being visited.
     * @return {@code true} to continue traversal, or {@code false} to cancel immediately.
     */
    @ApiStatus.OverrideOnly
    @OverridingMethodsMustInvokeSuper
    default boolean visit(final @NotNull TypeDescriptor desc) {
        return switch (desc) {
            case final PrimitiveType ignored -> true;
            case final VoidType ignored -> true;
            case final ClassTypeDescriptor ctd ->
                this.visit((ClassType) ctd)
                    && this.visit(ctd);
            case final ArrayTypeDescriptor atd ->
                this.visit((ArrayType) atd)
                    && this.visit(atd);
        };
    }

    /**
     * Visit a {@link ClassTypeDescriptor}.
     *
     * @param desc The class type descriptor being visited.
     * @return {@code true} to continue traversal, or {@code false} to cancel immediately.
     */
    @ApiStatus.OverrideOnly
    @OverridingMethodsMustInvokeSuper
    default boolean visit(final @NotNull ClassTypeDescriptor desc) {
        return true;
    }

    /**
     * Visit an {@link ArrayTypeDescriptor}.
     *
     * @param desc The array type descriptor being visited.
     * @return {@code true} to continue traversal, or {@code false} to cancel immediately.
     */
    @ApiStatus.OverrideOnly
    @OverridingMethodsMustInvokeSuper
    default boolean visit(final @NotNull ArrayTypeDescriptor desc) {
        return true;
    }

    /**
     * Visit a {@link MethodDescriptor}.
     *
     * @param desc The method descriptor being visited.
     * @return {@code true} to continue traversal, or {@code false} to cancel immediately.
     */
    @ApiStatus.OverrideOnly
    @OverridingMethodsMustInvokeSuper
    default boolean visit(final @NotNull MethodDescriptor desc) {
        return true;
    }

    // Signature
    /**
     * Visit a {@link Signature} and dispatch to its specific signature sub-category.
     *
     * @param sig The signature being visited.
     * @return {@code true} to continue traversal, or {@code false} to cancel immediately.
     */
    @ApiStatus.OverrideOnly
    @OverridingMethodsMustInvokeSuper
    default boolean visit(final @NotNull Signature sig) {
        return switch (sig) {
            case final PrimitiveType ignored -> true;
            case final VoidType ignored -> true;
            case final TypeSignature ts ->
                this.visit((TypeBindable) ts)
                    && this.visit((ValueType) ts)
                    && this.visit(ts);
            case final MethodSignature ms ->
                this.visit((TypeBindable) ms)
                    && this.visit((MethodType) ms)
                    && this.visit((TypeParameterHolder) ms)
                    && this.visit(ms);
            case final ClassSignature cs ->
                this.visit((TypeBindable) cs)
                    && this.visit((TypeParameterHolder) cs)
                    && this.visit(cs);
        };
    }

    /**
     * Visit a {@link TypeSignature} and dispatch to its specific signature sub-category.
     *
     * @param sig The type signature being visited.
     * @return {@code true} to continue traversal, or {@code false} to cancel immediately.
     */
    @ApiStatus.OverrideOnly
    @OverridingMethodsMustInvokeSuper
    default boolean visit(final @NotNull TypeSignature sig) {
        return switch (sig) {
            case final PrimitiveType ignored -> true;
            case final VoidType ignored -> true;
            case final ReferenceTypeSignature rts ->
                this.visit((TypeArgument) rts)
                    && this.visit(rts);
        };
    }

    /**
     * Visit a {@link ReferenceTypeSignature} and dispatch to its specific reference signature sub-category.
     *
     * @param sig The reference type signature being visited.
     * @return {@code true} to continue traversal, or {@code false} to cancel immediately.
     */
    @ApiStatus.OverrideOnly
    @OverridingMethodsMustInvokeSuper
    default boolean visit(final @NotNull ReferenceTypeSignature sig) {
        return switch (sig) {
            case final ClassTypeSignature cts ->
                this.visit((ClassType) cts)
                    && this.visit((ThrowsSignature) cts)
                    && this.visit(cts);
            case final ArrayTypeSignature ats ->
                this.visit((ArrayType) ats)
                    && this.visit(ats);
            case final TypeVariable tv ->
                this.visit((ThrowsSignature) tv)
                    && this.visit(tv);
            case final TypeVariable.Unbound u ->
                this.visit((ThrowsSignature) u)
                    && this.visit(u);
        };
    }

    /**
     * Visit a {@link ClassTypeSignature}.
     *
     * @param sig The class type signature being visited.
     * @return {@code true} to continue traversal, or {@code false} to cancel immediately.
     */
    @ApiStatus.OverrideOnly
    @OverridingMethodsMustInvokeSuper
    default boolean visit(final @NotNull ClassTypeSignature sig) {
        return true;
    }

    /**
     * Visit an {@link ArrayTypeSignature}.
     *
     * @param sig The array type signature being visited.
     * @return {@code true} to continue traversal, or {@code false} to cancel immediately.
     */
    @ApiStatus.OverrideOnly
    @OverridingMethodsMustInvokeSuper
    default boolean visit(final @NotNull ArrayTypeSignature sig) {
        return true;
    }

    /**
     * Visit a {@link TypeVariable}.
     *
     * @param var The type variable being visited.
     * @return {@code true} to continue traversal, or {@code false} to cancel immediately.
     */
    @ApiStatus.OverrideOnly
    @OverridingMethodsMustInvokeSuper
    default boolean visit(final @NotNull TypeVariable var) {
        return true;
    }

    /**
     * Visit an {@link TypeVariable.Unbound unbound} {@link TypeVariable}.
     *
     * @param var The unbound type variable being visited.
     * @return {@code true} to continue traversal, or {@code false} to cancel immediately.
     */
    @ApiStatus.OverrideOnly
    @OverridingMethodsMustInvokeSuper
    default boolean visit(final TypeVariable.@NotNull Unbound var) {
        return true;
    }

    /**
     * Visit a {@link MethodSignature}.
     *
     * @param sig The method signature being visited.
     * @return {@code true} to continue traversal, or {@code false} to cancel immediately.
     */
    @ApiStatus.OverrideOnly
    @OverridingMethodsMustInvokeSuper
    default boolean visit(final @NotNull MethodSignature sig) {
        return true;
    }

    /**
     * Visit a {@link ClassSignature}.
     *
     * @param sig The class signature being visited.
     * @return {@code true} to continue traversal, or {@code false} to cancel immediately.
     */
    @ApiStatus.OverrideOnly
    @OverridingMethodsMustInvokeSuper
    default boolean visit(final @NotNull ClassSignature sig) {
        return true;
    }

    /**
     * Visit any type that implements {@link ThrowsSignature}.
     *
     * @param sig The throws signature being visited.
     * @return {@code true} to continue traversal, or {@code false} to cancel immediately.
     */
    @ApiStatus.OverrideOnly
    @OverridingMethodsMustInvokeSuper
    default boolean visit(final @NotNull ThrowsSignature sig) {
        return true;
    }

    // Arg
    /**
     * Visit any type that implements {@link TypeArgument}.
     *
     * @param arg The type argument being visited.
     * @return {@code true} to continue traversal, or {@code false} to cancel immediately.
     */
    @ApiStatus.OverrideOnly
    @OverridingMethodsMustInvokeSuper
    default boolean visit(final @NotNull TypeArgument arg) {
        return true;
    }

    /**
     * Visit a {@link TypeParameter}.
     *
     * @param param The type parameter being visited.
     * @return {@code true} to continue traversal, or {@code false} to cancel immediately.
     */
    @ApiStatus.OverrideOnly
    @OverridingMethodsMustInvokeSuper
    default boolean visit(final @NotNull TypeParameter param) {
        return true;
    }

    /**
     * Visit a {@link BoundedTypeArgument}.
     *
     * @param arg The bounded type argument being visited.
     * @return {@code true} to continue traversal, or {@code false} to cancel immediately.
     */
    @ApiStatus.OverrideOnly
    @OverridingMethodsMustInvokeSuper
    default boolean visit(final @NotNull BoundedTypeArgument arg) {
        return true;
    }

    /**
     * Visit a {@link WildcardArgument}.
     *
     * @param arg The wildcard argument being visited.
     * @return {@code true} to continue traversal, or {@code false} to cancel immediately.
     */
    @ApiStatus.OverrideOnly
    @OverridingMethodsMustInvokeSuper
    default boolean visit(final @NotNull WildcardArgument arg) {
        return true;
    }

    /**
     * Visit a {@link WildcardBound}.
     *
     * @param bound The wildcard bound being visited.
     * @return {@code true} to continue traversal, or {@code false} to cancel immediately.
     */
    @ApiStatus.OverrideOnly
    @OverridingMethodsMustInvokeSuper
    default boolean visit(final @NotNull WildcardBound bound) {
        return true;
    }

    // Others
    /**
     * Visit any type that implements {@link TypeBindable}.
     *
     * @param bindable The bindable type being visited.
     * @return {@code true} to continue traversal, or {@code false} to cancel immediately.
     */
    @ApiStatus.OverrideOnly
    @OverridingMethodsMustInvokeSuper
    default boolean visit(final @NotNull TypeBindable bindable) {
        return true;
    }

    /**
     * Visit any type that implements {@link TypeParameterHolder}.
     *
     * @param holder The type parameter holder being visited.
     * @return {@code true} to continue traversal, or {@code false} to cancel immediately.
     */
    @ApiStatus.OverrideOnly
    @OverridingMethodsMustInvokeSuper
    default boolean visit(final @NotNull TypeParameterHolder holder) {
        return true;
    }
}
