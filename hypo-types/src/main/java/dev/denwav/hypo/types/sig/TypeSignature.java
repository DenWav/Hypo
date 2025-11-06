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
import dev.denwav.hypo.types.kind.ValueType;
import dev.denwav.hypo.types.intern.Intern;
import dev.denwav.hypo.types.PrimitiveType;
import dev.denwav.hypo.types.TypeBindable;
import dev.denwav.hypo.types.TypeRepresentable;
import dev.denwav.hypo.types.TypeVariableBinder;
import dev.denwav.hypo.types.VoidType;
import dev.denwav.hypo.types.desc.TypeDescriptor;
import dev.denwav.hypo.types.parsing.JvmTypeParseFailureException;
import dev.denwav.hypo.types.parsing.JvmTypeParser;
import dev.denwav.hypo.types.sig.param.BoundedTypeArgument;
import dev.denwav.hypo.types.sig.param.TypeVariable;
import dev.denwav.hypo.types.sig.param.WildcardArgument;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A <a href="https://docs.oracle.com/javase/specs/jvms/se23/html/jvms-4.html#jvms-4.7.9.1">JVM type signature</a>.
 *
 * <p>A type signature can be one of four different kinds:
 * <ul>
 *     <li>{@link dev.denwav.hypo.types.PrimitiveType PrimitiveType}</li>
 *     <li>{@link ClassTypeSignature}</li>
 *     <li>{@link ArrayTypeSignature}</li>
 *     <li>{@link dev.denwav.hypo.types.VoidType VoidType}</li>
 * </ul>
 *
 * <p>Type signatures are used by the Java compiler to enforce generic type checks at compile time. They are not used
 * during runtime on the JVM, though they are present to allow for compiling against and debugging generic code.
 *
 * <p>All implementations of this interface must be immutable.
 *
 * @see MethodSignature
 * @see TypeDescriptor
 */
