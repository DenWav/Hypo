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

package dev.denwav.hypo.model.data;

import dev.denwav.hypo.model.data.types.ArrayType;
import dev.denwav.hypo.model.data.types.ClassType;
import dev.denwav.hypo.model.data.types.JvmType;
import dev.denwav.hypo.model.data.types.PrimitiveType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("MethodDescriptor Tests")
public class MethodDescriptorTest {

    @Test
    @DisplayName("Test MethodDescriptor.parseDescriptor() and MethodDescriptor.toInternalString()")
    public void testParse() {
        // Test a whole bunch of permutations, no need to make each permutation its own test though
        final JvmType[] testTypes = new JvmType[]{
            PrimitiveType.CHAR,
            PrimitiveType.BYTE,
            PrimitiveType.SHORT,
            PrimitiveType.INT,
            PrimitiveType.LONG,
            PrimitiveType.FLOAT,
            PrimitiveType.DOUBLE,
            PrimitiveType.BOOLEAN,
            new ClassType("Ljava/lang/Object;"),
            new ClassType("Ljava/lang/String;"),
            new ArrayType(new ClassType("Ljava/lang/Object;"), 1),
            new ArrayType(new ClassType("Ljava/lang/String;"), 2)
        };

        final ArrayList<JvmType> returnTypeList = new ArrayList<>(Arrays.asList(testTypes));
        returnTypeList.add(PrimitiveType.VOID);
        final JvmType[] returnTypes = returnTypeList.toArray(new JvmType[0]);

        final JvmType[] params = new JvmType[4];
        final List<JvmType> paramList = Arrays.asList(params);

        for (final JvmType param0 : testTypes) {
            for (final JvmType param1 : testTypes) {
                for (final JvmType param2 : testTypes) {
                    for (final JvmType param3 : testTypes) {
                        params[0] = param0;
                        params[1] = param1;
                        params[2] = param2;
                        params[3] = param3;

                        for (final JvmType returnType : returnTypes) {
                            final MethodDescriptor dec = new MethodDescriptor(paramList, returnType);
                            Assertions.assertEquals(dec, MethodDescriptor.parseDescriptor(dec.toInternalString()));
                        }
                    }
                }
            }
        }
    }
}
