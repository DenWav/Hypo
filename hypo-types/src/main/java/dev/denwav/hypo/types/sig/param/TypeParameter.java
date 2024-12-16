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

package dev.denwav.hypo.types.sig.param;

import com.google.errorprone.annotations.Immutable;
import dev.denwav.hypo.types.intern.Intern;
import dev.denwav.hypo.types.TypeBindable;
import dev.denwav.hypo.types.TypeRepresentable;
import dev.denwav.hypo.types.TypeVariableBinder;
import dev.denwav.hypo.types.sig.ClassTypeSignature;
import dev.denwav.hypo.types.sig.ReferenceTypeSignature;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A type parameter is the type variable binding for a generic type. Type parameters can be present on
 * {@link dev.denwav.hypo.types.sig.ClassSignature ClassSignatures} and
 * {@link dev.denwav.hypo.types.sig.MethodSignature MethodSignatures}, and may reference other type parameters through
 * corresponding type variables.
 *
 * <p>Each declared type parameter <i>may</i> be referenced elsewhere in the type signature as a {@link TypeVariable},
 * but that it is not required. For example, the following method declaration declares a type parameter {@code T}, but
 * does not use it:
 *
 * <pre><code>
 *     static &lt;T&gt; void foo() { ... }
 * </code></pre>
 *
 * <p>Type parameters may define class and interface bounds to be more specific about which types are allowed to be used
 * as type arguments for the said parameter. If no class or interface bounds are specified, then the type parameter is
 * implied to have a class bound of {@code java/lang/Object}.
 */
@Immutable
public final class TypeParameter extends Intern<TypeParameter> implements TypeBindable, TypeRepresentable {

    private final @NotNull String name;
    private final @Nullable ReferenceTypeSignature classBound;
    @SuppressWarnings("Immutable")
    private final @NotNull List<? extends ReferenceTypeSignature> interfaceBounds;

    /**
     * Create a new instance of a {@link TypeParameter}.
     * @param name The name of the type parameter, as it appears in the Java source code.
     * @param classBound The optional class bound of the type parameter.
     * @param interfaceBounds The optional interface bounds of the type parameter.
     * @return The new {@link TypeParameter}.
     */
    public static @NotNull TypeParameter of(
        final @NotNull String name,
        final @Nullable ReferenceTypeSignature classBound,
        final @NotNull List<? extends ReferenceTypeSignature> interfaceBounds
    ) {
        return new TypeParameter(name, classBound, interfaceBounds).intern();
    }

    /**
     * Create a new instance of a {@link TypeParameter} with a specified class bound and no interface bounds.
     * @param name The name of the type parameter, as it appears in the Java source code.
     * @param classBound The class bound of the type parameter.
     * @return The new {@link TypeParameter}.
     */
    public static @NotNull TypeParameter of(
        final @NotNull String name,
        final @NotNull ReferenceTypeSignature classBound
    ) {
        return new TypeParameter(name, classBound, List.of()).intern();
    }

    /**
     * Create a new instance of a {@link TypeParameter} with no class bound and no interface bounds.
     * @param name The name of the type parameter, as it appears in the Java source code.
     * @return The new {@link TypeParameter}.
     */
    public static @NotNull TypeParameter of(
        final @NotNull String name
    ) {
        return new TypeParameter(name, ClassTypeSignature.of("java/lang/Object"), List.of()).intern();
    }

    private TypeParameter(
        final @NotNull String name,
        final @Nullable ReferenceTypeSignature classBound,
        final @NotNull List<? extends ReferenceTypeSignature> interfaceBounds
    ) {
        this.name = name;
        this.classBound = classBound;
        this.interfaceBounds = List.copyOf(interfaceBounds);

        if (classBound == null && this.interfaceBounds.isEmpty()) {
            throw new IllegalArgumentException(
                "Cannot construct a type with empty classBound and interfaceBounds, at least one must be set."
            );
        }
    }

    @Override
    public void asReadable(final @NotNull StringBuilder sb) {
        sb.append(this.name);

        if (this.classBound != null || !this.interfaceBounds.isEmpty()) {
            sb.append(" extends ");
        }

        if (this.classBound != null) {
            this.classBound.asReadable(sb);
        }

        for (int i = 0; i < this.interfaceBounds.size(); i++) {
            if (i > 0 || this.classBound != null) {
                sb.append(" & ");
            }
            this.interfaceBounds.get(i).asReadable(sb);
        }
    }

    @Override
    public void asInternal(final @NotNull StringBuilder sb) {
        this.asInternal(sb, false);
    }

    @Override
    public void asInternal(final @NotNull StringBuilder sb, final boolean withBindKey) {
        sb.append(this.name);

        sb.append(':');
        if (this.classBound != null) {
            this.classBound.asInternal(sb, withBindKey);
        }

        for (final ReferenceTypeSignature interfaceBound : this.interfaceBounds) {
            sb.append(':');
            interfaceBound.asInternal(sb, withBindKey);
        }
    }

    @Override
    public @NotNull TypeParameter bind(final @NotNull TypeVariableBinder binder) {
        return TypeParameter.of(
            this.name,
            binder.bind(this.classBound),
            this.interfaceBounds.stream().map(binder::bind).collect(Collectors.toList())
        );
    }

    @Override
    public boolean isUnbound() {
        if (this.classBound != null && this.classBound.isUnbound()) {
            return true;
        }
        for (final ReferenceTypeSignature b : this.interfaceBounds) {
            if (b.isUnbound()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the name of the type parameter, as it appears in the Java source code.
     * @return The name of the type parameter.
     */
    public @NotNull String getName() {
        return this.name;
    }

    /**
     * Get the optional class bound of the type parameter.
     * @return The class bound of the type parameter, or {@code null} if no class bound is specified.
     */
    public @Nullable ReferenceTypeSignature getClassBound() {
        return this.classBound;
    }

    /**
     * Get the optional interface bounds of the type parameter.
     * @return The interface bounds of the type parameter, or an empty list if no interface bounds are specified.
     */
    public @NotNull List<? extends ReferenceTypeSignature> getInterfaceBounds() {
        return this.interfaceBounds;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof final TypeParameter that)) {
            return false;
        }
        return Objects.equals(this.name, that.name)
            && Objects.equals(this.classBound, that.classBound)
            && Objects.equals(this.interfaceBounds, that.interfaceBounds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.classBound, this.interfaceBounds);
    }

    @Override
    public String toString() {
        return this.asReadable();
    }
}