@Immutable
public sealed interface TypeSignature
    extends ValueType, Signature, TypeBindable, TypeRepresentable
    permits PrimitiveType, VoidType, ReferenceTypeSignature {

    /**
     * Return a possibly erased version of this signature as a {@link TypeDescriptor}. This is a lossy process as type
     * descriptors cannot represent all components of type signatures - all generic type information will be lost.
     *
     * <p>This method will throw {@link IllegalStateException} if it contains an
     * {@link dev.denwav.hypo.types.sig.param.TypeVariable.Unbound unbound type variable}. Use the
     * {@link #bind(TypeVariableBinder)} method in that case to create a version of this signature which has type
     * variables which are properly bound to their parameters.
     *
     * @return A possibly erased version of this signature as a {@link TypeDescriptor}.
     */
    @NotNull TypeDescriptor asDescriptor();

    /**
     * Parse the given internal JVM type signature text into a new {@link TypeSignature}.
     *
     * <p>This method throws {@link JvmTypeParseFailureException} if the given text is not a valid type signature. Use
     * {@link JvmTypeParser#parseTypeSignature(String, int)} if you prefer to have {@code null} be returned instead.
     *
     * @param text The text to parse.
     * @return The {@link TypeSignature}.
     * @throws JvmTypeParseFailureException If the given text does not represent a valid JVM type signature.
     */
    static @NotNull TypeSignature parse(final @NotNull String text) throws JvmTypeParseFailureException {
        return parse(text, 0);
    }

    /**
     * Parse the given internal JVM type signature text into a new {@link TypeSignature}.
     *
     * <p>This method throws {@link JvmTypeParseFailureException} if the given text is not a valid type signature. Use
     * {@link JvmTypeParser#parseTypeSignature(String, int)} if you prefer to have {@code null} be returned instead.
     *
     * @param text The text to parse.
     * @param from The index to start parsing from.
     * @return The {@link TypeSignature}.
     * @throws JvmTypeParseFailureException If the given text does not represent a valid JVM type signature.
     */
    static @NotNull TypeSignature parse(final @NotNull String text, final int from) throws JvmTypeParseFailureException {
        if (text.length() > 1 && from == 0) {
            final TypeSignature r = Intern.tryFind(ClassTypeSignature.class, text);
            if (r != null) {
                return r;
            }
        }
        final TypeSignature result = JvmTypeParser.parseTypeSignature(text, from);
        if (result == null) {
            throw new JvmTypeParseFailureException("text is not a valid type signature: " + text.substring(from));
        }
        return result;
    }

    /**
     * Create a {@link TypeSignature} matching the given {@link Class}. Note the {@link Class} does not include type
     * parameter information so this will only
     *
     * @param clazz The {@link Class}.
     * @return A new {@link TypeSignature} matching the given {@link Class}.
     */
    static @NotNull TypeSignature of(final @NotNull Class<?> clazz) {
        final java.lang.reflect.TypeVariable<? extends Class<?>>[] params = clazz.getTypeParameters();
        if (params.length == 0) {
            return TypeDescriptor.of(clazz).asSignature();
        } else {
            return ClassTypeSignature.of(clazz.getName(), Arrays.stream(params).map(TypeVariable::of).toList());
        }
    }

    /**
     * Attempt to create a new {@link TypeSignature} from the given {@link Type}. {@link TypeSignature} in our model
     * does not cover every possible thing {@link Type} can represent, so this may fail. Notable in particular, since
     * {@link WildcardArgument} and {@link BoundedTypeArgument} are type arguments (which are not type signatures), then
     * if {@link Type} implements {@link WildcardType} this method will fail.
     *
     * <p>If you want this method to simply return {@code null} instead of throwing an exception, use
     * {@link #ofOrNull(Type)}.
     *
     * <p>If you want full flexibility to support as many possible values of {@link Type} as possible, use
     * {@link TypeRepresentable#of(Type)}.
     *
     * @param type The type.
     * @return The new {@link TypeSignature}.
     * @see TypeSignature#ofOrNull(Type)
     * @see TypeRepresentable#of(Type)
     */
    static @NotNull TypeSignature of(final @NotNull Type type) {
        return switch (type) {
            case final Class<?> clazz -> of(clazz);
            case final ParameterizedType parameterizedType -> ClassTypeSignature.of(parameterizedType);
            case final GenericArrayType arrayType -> ArrayTypeSignature.of(arrayType);
            case final java.lang.reflect.TypeVariable<?> typeVar -> TypeVariable.of(typeVar);
            case final WildcardType wildcardType ->
                throw new IllegalArgumentException("Unsupported type: " + wildcardType.getClass().getName() +
                    " (WildcardType is a TypeArgument, not a TypeSignature)");
            default -> throw new IllegalArgumentException("Unsupported type: " + type.getClass().getName());
        };
    }

    /**
     * Attempt to create a new {@link TypeSignature} from the given {@link Type}. {@link TypeSignature} in our model
     * does not cover every possible thing {@link Type} can represent, so this may fail. Notable in particular, since
     * {@link WildcardArgument} and {@link BoundedTypeArgument} are type arguments (which are not type signatures), then
     * if {@link Type} implements {@link WildcardType} this method will fail.
     *
     * <p>If you want this method to throw an exception instead of simply returning {@code null}, use {@link #of(Type)}.
     *
     * <p>If you want full flexibility to support as many possible values of {@link Type} as possible, use
     * {@link TypeRepresentable#of(Type)}.
     *
     * @param type The type.
     * @return The new {@link TypeSignature}.
     * @see TypeSignature#of(Type)
     * @see TypeRepresentable#of(Type)
     */
    static @Nullable TypeSignature ofOrNull(final @NotNull Type type) {
        try {
            return of(type);
        } catch (final IllegalArgumentException ignored) {
            return null;
        }
    }

    @Override
    @NotNull TypeSignature bind(final @NotNull TypeVariableBinder binder);
}
