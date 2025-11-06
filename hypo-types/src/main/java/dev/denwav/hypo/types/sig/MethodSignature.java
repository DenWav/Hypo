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
import dev.denwav.hypo.types.TypeBindable;
import dev.denwav.hypo.types.kind.MethodType;
import dev.denwav.hypo.types.intern.Intern;
import dev.denwav.hypo.types.TypeRepresentable;
import dev.denwav.hypo.types.TypeVariableBinder;
import dev.denwav.hypo.types.desc.MethodDescriptor;
import dev.denwav.hypo.types.desc.TypeDescriptor;
import dev.denwav.hypo.types.parsing.JvmTypeParseFailureException;
import dev.denwav.hypo.types.parsing.JvmTypeParser;
import dev.denwav.hypo.types.sig.param.TypeParameter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

/**
 * A <a href="https://docs.oracle.com/javase/specs/jvms/se23/html/jvms-4.html#jvms-4.7.9.1-500">JVM method signature</a>.
 *
 * <p>A method signature contains {@link TypeParameterHolder type paramters}, {@link TypeSignature potentially generic}
 * parameters, a return type (also a {@link TypeSignature}), and a {@link ThrowsSignature throws signature}, also
 * potentially generic.
 */
@Immutable
public final class MethodSignature
    extends Intern<MethodSignature>
    implements MethodType, Signature, TypeRepresentable, TypeBindable, TypeParameterHolder {

    @SuppressWarnings("Immutable")
    private final @NotNull List<? extends TypeParameter> typeParameters;
    @SuppressWarnings("Immutable")
    private final @NotNull List<? extends TypeSignature> parameters;
    private final @NotNull TypeSignature returnType;
    @SuppressWarnings("Immutable")
    private final @NotNull List<? extends ThrowsSignature> throwsSignatures;

    /**
     * Create a new {@link MethodSignature}.
     * @param typeParameters The method's {@link TypeParameter type parameters}.
     * @param parameters The method's parameters.
     * @param returnType The method's return type.
     * @param throwsSignatures The method's throws signature.
     * @return A new {@link MethodSignature}.
     */
    public static @NotNull MethodSignature of(
        final @NotNull List<? extends TypeParameter> typeParameters,
        final @NotNull List<? extends TypeSignature> parameters,
        final @NotNull TypeSignature returnType,
        final @NotNull List<? extends ThrowsSignature> throwsSignatures
    ) {
        return new MethodSignature(typeParameters, parameters, returnType, throwsSignatures).intern();
    }

    private MethodSignature(
        final @NotNull List<? extends TypeParameter> typeParameters,
        final @NotNull List<? extends TypeSignature> parameters,
        final @NotNull TypeSignature returnType,
        final @NotNull List<? extends ThrowsSignature> throwsSignatures
    ) {
        this.typeParameters = List.copyOf(typeParameters);
        this.parameters = List.copyOf(parameters);
        this.returnType = returnType;
        this.throwsSignatures = List.copyOf(throwsSignatures);
    }

    /**
     * Parse the given internal JVM method signature text into a new {@link MethodSignature}.
     *
     * <p>This method throws {@link JvmTypeParseFailureException} if the given text is not a valid method signature. Use
     * {@link JvmTypeParser#parseMethodSignature(String, int)} if you prefer to have {@code null} be returned instead.
     *
     * @param text The text to parse.
     * @return The {@link MethodSignature}.
     * @throws JvmTypeParseFailureException If the given text does not represent a valid JVM method signature.
     */
    public static @NotNull MethodSignature parse(final @NotNull String text) throws JvmTypeParseFailureException {
        return parse(text, 0);
    }

    /**
     * Parse the given internal JVM method signature text into a new {@link MethodSignature}.
     *
     * <p>This method throws {@link JvmTypeParseFailureException} if the given text is not a valid method signature. Use
     * {@link JvmTypeParser#parseMethodSignature(String, int)} if you prefer to have {@code null} be returned instead.
     *
     * @param text The text to parse.
     * @param from The index to start parsing from.
     * @return The {@link MethodSignature}.
     * @throws JvmTypeParseFailureException If the given text does not represent a valid JVM method signature.
     */
    public static @NotNull MethodSignature parse(final @NotNull String text, final int from) throws JvmTypeParseFailureException {
        if (text.length() > 1 && from == 0) {
            final MethodSignature r = Intern.tryFind(MethodSignature.class, text);
            if (r != null) {
                return r;
            }
        }
        final MethodSignature result = JvmTypeParser.parseMethodSignature(text, from);
        if (result == null) {
            throw new JvmTypeParseFailureException("text is not a valid method signature: " + text.substring(from));
        }
        return result;
    }

    @Override
    public @NotNull MethodSignature bind(final @NotNull TypeVariableBinder binder) {
        final List<TypeParameter> newTypeParams = this.typeParameters.stream()
            .map(t -> t.bind(binder))
            .collect(Collectors.toList());

        final List<TypeSignature> newParams = this.parameters.stream()
            .map(t -> t.bind(binder))
            .collect(Collectors.toList());

        final TypeSignature newReturnType = this.returnType.bind(binder);

        final List<ThrowsSignature> newThrows = this.throwsSignatures.stream()
            .map(t -> t.bind(binder))
            .collect(Collectors.toList());

        return MethodSignature.of(newTypeParams, newParams, newReturnType, newThrows);
    }

    @Override
    public boolean isUnbound() {
        for (final TypeParameter t : this.typeParameters) {
            if (t.isUnbound()) {
                return true;
            }
        }
        for (final TypeSignature p : this.parameters) {
            if (p.isUnbound()) {
                return true;
            }
        }
        if (this.returnType.isUnbound()) {
            return true;
        }
        for (final ThrowsSignature t : this.throwsSignatures) {
            if (t.isUnbound()) {
                return true;
            }
        }
        return false;
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

        this.returnType.asReadable(sb);
        sb.append(" (");

        for (int i = 0; i < this.parameters.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            this.parameters.get(i).asReadable(sb);
        }

        sb.append(')');

        if (!this.throwsSignatures.isEmpty()) {
            sb.append(" throws ");

            for (int i = 0; i < this.throwsSignatures.size(); i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                this.throwsSignatures.get(i).asReadable(sb);
            }
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
            for (final TypeParameter typeParam : this.typeParameters) {
                typeParam.asInternal(sb, withBindKey);
            }
            sb.append('>');
        }

        sb.append('(');
        for (final TypeSignature param : this.parameters) {
            param.asInternal(sb, withBindKey);
        }
        sb.append(')');

        this.returnType.asInternal(sb, withBindKey);

        for (final ThrowsSignature typeSig : this.throwsSignatures) {
            sb.append('^');
            typeSig.asInternal(sb, withBindKey);
        }
    }

    /**
     * Return a possibly erased version of this signature as a {@link MethodDescriptor}. This is a lossy process as
     * method and type descriptors cannot represent all components of method or type signatures - all generic type
     * information will be lost.
     *
     * <p>This method will throw {@link IllegalStateException} if any parameters contain an
     * {@link dev.denwav.hypo.types.sig.param.TypeVariable.Unbound unbound type variable}. Use the
     * {@link #bind(TypeVariableBinder)} method in that case to create a version of this signature which has type
     * variables which are properly bound to their parameters.
     *
     * @return A possibly erased version of this signature as a {@link MethodDescriptor}.
     */
    public @NotNull MethodDescriptor asDescriptor() {
        final List<TypeDescriptor> descParams = this.parameters.stream()
            .map(TypeSignature::asDescriptor)
            .collect(Collectors.toList());
        return MethodDescriptor.of(descParams, this.returnType.asDescriptor());
    }

    @Override
    public @NotNull List<? extends TypeParameter> getTypeParameters() {
        return this.typeParameters;
    }

    /**
     * Get the list of parameter types for this method signature. The returned list is immutable.
     *
     * @return The list of parameter types for this method signature.
     */

    @Override
    public @NotNull List<? extends TypeSignature> getParameters() {
        return this.parameters;
    }

    /**
     * Get the return type for this method signature.
     *
     * @return The return type for this method signature.
     */
    @Override
    public @NotNull TypeSignature getReturnType() {
        return this.returnType;
    }

    /**
     * Get the list of throws signatures for this method signature. The returned list is immutable.
     * @return The list of throws signatures for this method signature.
     */
    public @NotNull List<? extends ThrowsSignature> getThrowsSignatures() {
        return this.throwsSignatures;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof final MethodSignature that)) {
            return false;
        }
        return Objects.equals(this.typeParameters, that.typeParameters)
            && Objects.equals(this.parameters, that.parameters)
            && Objects.equals(this.returnType, that.returnType)
            && Objects.equals(this.throwsSignatures, that.throwsSignatures);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.typeParameters, this.parameters, this.returnType, this.throwsSignatures);
    }

    @Override
    public String toString() {
        return this.asReadable();
    }
}
