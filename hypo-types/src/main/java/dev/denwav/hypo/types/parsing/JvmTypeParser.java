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

package dev.denwav.hypo.types.parsing;

import dev.denwav.hypo.types.PrimitiveType;
import dev.denwav.hypo.types.VoidType;
import dev.denwav.hypo.types.desc.ArrayTypeDescriptor;
import dev.denwav.hypo.types.desc.ClassTypeDescriptor;
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
import dev.denwav.hypo.types.sig.param.WildcardArgument;
import dev.denwav.hypo.types.sig.param.WildcardBound;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Simple parser for internal JVM type names, descriptors, and signatures.
 */
public final class JvmTypeParser {

    private JvmTypeParser() {
    }

    // Public API

    /**
     * Parse the given text, starting at the given index, into a new {@link TypeDescriptor}.
     *
     * @param text The text to parse.
     * @param from The index to start parsing from.
     * @return The {@link TypeDescriptor}, or {@code null} if the text is not a valid type descriptor.
     */
    public static @Nullable TypeDescriptor parseTypeDescriptor(
        final @NotNull String text,
        final int from
    ) throws JvmTypeParseFailureException {
        final ParserState source = new ParserState(text, from);
        if (consumeTypeDesc(source)) {
            return source.getLastResult();
        }
        return null;
    }

    /**
     * Parse the given text, starting at the given index, into a new {@link MethodDescriptor}.
     *
     * @param text The text to parse.
     * @param from The index to start parsing from.
     * @return The {@link MethodDescriptor}, or {@code null} if the text is not a valid method descriptor.
     */
    public static @Nullable MethodDescriptor parseMethodDescriptor(
        final @NotNull String text,
        final int from
    ) {
        final ParserState source = new ParserState(text, from);
        if (consumeMethodDesc(source)) {
            return source.getLastResult();
        }
        return null;
    }

    /**
     * Parse the given text, starting at the given index, into a new {@link TypeSignature}.
     *
     * @param text The text to parse.
     * @param from The index to start parsing from.
     * @return The {@link TypeSignature}, or {@code null} if the text is not a valid type signature.
     */
    public static @Nullable TypeSignature parseTypeSignature(
        final @NotNull String text,
        final int from
    ) throws JvmTypeParseFailureException {
        final ParserState source = new ParserState(text, from);
        if (consumeTypeSig(source)) {
            return source.getLastResult();
        }
        return null;
    }

    /**
     * Parse the given text, starting at the given index, into a new {@link MethodSignature}.
     *
     * @param text The text to parse.
     * @param from The index to start parsing from.
     * @return The {@link MethodSignature}.
     * @throws JvmTypeParseFailureException If the given text does not represent a valid JVM method signature.
     */
    public static @Nullable MethodSignature parseMethodSignature(
        final @NotNull String text,
        final int from
    ) throws JvmTypeParseFailureException {
        final ParserState source = new ParserState(text, from);
        if (consumeMethodSignature(source)) {
            return source.getLastResult();
        }
        return null;
    }

    /**
     * Parse the given text, starting at the given index, into a new {@link ClassSignature}.
     *
     * @param text The text to parse.
     * @param from The index to start parsing from.
     * @return The {@link ClassSignature}.
     * @throws JvmTypeParseFailureException If the given text does not represent a valid JVM class signature.
     */
    public static @Nullable ClassSignature parseClassSignature(
        final @NotNull String text,
        final int from
    ) throws JvmTypeParseFailureException {
        final ParserState source = new ParserState(text, from);
        if (consumeClassSig(source)) {
            return source.getLastResult();
        }
        return null;
    }

    // Type Descriptor

    private static boolean consumeTypeDesc(final @NotNull ParserState source) {
        if (source.isAtEnd()) {
            return false;
        }

        return consumePrimitive(source)
            || consumeVoid(source)
            || consumeArrayDesc(source)
            || consumeClassTypeDesc(source);
    }

    private static boolean consumePrimitive(final @NotNull ParserState source) {
        final PrimitiveType type = PrimitiveType.fromChar(source.current());
        if (type != null) {
            source.advance();
            return source.setLastResult(type);
        }
        return false;
    }

    private static boolean consumeVoid(final @NotNull ParserState source) {
        if (source.current() == 'V') {
            source.advance();
            return source.setLastResult(VoidType.INSTANCE);
        }
        return false;
    }

    private static boolean consumeArrayDesc(final @NotNull ParserState source) {
        if (source.current() != '[') {
            return false;
        }

        int dim = 1;
        while (source.advance() == '[') {
            dim++;
        }

        if (source.isAtEnd()) {
            return false;
        }
        if (!consumeTypeDesc(source)) {
            return false;
        }

        final TypeDescriptor baseType = source.getLastResult();
        return source.setLastResult(ArrayTypeDescriptor.of(dim, baseType));
    }

