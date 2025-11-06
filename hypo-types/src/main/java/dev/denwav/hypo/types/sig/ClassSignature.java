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
import dev.denwav.hypo.types.intern.Intern;
import dev.denwav.hypo.types.TypeBindable;
import dev.denwav.hypo.types.TypeRepresentable;
import dev.denwav.hypo.types.TypeVariableBinder;
import dev.denwav.hypo.types.parsing.JvmTypeParseFailureException;
import dev.denwav.hypo.types.parsing.JvmTypeParser;
import dev.denwav.hypo.types.sig.param.TypeParameter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

/**
 * A <a href="https://docs.oracle.com/javase/specs/jvms/se23/html/jvms-4.html#jvms-4.7.9.1-400">JVM class signature</a>.
 *
 * <p>A class signature defines the type parameters of a class declaration, as well as the generic super class and super
 * interface definitions. Every part after the class name and before the opening brace of a class declaration is part of
 * the class signature.
 *
 * <p>For example, the following class signature defines a class named {@code Foo} with an {@code extends} and
 * {@code implements} clauses:
 *
 * <pre><code>
 *     class Foo&lt;T extends Number&gt; extends List&lt;T&gt; implements Serializable {
 * </code></pre>
 *
 * <p>The {@link #getTypeParameters() type parameters} of a class signature match up with the
 * {@link ClassTypeSignature#getTypeArguments() type arguemnts} of corresponding {@link ClassTypeSignature class type
 * signatures} for the same type.
 */
@Immutable
public final class ClassSignature extends Intern<ClassSignature> implements Signature, TypeBindable, TypeRepresentable, TypeParameterHolder {

    @SuppressWarnings("Immutable")
    private final @NotNull List<? extends TypeParameter> typeParameters;
    private final @NotNull ClassTypeSignature superClass;
    @SuppressWarnings("Immutable")
    private final @NotNull List<? extends ClassTypeSignature> superInterfaces;

    /**
     * Create a new {@link ClassSignature}.
     * @param typeParameters The class's {@link TypeParameter type parameters}.
     * @param superClass The class's generic super class.
     * @param superInterfaces The class's generic super interfaces.
     * @return A new {@link ClassSignature}.
     */
    public static @NotNull ClassSignature of(
        final @NotNull List<? extends TypeParameter> typeParameters,
        final @NotNull ClassTypeSignature superClass,
        final @NotNull List<? extends ClassTypeSignature> superInterfaces
    ) {
        return new ClassSignature(typeParameters, superClass, superInterfaces).intern();
    }

    /**
     * Parse the given internal JVM class signature text into a new {@link ClassSignature}.
     *
     * <p>This method throws {@link JvmTypeParseFailureException} if the given text is not a valid class signature. Use
     * {@link JvmTypeParser#parseClassSignature(String, int)} if you prefer to have {@code null} be returned instead.
     *
     * @param text The text to parse.
     * @return The {@link ClassSignature}.
     * @throws JvmTypeParseFailureException If the given text does not represent a valid JVM class signature.
     */
    public static @NotNull ClassSignature parse(final String text) throws JvmTypeParseFailureException {
        return parse(text, 0);
    }

    /**
     * Parse the given internal JVM class signature text into a new {@link ClassSignature}.
     *
     * <p>This method throws {@link JvmTypeParseFailureException} if the given text is not a valid class signature. Use
     * {@link JvmTypeParser#parseClassSignature(String, int)} if you prefer to have {@code null} be returned instead.
     *
     * @param text The text to parse.
     * @param from The index to start parsing from.
     * @return The {@link ClassSignature}.
     * @throws JvmTypeParseFailureException If the given text does not represent a valid JVM class signature.
     */

    public static @NotNull ClassSignature parse(final String text, final int from) throws JvmTypeParseFailureException {
        if (text.length() > 1 && from == 0) {
            final ClassSignature r = Intern.tryFind(ClassSignature.class, text);
            if (r != null) {
                return r;
            }
        }
        final ClassSignature result = JvmTypeParser.parseClassSignature(text, from);
        if (result == null) {
            throw new JvmTypeParseFailureException("text is not a valid class signature: " + text.substring(from));
        }
        return result;
    }

    private ClassSignature(
        final @NotNull List<? extends TypeParameter> typeParameters,
        final @NotNull ClassTypeSignature superClass,
        final @NotNull List<? extends ClassTypeSignature> superInterfaces
    ) {
        this.typeParameters = List.copyOf(typeParameters);
        this.superClass = superClass;
        this.superInterfaces = List.copyOf(superInterfaces);
    }

    @Override
    public void asReadable(final @NotNull StringBuilder sb) {
        if (!this.typeParameters.isEmpty()) {
            sb.append('<');
            for (int i = 0; i < this.typeParameters.size(); i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                this.typeParameters.get(i).asReadable(sb);
            }
            sb.append("> ");
        }

        sb.append("extends ");
        this.superClass.asReadable(sb);

        if (!this.superInterfaces.isEmpty()) {
            sb.append(" implements ");
        }
        for (int i = 0; i < this.superInterfaces.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            this.superInterfaces.get(i).asReadable(sb);
        }
    }

    @Override
    public void asInternal(final @NotNull StringBuilder sb) {
        this.asInternal(sb, false);
    }

    @Override
    public void asInternal(final @NotNull StringBuilder sb, final boolean withBindKey) {
        if (!this.typeParameters.isEmpty()) {
            sb.append('<');
            for (final TypeParameter param : this.typeParameters) {
                param.asInternal(sb, withBindKey);
            }
            sb.append('>');
        }
        this.superClass.asInternal(sb, withBindKey);
        for (final TypeSignature superInt : this.superInterfaces) {
            superInt.asInternal(sb, withBindKey);
        }
    }

    @Override
    public @NotNull ClassSignature bind(final @NotNull TypeVariableBinder binder) {
        return ClassSignature.of(
            this.typeParameters.stream()
                .map(t -> t.bind(binder))
                .collect(Collectors.toList()),
            this.superClass.bind(binder),
            this.superInterfaces.stream()
                .map(t -> t.bind(binder))
                .collect(Collectors.toList())
        );
    }

    @Override
    public boolean isUnbound() {
        for (final TypeParameter p : this.typeParameters) {
            if (p.isUnbound()) {
                return true;
            }
        }
        if (this.superClass.isUnbound()) {
            return true;
        }
        for (final ClassTypeSignature c : this.superInterfaces) {
            if (c.isUnbound()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public @NotNull List<? extends TypeParameter> getTypeParameters() {
        return this.typeParameters;
    }

    /**
     * Get the generic super class of this class.
     * @return The generic super class of this class.
     */
    public @NotNull ClassTypeSignature getSuperClass() {
        return this.superClass;
    }

    /**
     * Get the generic super interfaces of this class.
     * @return The generic super interfaces of this class.
     */
    public @NotNull List<? extends ClassTypeSignature> getSuperInterfaces() {
        return this.superInterfaces;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof final ClassSignature that)) {
            return false;
        }
        return Objects.equals(this.typeParameters, that.typeParameters)
            && Objects.equals(this.superClass, that.superClass)
            && Objects.equals(this.superInterfaces, that.superInterfaces);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.typeParameters, this.superClass, this.superInterfaces);
    }

    @Override
    public String toString() {
        return this.asReadable();
    }
}
