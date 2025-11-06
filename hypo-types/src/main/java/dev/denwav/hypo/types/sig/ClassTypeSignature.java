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

package dev.denwav.hypo.types.sig;

import com.google.errorprone.annotations.Immutable;
import dev.denwav.hypo.types.HypoTypesUtil;
import dev.denwav.hypo.types.intern.Intern;
import dev.denwav.hypo.types.TypeVariableBinder;
import dev.denwav.hypo.types.desc.ClassTypeDescriptor;
import dev.denwav.hypo.types.desc.TypeDescriptor;
import dev.denwav.hypo.types.kind.ClassType;
import dev.denwav.hypo.types.sig.param.TypeArgument;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A {@link ReferenceTypeSignature} representing a class type. Class type signatures can contain type arguments,
 * which correspond to the {@link ClassSignature#getTypeParameters() type parameters} of the class's signature.
 *
 * <p>The class {@link #getName() name} will typically be the fully qualified class name, except when this class
 * type signature also contains an {@link #getOwnerClass() owner}. In that case, the name of this class will only
 * contain the simple name of this class, which is appended after the fully qualified name of the owner class.
 *
 * <p>Owner classes in a class type signature occur when a non-static class is nested within another generic class. For
 * example, the {@code Bar} class here is a nested class of the {@code Foo} class:
 *
 * <pre><code>
 *     package com.example;
 *
 *     class Foo&lt;T&gt; {
 *         class Bar&lt;U&gt; { ... }
 *     }
 *
 *     Foo&lt;String&gt;.Bar&lt;Integer&gt; baz;
 * </code></pre>
 *
 * <p>In this scenario, the fully qualified name of the {@code baz} type is {@code com.example.Foo.Bar}, and the fully
 * qualified type signature is <code>com/example/Foo&lt;String&gt;.Bar&lt;Integer&gt;</code>.
 *
 * <p>Note that generic nested static classes do not have an owner, since they are static, so they do not inherit any
 * generic type information from the nest-host class.
 */
@Immutable
public final class ClassTypeSignature
    extends Intern<ClassTypeSignature>
    implements ReferenceTypeSignature, ClassType, ThrowsSignature {

    private final @Nullable ClassTypeSignature ownerClass;
    private final @NotNull String name;
    @SuppressWarnings("Immutable")
    private final @NotNull List<? extends TypeArgument> typeArguments;

    /**
     * Create a new {@link ClassTypeSignature} from the given {@code name}.
     * @param name The name of the class.
     * @return A new {@link ClassTypeSignature}.
     */
    public static @NotNull ClassTypeSignature of(
        final @NotNull String name
    ) {
        return ClassTypeSignature.of(null, name, null);
    }

    /**
     * Create a new {@link ClassTypeSignature} from the given {@code name} with the given {@code typeArguments}.
     * @param name The name of the class.
     * @param typeArguments The type arguments.
     * @return A new {@link ClassTypeSignature}.
     */
    public static @NotNull ClassTypeSignature of(
        final @NotNull String name,
        final @Nullable List<? extends TypeArgument> typeArguments
    ) {
        return ClassTypeSignature.of(null, name, typeArguments);
    }

    /**
     * Create a new {@link ClassTypeSignature} from the given {@code ownerClass}, {@code name}, and
     * {@code typeArguments}.
     *
     * @param ownerClass The owner class.
     * @param name The name of the class.
     * @param typeArguments The type arguments.
     * @return A new {@link ClassTypeSignature}.
     */
    public static @NotNull ClassTypeSignature of(
        final @Nullable ClassTypeSignature ownerClass,
        final @NotNull String name,
        final @Nullable List<? extends TypeArgument> typeArguments
    ) {
        return new ClassTypeSignature(ownerClass, HypoTypesUtil.normalizedClassName(name), typeArguments).intern();
    }

    /**
     * Create a {@link ClassTypeSignature} matching the given {@link ParameterizedType}.
     * @param parameterizedType The type.
     * @return A new {@link ClassTypeSignature} matching the given {@link ParameterizedType}.
     */
    public static @NotNull ClassTypeSignature of(final @NotNull ParameterizedType parameterizedType) {
        final Type owner = parameterizedType.getOwnerType();
        final ClassTypeSignature ownerType = owner != null ? (ClassTypeSignature) TypeSignature.of(owner) : null;
        final ClassTypeSignature rawType = (ClassTypeSignature) TypeSignature.of(parameterizedType.getRawType());

        final String className;
        if (ownerType == null) {
            className = rawType.getName();
        } else {
            final String rawName = rawType.getName();
            className = rawName.substring(rawName.lastIndexOf('.') + 1);
        }

        final Type[] actualTypeArgs = parameterizedType.getActualTypeArguments();
        final ArrayList<TypeArgument> typeArgs = new ArrayList<>(actualTypeArgs.length);
        for (final Type actualTypeArg : actualTypeArgs) {
            typeArgs.add((TypeArgument) TypeSignature.of(actualTypeArg));
        }

        return ClassTypeSignature.of(ownerType, className, typeArgs);
    }

    private ClassTypeSignature(
        final @Nullable ClassTypeSignature ownerClass,
        final @NotNull String name,
        final @Nullable List<? extends TypeArgument> typeArguments
    ) {
        this.ownerClass = ownerClass;
        this.name = name;
        if (typeArguments == null) {
            this.typeArguments = Collections.emptyList();
        } else {
            this.typeArguments = List.copyOf(typeArguments);
        }
    }

    @Override
    public void asReadable(final @NotNull StringBuilder sb) {
        if (this.ownerClass != null) {
            this.ownerClass.asReadable(sb);
            sb.append('.');
        }

        sb.append(this.name.replace('/', '.'));
        if (!this.typeArguments.isEmpty()) {
            sb.append("<");
            for (int i = 0; i < this.typeArguments.size(); i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                this.typeArguments.get(i).asReadable(sb);
            }
            sb.append('>');
        }
    }

    @Override
    public void asInternal(final @NotNull StringBuilder sb) {
        this.asInternal(sb, false);
    }

    @Override
    public void asInternal(final @NotNull StringBuilder sb, final boolean withBindKey) {
        if (this.ownerClass != null) {
            this.ownerClass.asInternal(sb, withBindKey);
            // Owner class will include a `;` at the end
            sb.setLength(sb.length() - 1);
            sb.append('.');
        } else {
            sb.append('L');
        }

        sb.append(this.name);
        if (!this.typeArguments.isEmpty()) {
            sb.append("<");
            for (final TypeArgument typeArg : this.typeArguments) {
                typeArg.asInternal(sb, withBindKey);
            }
            sb.append('>');
        }

        sb.append(';');
    }

    @Override
    public @NotNull TypeDescriptor asDescriptor() {
        return ClassTypeDescriptor.of(this.name);
    }

    @Override
    public @NotNull ClassTypeSignature bind(final @NotNull TypeVariableBinder binder) {
        ClassTypeSignature newOwner;
        if (this.ownerClass == null) {
            newOwner = null;
        } else {
            newOwner = this.ownerClass.bind(binder);
        }
        return ClassTypeSignature.of(
            newOwner,
            this.name,
            this.typeArguments.stream()
                .map(t -> t.bind(binder))
                .collect(Collectors.toList())
        );
    }

    @Override
    public boolean isUnbound() {
        if (this.ownerClass != null && this.ownerClass.isUnbound()) {
            return true;
        }
        for (final TypeArgument t : this.typeArguments) {
            if (t.isUnbound()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the owner class of this type, if present. Returns {@code null} if this type does not have an owner.
     * @return The owner of this type.
     */
    public @Nullable ClassTypeSignature getOwnerClass() {
        return this.ownerClass;
    }

    @Override
    public @NotNull String getName() {
        return this.name;
    }

    /**
     * Get the type arguments of this type. If this type has no type arguments, an empty list is returned. The returned
     * list is immutable.
     * @return The type arguments of this type.
     */
    public @NotNull List<? extends TypeArgument> getTypeArguments() {
        return this.typeArguments;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof final ClassTypeSignature that)) {
            return false;
        }
        return Objects.equals(this.ownerClass, that.ownerClass)
            && Objects.equals(this.name, that.name)
            && Objects.equals(this.typeArguments, that.typeArguments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.ownerClass, this.name, this.typeArguments);
    }

    @Override
    public String toString() {
        return this.asReadable();
    }
}