    private static boolean consumeClassTypeDesc(final @NotNull ParserState source) {
        if (source.current() != 'L') {
            return false;
        }

        source.advanceAndMark();

        while (!source.isAtEnd()) {
            if (source.advance() == ';') {
                break;
            }
        }
        if (source.isAtEnd()) {
            return false;
        }

        final String className = source.substringFromMark();
        return source.advanceIfSet(ClassTypeDescriptor.of(className));
    }

    private static boolean consumeMethodDesc(final @NotNull ParserState source) {
        if (source.current() != '(') {
            return false;
        }

        source.advance();

        final ArrayList<TypeDescriptor> paramTypes = new ArrayList<>();
        while (consumeTypeDesc(source)) {
            paramTypes.add(source.getLastResult());
        }

        if (source.current() != ')') {
            return false;
        }

        source.advance();

        if (!consumeTypeDesc(source)) {
            return false;
        }

        final TypeDescriptor returnType = source.getLastResult();
        return source.setLastResult(MethodDescriptor.of(paramTypes, returnType));
    }

    // Type Signature

    private static boolean consumeTypeSig(final @NotNull ParserState source) {
        if (source.isAtEnd()) {
            return false;
        }

        return consumePrimitive(source)
            || consumeVoid(source)
            || consumeArraySig(source)
            || consumeClassTypeSig(source)
            || consumeTypeVariable(source);
    }

    private static boolean consumeArraySig(final @NotNull ParserState source) {
        if (source.current() != '[') {
            return false;
        }

        int dim = 1;
        while (source.advance() == '[') {
            dim++;
        }

        if (source.isAtEnd()) {
            return false;
        }
        if (!consumeTypeSig(source)) {
            return false;
        }

        final TypeSignature baseType = source.getLastResult();
        return source.setLastResult(ArrayTypeSignature.of(dim, baseType));
    }

    @SuppressWarnings("DuplicatedCode")
    private static boolean consumeClassTypeSig(final @NotNull ParserState source) {
        if (source.current() != 'L') {
            return false;
        }

        source.advanceAndMark();

        while (!source.isAtEnd()) {
            final char c = source.advance();
            if (c == ';' || c == '<' || c == '.') {
                break;
            }
        }
        if (source.isAtEnd()) {
            return false;
        }

        final String className = source.substringFromMark();

        final List<TypeArgument> typeArgs;
        if (consumeTypeArguments(source)) {
            typeArgs = source.getLastResult();
        } else {
            typeArgs = Collections.emptyList();
        }
        ClassTypeSignature currentClassType = ClassTypeSignature.of(className, typeArgs);

        while (consumeSubclass(source, currentClassType)) {
            currentClassType = source.getLastResult();
        }

        if (source.current() == ';') {
            source.advance();
            return source.setLastResult(currentClassType);
        }

        return false;
    }

    @SuppressWarnings("DuplicatedCode")
    private static boolean consumeSubclass(
        final @NotNull ParserState source,
        final @NotNull ClassTypeSignature parentClass
    ) {
        if (source.current() != '.') {
            return false;
        }

        source.advanceAndMark();

        while (!source.isAtEnd()) {
            final char c = source.advance();
            if (c == ';' || c == '<' || c == '.') {
                break;
            }
        }
        if (source.isAtEnd()) {
            return false;
        }

        final String className = source.substringFromMark();
        final List<TypeArgument> typeArgs;
        if (consumeTypeArguments(source)) {
            typeArgs = source.getLastResult();
        } else {
            typeArgs = Collections.emptyList();
        }

        final char c = source.current();
        if (c == ';' || c == '.') {
            return source.setLastResult(ClassTypeSignature.of(parentClass, className, typeArgs));
        }

        return false;
    }

    private static boolean consumeTypeArguments(final @NotNull ParserState source) {
        if (source.current() != '<') {
            return false;
        }

        source.advanceAndMark();

        final ArrayList<TypeArgument> typeArgs = new ArrayList<>();
        while (consumeTypeArgument(source)) {
            typeArgs.add(source.getLastResult());
        }

        if (source.current() != '>') {
            return false;
        }

        source.advance();
        return source.setLastResult(typeArgs);
    }

    private static boolean consumeTypeArgument(final @NotNull ParserState source) {
        return consumeWildcardTypeArgument(source) || consumeBoundedTypeArgument(source);
    }

    private static boolean consumeWildcardTypeArgument(final @NotNull ParserState source) {
        if (source.current() == '*') {
            source.advance();
            return source.setLastResult(WildcardArgument.INSTANCE);
        }
        return false;
    }

