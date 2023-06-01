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

package dev.denwav.hypo.mappings;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
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

import static dev.denwav.hypo.mappings.LorenzUtil.checkType;
import static dev.denwav.hypo.mappings.LorenzUtil.notNull;
import static dev.denwav.hypo.model.HypoModelUtil.cast;

/**
 * {@link LorenzUtilHelper} implementation for Java 9+.
 */
@SuppressWarnings("unused") // Loaded dynamically
final class LorenzUtilHelperJdk9 extends LorenzUtilHelper {

    private static final VarHandle topLevelClassesHandle;
    private static final VarHandle innerClassesHandle;
    private static final VarHandle fieldsHandle;
    private static final VarHandle fieldsByNameHandle;
    private static final VarHandle methodsHandle;
    private static final VarHandle paramsHandle;

    static {
        try {
            topLevelClassesHandle = MethodHandles
                .privateLookupIn(MappingSetImpl.class, MethodHandles.lookup())
                .findVarHandle(MappingSetImpl.class, "topLevelClasses", Map.class);
            innerClassesHandle = MethodHandles
                .privateLookupIn(AbstractClassMappingImpl.class, MethodHandles.lookup())
                .findVarHandle(AbstractClassMappingImpl.class, "innerClasses", Map.class);
            fieldsHandle = MethodHandles
                .privateLookupIn(AbstractClassMappingImpl.class, MethodHandles.lookup())
                .findVarHandle(AbstractClassMappingImpl.class, "fields", Map.class);
            fieldsByNameHandle = MethodHandles
                .privateLookupIn(AbstractClassMappingImpl.class, MethodHandles.lookup())
                .findVarHandle(AbstractClassMappingImpl.class, "fieldsByName", Map.class);
            methodsHandle = MethodHandles
                .privateLookupIn(AbstractClassMappingImpl.class, MethodHandles.lookup())
                .findVarHandle(AbstractClassMappingImpl.class, "methods", Map.class);
            paramsHandle = MethodHandles
                .privateLookupIn(MethodMappingImpl.class, MethodHandles.lookup())
                .findVarHandle(MethodMappingImpl.class, "parameters", Map.class);
        } catch (final Throwable t) {
            throw new AssertionError(t);
        }
    }

    /**
     * Constructor.
     */
    LorenzUtilHelperJdk9() {}

    @Override
    @NotNull Map<String, TopLevelClassMapping> getTopLevelClassesMap(@NotNull MappingSet mappingSet) {
        final MappingSetImpl castMap = checkType(mappingSet);
        final Object handleResult = topLevelClassesHandle.get(castMap);
        final Map<String, TopLevelClassMapping> result = cast(handleResult);
        return notNull(result, "topLevelClasses");
    }

    @Override
    @NotNull Map<String, InnerClassMapping> getInnerClassesMap(@NotNull ClassMapping<?, ?> mapping) {
        final AbstractClassMappingImpl<?, ?> castMap = checkType(mapping);
        final Object handleResult = innerClassesHandle.get(castMap);
        final Map<String, InnerClassMapping> result = cast(handleResult);
        return notNull(result, "innerClasses");
    }

    @Override
    @NotNull Map<FieldSignature, FieldMapping> getFieldsMap(final @NotNull ClassMapping<?, ?> mapping) {
        final AbstractClassMappingImpl<?, ?> castMap = checkType(mapping);
        final Object handleResult = fieldsHandle.get(castMap);
        final Map<FieldSignature, FieldMapping> result = cast(handleResult);
        return notNull(result, "fields");
    }

    @Override
    @NotNull Map<String, FieldMapping> getFieldsByNameMap(final @NotNull ClassMapping<?, ?> mapping) {
        final AbstractClassMappingImpl<?, ?> castMap = checkType(mapping);
        final Object handleResult = fieldsByNameHandle.get(castMap);
        final Map<String, FieldMapping> result = cast(handleResult);
        return notNull(result, "fieldsByName");
    }

    @Override
    @NotNull Map<MethodSignature, MethodMapping> getMethodsMap(final @NotNull ClassMapping<?, ?> mapping) {
        final AbstractClassMappingImpl<?, ?> castMap = checkType(mapping);
        final Object handleResult = methodsHandle.get(castMap);
        final Map<MethodSignature, MethodMapping> result = cast(handleResult);
        return notNull(result, "methods");
    }

    @Override
    @NotNull Map<Integer, MethodParameterMapping> getParamsMap(final @NotNull MethodMapping mapping) {
        final MethodMappingImpl castMap = checkType(mapping);
        final Object handleResult = paramsHandle.get(castMap);
        final Map<Integer, MethodParameterMapping> result = cast(handleResult);
        return notNull(result, "parameters");
    }
}
