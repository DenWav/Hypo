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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package dev.denwav.hypo.types;

import dev.denwav.hypo.types.desc.ArrayTypeDescriptor;
import dev.denwav.hypo.types.desc.ClassTypeDescriptor;
import dev.denwav.hypo.types.desc.MethodDescriptor;
import dev.denwav.hypo.types.parsing.JvmTypeParseFailureException;
import dev.denwav.hypo.types.parsing.JvmTypeParser;
import dev.denwav.hypo.types.sig.ClassSignature;
import dev.denwav.hypo.types.sig.ClassTypeSignature;
import dev.denwav.hypo.types.sig.MethodSignature;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("[types] JvmTypeParser Tests")
class JvmTypeParserTest {

    // -------------------------------------------------------------------------
    // TypeDescriptor
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Type Descriptor")
    class TypeDescriptorTests {

        static Stream<String> invalidInputs() {
            return Stream.of(
                "",            // empty
                "[",           // array with no element type
                "[[",          // nested array with no element type
                "L",           // class ref started but no name
                "Ljava/lang/String", // class ref with no terminating semicolon
                "X"            // unknown first character
            );
        }

        @ParameterizedTest(name = "parseTypeDescriptor(\"{0}\") == null")
        @MethodSource("invalidInputs")
        @DisplayName("parseTypeDescriptor returns null for malformed type descriptor strings")
        void invalidInputs(final String input) {
            assertNull(JvmTypeParser.parseTypeDescriptor(input, 0),
                "Expected null for malformed type descriptor: '" + input + "'");
        }

        @Test
        @DisplayName("parseTypeDescriptor respects a non-zero from offset")
        void fromOffset() {
            final String prefixed = "###Ljava/lang/Object;";
            final var result = JvmTypeParser.parseTypeDescriptor(prefixed, 3);
            assertNotNull(result, "Expected non-null result when parsing from offset 3");
            assertEquals(ClassTypeDescriptor.of("java/lang/Object"), result,
                "Parsing from offset 3 should yield ClassTypeDescriptor for java/lang/Object");
        }

        @Test
        @DisplayName("parseTypeDescriptor respects a non-zero from offset for array types")
        void fromOffsetArray() {
            final String prefixed = "XXX[I";
            final var result = JvmTypeParser.parseTypeDescriptor(prefixed, 3);
            assertNotNull(result, "Expected non-null result when parsing '[I' from offset 3");
            assertEquals(ArrayTypeDescriptor.of(1, PrimitiveType.INT), result,
                "Parsing from offset 3 should yield ArrayTypeDescriptor for [I");
        }
    }

    // -------------------------------------------------------------------------
    // MethodDescriptor
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Method Descriptor")
    class MethodDescriptorTests {

        static Stream<String> invalidInputs() {
            return Stream.of(
                "",            // empty
                "II",          // no opening parenthesis
                "(II",         // no closing parenthesis
                "(II)"         // no return type after closing parenthesis
            );
        }

        @ParameterizedTest(name = "parseMethodDescriptor(\"{0}\") == null")
        @MethodSource("invalidInputs")
        @DisplayName("parseMethodDescriptor returns null for malformed method descriptor strings")
        void invalidInputs(final String input) {
            assertNull(JvmTypeParser.parseMethodDescriptor(input, 0),
                "Expected null for malformed method descriptor: '" + input + "'");
        }

        @Test
        @DisplayName("parseMethodDescriptor respects a non-zero from offset")
        void fromOffset() {
            final String prefixed = "XXX(I)V";
            final var result = JvmTypeParser.parseMethodDescriptor(prefixed, 3);
            assertNotNull(result, "Expected non-null result when parsing '(I)V' from offset 3");
            assertEquals(MethodDescriptor.parse("(I)V"), result,
                "Parsing from offset 3 should yield MethodDescriptor for (I)V");
        }
    }

    // -------------------------------------------------------------------------
    // TypeSignature
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Type Signature")
    class TypeSignatureTests {