    private static boolean consumeBoundedTypeArgument(final @NotNull ParserState source) {
        final char c = source.current();
        final WildcardBound bound;
        if (c == '+') {
            bound = WildcardBound.UPPER;
            source.advance();
        } else if (c == '-') {
            bound = WildcardBound.LOWER;
            source.advance();
        } else {
            bound = null;
        }

        if (consumeTypeSig(source)) {
            final ReferenceTypeSignature boundedType = source.getLastResult();
            if (bound == null) {
                return source.setLastResult(boundedType);
            } else {
                return source.setLastResult(BoundedTypeArgument.of(bound, boundedType));
            }
        }
        return false;
    }

    private static boolean consumeTypeVariable(final @NotNull ParserState source) {
        if (source.current() != 'T') {
            return false;
        }

        source.advanceAndMark();

        while (!source.isAtEnd()) {
            if (source.advance() == ';') {
                break;
            }
        }
        if (source.isAtEnd()) {
            return false;
        }

        final String typeName = source.substringFromMark();
        source.advance();
        return source.setLastResult(TypeVariable.unbound(typeName));
    }

    private static boolean consumeMethodSignature(final @NotNull ParserState source) {
        List<TypeParameter> typeParams;
        if (consumeTypeParameters(source)) {
            typeParams = source.getLastResult();
        } else {
            typeParams = Collections.emptyList();
        }

        if (source.current() != '(') {
            return false;
        }

        source.advance();

        final ArrayList<TypeSignature> methodParameters = new ArrayList<>();
        while (consumeTypeSig(source)) {
            methodParameters.add(source.getLastResult());
        }
        if (source.isAtEnd() || source.current() != ')') {
            return false;
        }

        source.advance();

        if (!consumeTypeSig(source)) {
            return false;
        }
        final TypeSignature returnType = source.getLastResult();

        final ArrayList<ThrowsSignature> throwsSignatures = new ArrayList<>();
        while (consumeThrowsSignature(source)) {
            throwsSignatures.add(source.getLastResult());
        }

        return source.setLastResult(MethodSignature.of(typeParams, methodParameters, returnType, throwsSignatures));
    }

    private static boolean consumeTypeParameters(final @NotNull ParserState source) {
        if (source.current() != '<') {
            return false;
        }

        source.advance();

        final ArrayList<TypeParameter> typeParams = new ArrayList<>();
        while (consumeTypeParameter(source)) {
            final TypeParameter typeParam = source.getLastResult();
            typeParams.add(typeParam);
        }
        if (source.isAtEnd() || source.current() != '>') {
            return false;
        }

        source.advance();
        return source.setLastResult(typeParams);
    }

    private static boolean consumeTypeParameter(final @NotNull ParserState source) {
        if (source.current() == '>') {
            return false;
        }

        source.mark();

        while (!source.isAtEnd()) {
            if (source.advance() == ':') {
                break;
            }
        }
        if (source.isAtEnd()) {
            return false;
        }

        final String paramName = source.substringFromMark();

        if (!consumeTypeParamBounds(source)) {
            return false;
        }
        final ReferenceTypeSignature classBound = source.getLastResultOrNull();

        final ArrayList<ReferenceTypeSignature> interfaceBounds = new ArrayList<>();
        while (consumeTypeParamBounds(source)) {
            interfaceBounds.add(source.getLastResult());
        }
        if (source.isAtEnd()) {
            return false;
        }

        return source.setLastResult(TypeParameter.of(paramName, classBound, interfaceBounds));
    }

    private static boolean consumeTypeParamBounds(final @NotNull ParserState source) {
        if (source.current() != ':') {
            return false;
        }

        source.advance();

        char next = source.current();
        if (next == ':' || next == '>') {
            return true;
        }

        return consumeTypeSig(source);
    }

    private static boolean consumeThrowsSignature(final @NotNull ParserState source) {
        if (source.current() != '^') {
            return false;
        }

        source.advance();
        return consumeTypeSig(source);
    }

    private static boolean consumeClassSig(final @NotNull ParserState source) {
        consumeTypeParameters(source);
        List<TypeParameter> typeParams = source.getLastResultOrNull();
        if (typeParams == null) {
            typeParams = Collections.emptyList();
        }

        if (!consumeClassTypeSig(source)) {
            return false;
        }

        final ClassTypeSignature superClass = source.getLastResult();

        final List<ClassTypeSignature> superInterfaces = new ArrayList<>();
        while (consumeClassTypeSig(source)) {
            superInterfaces.add(source.getLastResult());
        }

        return source.setLastResult(ClassSignature.of(typeParams, superClass, superInterfaces));
    }
}
