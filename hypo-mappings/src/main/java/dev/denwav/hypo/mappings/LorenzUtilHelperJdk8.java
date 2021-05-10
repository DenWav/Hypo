/*
 * Hypo, an extensible and pluggable Java bytecode analytical model.
 *
 * Copyright (C) 2021  Kyle Wood (DenWav)
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

package dev.denwav.hypo.mappings;

import java.lang.reflect.Field;
import java.util.Map;
import org.cadixdev.bombe.type.signature.FieldSignature;
import org.cadixdev.bombe.type.signature.MethodSignature;
import org.cadixdev.lorenz.MappingSet;
import org.cadixdev.lorenz.impl.MappingSetImpl;
import org.cadixdev.lorenz.impl.model.AbstractClassMappingImpl;
import org.cadixdev.lorenz.impl.model.MethodMappingImpl;
import org.cadixdev.lorenz.model.ClassMapping;
import org.cadixdev.lorenz.model.FieldMapping;
import org.cadixdev.lorenz.model.InnerClassMapping;
import org.cadixdev.lorenz.model.MethodMapping;
import org.cadixdev.lorenz.model.MethodParameterMapping;
import org.cadixdev.lorenz.model.TopLevelClassMapping;
import org.jetbrains.annotations.NotNull;
import sun.misc.Unsafe;

import static dev.denwav.hypo.mappings.LorenzUtil.checkType;
import static dev.denwav.hypo.mappings.LorenzUtil.notNull;
import static dev.denwav.hypo.model.HypoModelUtil.cast;

/**
 * {@link LorenzUtilHelper} implementation for Java 8.
 */
@SuppressWarnings("unused") // Loaded dynamically
final class LorenzUtilHelperJdk8 extends LorenzUtilHelper {

    private static final @NotNull Unsafe unsafe;

    private static final long topLevelClassesOffset;
    private static final long innerClassesOffset;
    private static final long fieldsOffset;
    private static final long fieldsByNameOffset;
    private static final long methodsOffset;
    private static final long paramsOffset;

    static {
        try {
            final Field theUnsafe = Class.forName("sun.misc.Unsafe").getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            unsafe = (Unsafe) theUnsafe.get(null);

            topLevelClassesOffset = unsafe.objectFieldOffset(MappingSetImpl.class.getDeclaredField("topLevelClasses"));
            innerClassesOffset = unsafe.objectFieldOffset(AbstractClassMappingImpl.class.getDeclaredField("innerClasses"));
            fieldsOffset = unsafe.objectFieldOffset(AbstractClassMappingImpl.class.getDeclaredField("fields"));
            fieldsByNameOffset = unsafe.objectFieldOffset(AbstractClassMappingImpl.class.getDeclaredField("fieldsByName"));
            methodsOffset = unsafe.objectFieldOffset(AbstractClassMappingImpl.class.getDeclaredField("methods"));
            paramsOffset = unsafe.objectFieldOffset(MethodMappingImpl.class.getDeclaredField("parameters"));
        } catch (final Throwable t) {
            throw new AssertionError(t);
        }
    }

    /**
     * Constructor.
     */
    LorenzUtilHelperJdk8() {}

    @Override
    @NotNull Map<String, TopLevelClassMapping> getTopLevelClassesMap(@NotNull MappingSet mappingSet) {
        final MappingSetImpl castMap = checkType(mappingSet);
        final Object unsafeResult = unsafe.getObject(castMap, innerClassesOffset);
        final Map<String, TopLevelClassMapping> result = cast(unsafeResult);
        return notNull(result, "topLevelClasses");
    }

    @Override
    final @NotNull Map<String, InnerClassMapping> getInnerClassesMap(@NotNull ClassMapping<?, ?> mapping) {
        final AbstractClassMappingImpl<?, ?> castMap = checkType(mapping);
        final Object unsafeResult = unsafe.getObject(castMap, innerClassesOffset);
        final Map<String, InnerClassMapping> result = cast(unsafeResult);
        return notNull(result, "innerClasses");
    }

    @Override
    @NotNull Map<FieldSignature, FieldMapping> getFieldsMap(final @NotNull ClassMapping<?, ?> mapping) {
        final AbstractClassMappingImpl<?, ?> castMap = checkType(mapping);
        final Object unsafeResult = unsafe.getObject(castMap, fieldsOffset);
        final Map<FieldSignature, FieldMapping> result = cast(unsafeResult);
        return notNull(result, "fields");
    }

    @Override
    @NotNull Map<String, FieldMapping> getFieldsByNameMap(final @NotNull ClassMapping<?, ?> mapping) {
        final AbstractClassMappingImpl<?, ?> castMap = checkType(mapping);
        final Object unsafeResult = unsafe.getObject(castMap, fieldsByNameOffset);
        final Map<String, FieldMapping> result = cast(unsafeResult);
        return notNull(result, "fieldsByName");
    }

    @Override
    @NotNull Map<MethodSignature, MethodMapping> getMethodsMap(final @NotNull ClassMapping<?, ?> mapping) {
        final AbstractClassMappingImpl<?, ?> castMap = checkType(mapping);
        final Object unsafeResult = unsafe.getObject(castMap, methodsOffset);
        final Map<MethodSignature, MethodMapping> result = cast(unsafeResult);
        return notNull(result, "methods");
    }

    @Override
    @NotNull Map<Integer, MethodParameterMapping> getParamsMap(final @NotNull MethodMapping mapping) {
        final MethodMappingImpl castMap = checkType(mapping);
        final Object unsafeResult = unsafe.getObject(castMap, paramsOffset);
        final Map<Integer, MethodParameterMapping> result = cast(unsafeResult);
        return notNull(result, "parameters");
    }
}