        static Stream<String> invalidInputs() {
            return Stream.of(
                "",                       // empty
                "TT",                     // type variable with no terminating semicolon
                "Ljava/util/List<",       // unclosed generic type argument
                "<"                       // stray generic opener
            );
        }

        @ParameterizedTest(name = "parseTypeSignature(\"{0}\") returns null or throws")
        @MethodSource("invalidInputs")
        @DisplayName("parseTypeSignature returns null or throws JvmTypeParseFailureException for malformed type signature strings")
        void invalidInputs(final String input) {
            try {
                assertNull(JvmTypeParser.parseTypeSignature(input, 0),
                    "Expected null for malformed type signature: '" + input + "'");
            } catch (final JvmTypeParseFailureException e) {
                // also acceptable
            }
        }

        @Test
        @DisplayName("parseTypeSignature respects a non-zero from offset")
        void fromOffset() throws JvmTypeParseFailureException {
            final String prefixedClass = "XXXLjava/lang/String;";
            final var result = JvmTypeParser.parseTypeSignature(prefixedClass, 3);
            assertNotNull(result, "Expected non-null result when parsing 'Ljava/lang/String;' from offset 3");
            assertEquals(ClassTypeSignature.of("java/lang/String"), result,
                "Parsing from offset 3 should yield ClassTypeSignature for java/lang/String");
        }
    }

    // -------------------------------------------------------------------------
    // MethodSignature
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Method Signature")
    class MethodSignatureTests {

        static Stream<String> invalidInputs() {
            return Stream.of(
                "",  // empty
                "<T",        // unclosed type parameter list
                "()"         // no return type after closing parenthesis
            );
        }

        @ParameterizedTest(name = "parseMethodSignature(\"{0}\") returns null or throws")
        @MethodSource("invalidInputs")
        @DisplayName("parseMethodSignature returns null or throws JvmTypeParseFailureException for malformed method signature strings")
        void invalidInputs(final String input) {
            try {
                assertNull(JvmTypeParser.parseMethodSignature(input, 0),
                    "Expected null for malformed method signature: '" + input + "'");
            } catch (final JvmTypeParseFailureException e) {
                // also acceptable
            }
        }

        @Test
        @DisplayName("parseMethodSignature respects a non-zero from offset")
        void fromOffset() throws JvmTypeParseFailureException {
            final String prefixed = "XXX()V";
            final var result = JvmTypeParser.parseMethodSignature(prefixed, 3);
            assertNotNull(result, "Expected non-null result when parsing '()V' from offset 3");
            assertEquals(MethodSignature.parse("()V"), result,
                "Parsing from offset 3 should yield MethodSignature for ()V");
        }
    }

    // -------------------------------------------------------------------------
    // ClassSignature
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Class Signature")
    class ClassSignatureTests {

        static Stream<String> invalidInputs() {
            return Stream.of(
                "",    // empty
                "<",   // stray generic opener
                "<T"   // unclosed type parameter list
            );
        }

        @ParameterizedTest(name = "parseClassSignature(\"{0}\") returns null or throws")
        @MethodSource("invalidInputs")
        @DisplayName("parseClassSignature returns null or throws JvmTypeParseFailureException for malformed class signature strings")
        void invalidInputs(final String input) {
            try {
                assertNull(JvmTypeParser.parseClassSignature(input, 0),
                    "Expected null for malformed class signature: '" + input + "'");
            } catch (final JvmTypeParseFailureException e) {
                // also acceptable
            }
        }

        @Test
        @DisplayName("parseClassSignature respects a non-zero from offset")
        void fromOffset() throws JvmTypeParseFailureException {
            final String valid = "Ljava/lang/Object;";
            final String prefixed = "XXX" + valid;
            final var result = JvmTypeParser.parseClassSignature(prefixed, 3);
            assertNotNull(result, "Expected non-null result when parsing a class signature from offset 3");
            assertEquals(ClassSignature.parse(valid), result,
                "Parsing from offset 3 should yield the same ClassSignature as parsing the bare string");
        }
    }
}
