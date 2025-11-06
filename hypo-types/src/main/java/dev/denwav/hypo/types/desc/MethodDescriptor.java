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

package dev.denwav.hypo.types.desc;

import com.google.errorprone.annotations.Immutable;
import dev.denwav.hypo.types.kind.MethodType;
import dev.denwav.hypo.types.intern.Intern;
import dev.denwav.hypo.types.TypeRepresentable;
import dev.denwav.hypo.types.parsing.JvmTypeParseFailureException;
import dev.denwav.hypo.types.parsing.JvmTypeParser;
import dev.denwav.hypo.types.sig.MethodSignature;
import dev.denwav.hypo.types.sig.TypeSignature;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

/**
 * A <a href="https://docs.oracle.com/javase/specs/jvms/se23/html/jvms-4.html#jvms-4.3.3">JVM method descriptor</a>.
 *
 * <p>Method descriptors consist of 2 parts, a {@link #getParameters() parameter list} and a
 * {@link #getReturnType() return type}.
 *
 * <p>Method descriptors are used by the JVM to wire together method calls and do method call lookups. Method
 * descriptors do not contain any generic type information.
 *
 * <p>This class is immutable.
 *
 * @see Descriptor
 * @see TypeDescriptor
 * @see MethodSignature
 */
@Immutable
public final class MethodDescriptor
    extends Intern<MethodDescriptor>
    implements MethodType, Descriptor, TypeRepresentable {

    @SuppressWarnings("Immutable")
    private final @NotNull List<? extends TypeDescriptor> parameters;
    private final @NotNull TypeDescriptor returnType;

    /**
     * Create a {@link MethodDescriptor} instance.
     *
     * @param parameters The parameter types for the new method descriptor.
     * @param returnType The return type for the new method descriptor.
     * @return The new {@link MethodDescriptor}.
     */
    public static @NotNull MethodDescriptor of(
        final @NotNull List<? extends TypeDescriptor> parameters,
        final @NotNull TypeDescriptor returnType
    ) {
        return new MethodDescriptor(parameters, returnType).intern();
    }

    /**
     * Parse the given internal JVM method descriptor text into a new {@link MethodDescriptor}.
     *
     * <p>This method throws {@link JvmTypeParseFailureException} if the given text is not a valid method descriptor.
     * Use {@link JvmTypeParser#parseMethodDescriptor(String, int)} if you prefer to have {@code null} be returned
     * instead.
     *
     * @param text The text to parse.
     * @return The {@link MethodDescriptor}.
     * @throws JvmTypeParseFailureException If the given text does not represent a valid JVM method descriptor.
     */
    public static @NotNull MethodDescriptor parse(final @NotNull String text) throws JvmTypeParseFailureException {
        return parse(text, 0);
    }

    /**
     * Parse the given internal JVM method descriptor text into a new {@link MethodDescriptor}.
     *
     * <p>This method throws {@link JvmTypeParseFailureException} if the given text is not a valid method descriptor.
     * Use {@link JvmTypeParser#parseMethodDescriptor(String, int)} if you prefer to have {@code null} be returned
     * instead.
     *
     * @param text The text to parse.
     * @param from The index to start parsing from.
     * @return The {@link MethodDescriptor}.
     * @throws JvmTypeParseFailureException If the given text does not represent a valid JVM method descriptor.
     */
    public static @NotNull MethodDescriptor parse(
        final @NotNull String text,
        final int from
    ) throws JvmTypeParseFailureException {
        if (text.length() > 1 && from == 0) {
            final MethodDescriptor r = Intern.tryFind(MethodDescriptor.class, text);
            if (r != null) {
                return r;
            }
        }
        final MethodDescriptor result = JvmTypeParser.parseMethodDescriptor(text, from);
        if (result == null) {
            throw new JvmTypeParseFailureException("text is not a valid method descriptor: " + text.substring(from));
        }
        return result;
    }

    /**
     * Create a new {@link MethodDescriptor} matching the given {@link Method}.
     *
     * @param method The {@link Method} to create a descriptor from.
     * @return A new {@link MethodDescriptor} matching the given {@link Method}.
     *
     * @see TypeDescriptor#of(Class)
     */
    public static @NotNull MethodDescriptor of(final @NotNull Method method) {
        final int len = method.getParameterCount();
        final Class<?>[] methodParams = method.getParameterTypes();
        final ArrayList<TypeDescriptor> params = new ArrayList<>(len);
        for (int i = 0; i < len; i++) {
            params.add(TypeDescriptor.of(methodParams[i]));
        }
        return MethodDescriptor.of(params, TypeDescriptor.of(method.getReturnType()));
    }

    private MethodDescriptor(
        final @NotNull List<? extends TypeDescriptor> parameters,
        final @NotNull TypeDescriptor returnType
    ) {
        this.parameters = List.copyOf(parameters);
        this.returnType = returnType;
    }

    @Override
    public void asReadable(final @NotNull StringBuilder sb) {
        this.returnType.asReadable(sb);
        sb.append(" (");
        for (int i = 0; i < this.parameters.size(); i++) {
            this.parameters.get(i).asReadable(sb);
            if (i < this.parameters.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append(')');
    }

    @Override
    public void asInternal(final @NotNull StringBuilder sb) {
        sb.append('(');
        for (final TypeDescriptor param : this.parameters) {
            param.asInternal(sb);
        }
        sb.append(')');
        this.returnType.asInternal(sb);
    }

    /**
     * Return this descriptor as a valid {@link MethodSignature}. Signatures are a super set of descriptors, so the
     * result of this method is guaranteed to maintain all type information. That is to say, the following code will
     * evaluate to {@code true}:
     * <pre><code>
     *     MethodDescriptor desc = MethodDescriptor.parse(text);
     *     desc.equals(desc.asSignature().asDescriptor());
     * </code></pre>
     *
     * @return A {@link MethodSignature} which represents the same type as this descriptor.
     */
    public @NotNull MethodSignature asSignature() {
        final List<TypeSignature> sigParams = this.parameters.stream()
            .map(TypeDescriptor::asSignature)
            .collect(Collectors.toList());
        return MethodSignature.of(
            Collections.emptyList(),
            sigParams,
            this.returnType.asSignature(),
            Collections.emptyList()
        );
    }

    /**
     * Get the list of parameter types for this method descriptor. The returned list is immutable.
     *
     * @return The list of parameter types for this method descriptor.
     */
    @Override
    public @NotNull List<? extends TypeDescriptor> getParameters() {
        return this.parameters;
    }

    /**
     * Get the return type for this method descriptor.
     *
     * @return The return type for this method descriptor.
     */
    @Override
    public @NotNull TypeDescriptor getReturnType() {
        return this.returnType;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof final MethodDescriptor that)) {
            return false;
        }
        return Objects.equals(this.parameters, that.parameters)
            && Objects.equals(this.returnType, that.returnType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.parameters, this.returnType);
    }

    @Override
    public String toString() {
        return this.asReadable();
    }
}
