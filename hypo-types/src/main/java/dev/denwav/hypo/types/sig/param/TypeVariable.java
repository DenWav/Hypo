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
import dev.denwav.hypo.types.TypeRepresentable;
import dev.denwav.hypo.types.TypeVariableBinder;
import dev.denwav.hypo.types.desc.ClassTypeDescriptor;
import dev.denwav.hypo.types.desc.TypeDescriptor;
import dev.denwav.hypo.types.sig.ReferenceTypeSignature;
import dev.denwav.hypo.types.sig.ThrowsSignature;
import dev.denwav.hypo.types.sig.TypeSignature;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * A type varaible is a reference to a type parameter in a type signature. Type variables can be used as standalone
 * types or as bounds for other type definitions.
 *
 * <p>This model only considers a type variable to be fully defined if the corresponding {@link TypeParameter} is also
 * specified. The type parameter definition corresponding to a type variable allows the type variable usage to be
 * properly replaced when type erasure occurs, such as when transforming a signature into a descriptor.
 *
 * <p>When a type variable is initially parsed it will be considered {@link Unbound unbound}, which means no type
 * parameter definition has been specified for the type variable. Attempting to call {@link #asDescriptor()} on an
 * unbound type variable will fail.
 *
 * <p>Type parameter definitions attach to a type variable following scoping rules defined in the JLS. In most code type
 * parameter definitions do not shadow other type parameter definitions, but it is possible. Given the following code:
 *
 * <pre><code>
 *     public class Foo&lt;T&gt; {
 *         public &lt;T&gt; void bar(T t) { ... }
 *     }
 * </code></pre>
 *
 * <p>The type variable {@code T} used as the type for the first parameter of the {@code bar} method will bind to the
 * type parmater {@code T} of the <i>method</i> {@code bar}, rather than to the type parameter of the class {@code Foo}.
 */
@Immutable
public final class TypeVariable
    extends Intern<TypeVariable>
    implements ReferenceTypeSignature, TypeRepresentable, ThrowsSignature {

    private final @NotNull TypeParameter definition;

    /**
     * Create a new bounded {@link TypeParameter} instance.
     * @param definition The type parameter bound of this type variable.
     * @return The new {@link TypeVariable}.
     */
    public static TypeVariable of(final @NotNull TypeParameter definition) {
        return new TypeVariable(definition).intern();
    }

    /**
     * Create a new {@link TypeParameter} instance from the given {@link java.lang.reflect.TypeVariable}.
     *
     * <p>The returned parameter will be bound as long as the given type variable has defined bounds from
     * {@link java.lang.reflect.TypeVariable#getBounds()}.
     *
     * @param typeVar The Java reflection object to create a {@link TypeVariable} from.
     * @return The new {@link TypeVariable}.
     */
    public static @NotNull TypeVariable of(final @NotNull java.lang.reflect.TypeVariable<?> typeVar) {
        final TypeParameter bindingParam;
        final Type[] bounds = typeVar.getBounds();
        if (bounds.length == 0) {
            bindingParam = TypeParameter.of(typeVar.getName());
        } else if (bounds.length == 1) {
            bindingParam = TypeParameter.of(typeVar.getName(), (ReferenceTypeSignature) TypeSignature.of(bounds[0]));
        } else {
            final ReferenceTypeSignature[] interfaceBounds = new ReferenceTypeSignature[bounds.length - 1];
            for (int i = 1; i < bounds.length; i++) {
                interfaceBounds[i - 1] = (ReferenceTypeSignature) TypeSignature.of(bounds[i]);
            }
            bindingParam = TypeParameter.of(
                typeVar.getName(),
                (ReferenceTypeSignature) TypeSignature.of(bounds[0]),
                Arrays.asList(interfaceBounds)
            );
        }
        return TypeVariable.of(bindingParam);
    }

    /**
     * Create a new {@link Unbound unbound} {@link TypeParameter} instance.
     * @param name The name of the unbound type variable.
     * @return The new {@link Unbound unbound} {@link TypeParameter} instance.
     */
    public static Unbound unbound(final @NotNull String name) {
        return new TypeVariable.Unbound(name).intern();
    }

    private TypeVariable(final @NotNull TypeParameter definition) {
        this.definition = definition;
    }

    @Override
    public void asReadable(final @NotNull StringBuilder sb) {
        sb.append(this.definition.getName());
    }

    @Override
    public void asInternal(final @NotNull StringBuilder sb) {
        this.asInternal(sb, false);
    }

    @Override
    public void asInternal(final @NotNull StringBuilder sb, final boolean withBindKey) {
        if (withBindKey) {
            sb.append("{BIND:");
            this.definition.asInternal(sb, true);
            sb.append('}');
        }
        sb.append('T');
        sb.append(this.definition.getName());
        sb.append(';');
    }

    @Override
    public @NotNull TypeDescriptor asDescriptor() {
        {
            final ReferenceTypeSignature bound = this.definition.getClassBound();
            if (bound != null) {
                return bound.asDescriptor();
            }
        }
        for (final ReferenceTypeSignature bound : this.definition.getInterfaceBounds()) {
            return bound.asDescriptor();
        }
        return ClassTypeDescriptor.of("java/lang/Object");
    }

    @Override
    public @NotNull TypeVariable bind(final @NotNull TypeVariableBinder binder) {
        return this;
    }

    @Override
    public boolean isUnbound() {
        return this.definition.isUnbound();
    }

    /**
     * Return the {@link TypeParameter} bound of this type variable.
     * @return The {@link TypeParameter} bound of this type variable.
     */
    public @NotNull TypeParameter getDefinition() {
        return this.definition;
    }

    /**
     * Return the name of the type variable, which matches the name of the corresponding {@link TypeParameter}.
     * @return The name of the type variable.
     */
    public @NotNull String getName() {
        return this.definition.getName();
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof final TypeVariable that)) {
            return false;
        }
        return Objects.equals(this.definition, that.definition);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.definition);
    }

    @Override
    public String toString() {
        return this.asReadable();
    }

    /**
     * A {@link TypeParameter} which is not bound to a definint {@link TypeParameter}. This type cannot be properly
     * erased into a {@link TypeDescriptor}, so {@link #asDescriptor()} will always fail. Use
     * {@link #bind(TypeVariableBinder)} to convert this into a bound {@link TypeVariable}, as long as the
     * {@link TypeVariableBinder} given includes a matching {@link TypeParameter} definition for this type variable.
     *
     * <p>Create new instances of this class using {@link TypeVariable#unbound(String)}.
     */
    public static final class Unbound
        extends Intern<Unbound>
        implements ReferenceTypeSignature, TypeRepresentable, ThrowsSignature {

        private final @NotNull String name;

        private Unbound(final @NotNull String name) {
            this.name = name;
        }

        /**
         * Return the name of this type variable.
         * @return The name of this type variable.
         */
        public @NotNull String getName() {
            return this.name;
        }

        @Override
        public @NotNull TypeVariable bind(final @NotNull TypeVariableBinder binder) {
            final TypeParameter param = binder.bindingFor(this.name);
            if (param == null) {
                throw new IllegalStateException("TypeParameter not found for name: " + this.name);
            }
            return TypeVariable.of(param);
        }

        @Override
        public boolean isUnbound() {
            return true;
        }

        @Override
        public void asReadable(final @NotNull StringBuilder sb) {
            sb.append(this.name);
        }

        @Override
        public void asInternal(final @NotNull StringBuilder sb) {
            this.asInternal(sb, false);
        }

        @Override
        public void asInternal(final @NotNull StringBuilder sb, final boolean withBindKey) {
            if (withBindKey) {
                sb.append("{UNBOUND}");
            }
            sb.append('T');
            sb.append(this.name);
            sb.append(';');
        }

        @Override
        public @NotNull TypeDescriptor asDescriptor() {
            throw new IllegalStateException(
                "TypeVariable must be bounded by calling bind() in order to create a TypeDescriptor"
            );
        }

        @Override
        public boolean equals(final Object o) {
            if (!(o instanceof final Unbound that)) {
                return false;
            }
            return Objects.equals(this.name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(this.name);
        }

        @Override
        public String toString() {
            return this.asReadable();
        }
    }
}
