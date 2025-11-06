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
import dev.denwav.hypo.types.kind.ValueType;
import dev.denwav.hypo.types.intern.Intern;
import dev.denwav.hypo.types.PrimitiveType;
import dev.denwav.hypo.types.TypeRepresentable;
import dev.denwav.hypo.types.VoidType;
import dev.denwav.hypo.types.parsing.JvmTypeParseFailureException;
import dev.denwav.hypo.types.parsing.JvmTypeParser;
import dev.denwav.hypo.types.sig.TypeSignature;
import java.lang.reflect.Method;
import org.jetbrains.annotations.NotNull;

/**
 * A <a href="https://docs.oracle.com/javase/specs/jvms/se23/html/jvms-4.html#jvms-4.3.2">JVM type descriptor</a>.
 *
 * <p>A type descriptor can be one of four different kinds:
 * <ul>
 *     <li>{@link dev.denwav.hypo.types.PrimitiveType PrimitiveType}</li>
 *     <li>{@link ClassTypeDescriptor}</li>
 *     <li>{@link ArrayTypeDescriptor}</li>
 *     <li>{@link dev.denwav.hypo.types.VoidType VoidType}</li>
 * </ul>
 *
 * <p>Type descriptors are used by the JVM to do all type checking and method table lookups. They contain no generic
 * type information.
 *
 * <p>All implementations of this interface must be immutable.
 *
 * @see Descriptor
 * @see ValueType
 * @see MethodDescriptor
 * @see TypeSignature
 */
@Immutable
public sealed interface TypeDescriptor
    extends ValueType, Descriptor, TypeRepresentable
    permits PrimitiveType, VoidType, ArrayTypeDescriptor, ClassTypeDescriptor {

    /**
     * Return the equivalent {@link TypeSignature} which matches this {@link TypeDescriptor}. Since signatures are a
     * super set of descriptors the returned signature is guaranteed to match exactly with this descriptor. That is to
     * say, the following code will evaluate to {@code true}:
     * <pre><code>
     *     TypeDescriptor desc = TypeDescriptor.parse(text);
     *     desc.equals(desc.asSignature().asDescriptor());
     * </code></pre>
     *
     * @return A {@link TypeSignature} which represents the same type as this descriptor.
     */
    @NotNull TypeSignature asSignature();

    /**
     * Parse the given internal JVM type descriptor text into a new {@link TypeDescriptor}.
     *
     * <p>This method throws {@link JvmTypeParseFailureException} if the given text is not a valid type descriptor. Use
     * {@link JvmTypeParser#parseTypeDescriptor(String, int)} if you prefer to have {@code null} be returned instead.
     *
     * @param text The text to parse.
     * @return The {@link TypeDescriptor}.
     * @throws JvmTypeParseFailureException If the given text does not represent a valid JVM type descriptor.
     */
    static @NotNull TypeDescriptor parse(final @NotNull String text) throws JvmTypeParseFailureException {
        return parse(text, 0);
    }

    /**
     * Parse the given internal JVM type descriptor text from the given index into a new {@link TypeDescriptor}.
     *
     * <p>This method throws {@link JvmTypeParseFailureException} if the given text is not a valid type descriptor. Use
     * {@link JvmTypeParser#parseTypeDescriptor(String, int)} if you prefer to have {@code null} be returned instead.
     *
     * @param text The text to parse.
     * @param from The index to start parsing from.
     * @return The {@link TypeDescriptor}.
     * @throws JvmTypeParseFailureException If the given text does not represent a valid JVM type descriptor.
     */
    static @NotNull TypeDescriptor parse(final @NotNull String text, final int from) throws JvmTypeParseFailureException {
        if (text.length() > 1 && from == 0) {
            final TypeDescriptor r = Intern.tryFind(ClassTypeDescriptor.class, text);
            if (r != null) {
                return r;
            }
        }
        final TypeDescriptor result = JvmTypeParser.parseTypeDescriptor(text, from);
        if (result == null) {
            throw new JvmTypeParseFailureException("text is not a valid type descriptor: " + text.substring(from));
        }
        return result;
    }

    /**
     * Create a new {@link TypeDescriptor} from the given {@link Class}.
     *
     * @param clazz The {@link Class} to create a {@link TypeDescriptor} from.
     * @return A new {@link TypeDescriptor} from the given {@link Class}.
     * @see MethodDescriptor#of(Method)
     */
    static @NotNull TypeDescriptor of(final @NotNull Class<?> clazz) {
        if (clazz == void.class) {
            return VoidType.INSTANCE;
        } else if (clazz == boolean.class) {
            return PrimitiveType.BOOLEAN;
        } else if (clazz == char.class) {
            return PrimitiveType.CHAR;
        } else if (clazz == byte.class) {
            return PrimitiveType.BYTE;
        } else if (clazz == short.class) {
            return PrimitiveType.SHORT;
        } else if (clazz == int.class) {
            return PrimitiveType.INT;
        } else if (clazz == long.class) {
            return PrimitiveType.LONG;
        } else if (clazz == float.class) {
            return PrimitiveType.FLOAT;
        } else if (clazz == double.class) {
            return PrimitiveType.DOUBLE;
        } else if (clazz.isArray()) {
            Class<?> baseType = clazz;
            int dim = 0;
            while (true) {
                final Class<?> next = baseType.getComponentType();
                if (next == null) {
                    break;
                }
                dim++;
                baseType = next;
            }
            return ArrayTypeDescriptor.of(dim, TypeDescriptor.of(baseType));
        } else {
            return ClassTypeDescriptor.of(clazz.getName());
        }
    }
}
