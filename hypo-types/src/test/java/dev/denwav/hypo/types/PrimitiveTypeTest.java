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

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("[types] PrimitiveType Tests")
class PrimitiveTypeTest {

    static Stream<Arguments> validChars() {
        return Stream.of(
            Arguments.of('B', PrimitiveType.BYTE),
            Arguments.of('C', PrimitiveType.CHAR),
            Arguments.of('D', PrimitiveType.DOUBLE),
            Arguments.of('F', PrimitiveType.FLOAT),
            Arguments.of('I', PrimitiveType.INT),
            Arguments.of('J', PrimitiveType.LONG),
            Arguments.of('S', PrimitiveType.SHORT),
            Arguments.of('Z', PrimitiveType.BOOLEAN)
        );
    }

    static Stream<Character> invalidChars() {
        return Stream.of('V', 'A', 'X', 'L', '0', '[', ' ', '\0');
    }

    @ParameterizedTest(name = "fromChar(''{0}'') == {1}")
    @MethodSource("validChars")
    @DisplayName("fromChar returns the correct PrimitiveType for every valid JVM primitive character")
    void validFromChar(char c, PrimitiveType expected) {
        assertEquals(expected, PrimitiveType.fromChar(c),
            "PrimitiveType.fromChar('" + c + "') should return " + expected);
    }

    @ParameterizedTest(name = "fromChar(''{0}'') == null")
    @MethodSource("invalidChars")
    @DisplayName("fromChar returns null for every character that does not represent a JVM primitive")
    void invalidFromChar(char c) {
        assertNull(PrimitiveType.fromChar(c),
            "PrimitiveType.fromChar('" + c + "') should return null for an invalid character");
    }
}
