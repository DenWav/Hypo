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

import dev.denwav.hypo.model.data.ClassData;
import dev.denwav.hypo.model.data.FieldData;
import dev.denwav.hypo.model.data.MethodData;
import dev.denwav.hypo.model.data.MethodDescriptor;
import dev.denwav.hypo.model.data.types.ArrayType;
import dev.denwav.hypo.model.data.types.ClassType;
import dev.denwav.hypo.model.data.types.JvmType;
import dev.denwav.hypo.model.data.types.PrimitiveType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.cadixdev.bombe.type.FieldType;
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
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * General utility for interacting with the Lorenz API. This utility class helps with easing the interactions with
 * {@link Optional} and it provides utilities to help with modifying {@link MappingSet} models outside of what the
 * standard Lorenz API allows.
 */
public final class LorenzUtil {

    private LorenzUtil() {}

    /**
     * Return a {@link Nullable} version of what the given {@link Optional} contains. This is just a cleaner way of
     * calling {@code orElse(null)}.
     *
     * @param opt The optional to unwrap.
     * @param <T> The type of the optional.
     * @return The value the optional contains, or {@code null} if it is {@link Optional#empty() empty}.
     */
    @SuppressWarnings({"OptionalAssignedToNull", "OptionalUsedAsFieldOrParameterType"})
    public static <T> @Nullable T unwrap(final @Nullable Optional<T> opt) {
        if (opt == null) {
            return null;
        }
        return opt.orElse(null);
    }

    /**
     * Find {@link MethodData} the obfuscated name of the given {@link MethodMapping} refers to in the given
     * {@link ClassData} object.
     *
     * @param classData The class data to find the method in.
     * @param mapping The mapping to determine the reference from.
     * @return The {@link MethodData} or {@code null} if not found.
     */
    @Contract(pure = true)
    public static @Nullable MethodData findMethod(
        final @NotNull ClassData classData,
        final @NotNull MethodMapping mapping
    ) {
        return classData.method(mapping.getObfuscatedName(), convertDesc(mapping.getDescriptor()));
    }

    /**
     * Find the set of {@link FieldData} the obfuscated name of the given {@link FieldMapping} refers to in the given
     * {@link ClassData} object.
     *
     * @param classData The class data to find the method in.
     * @param mapping The mapping to determine the reference from.
     * @return The set of {@link FieldData}.
     */

    @Contract(pure = true)
    public static @NotNull List<@NotNull FieldData> findField(
        final @NotNull ClassData classData,
        final @NotNull FieldMapping mapping
    ) {
        final FieldType type = getType(mapping);
        if (type == null) {
            return classData.fields(mapping.getObfuscatedName());
        } else {
            final FieldData field = classData.field(mapping.getObfuscatedName(), convertType(type));
            if (field == null) {
                return Collections.emptyList();
            } else {
                return Collections.singletonList(field);
            }
        }
    }

    /**
     * Convert a Bombe {@link org.cadixdev.bombe.type.MethodDescriptor MethodDescriptor} into a Hypo
     * {@link MethodDescriptor}.
     *
     * @param desc The Bombe descriptor to convert.
     * @return The same descriptor, but in the Hypo model.
     */
    public static @NotNull MethodDescriptor convertDesc(final @NotNull org.cadixdev.bombe.type.MethodDescriptor desc) {
        final ArrayList<JvmType> params = new ArrayList<>(desc.getParamTypes().size());
        for (final org.cadixdev.bombe.type.FieldType paramType : desc.getParamTypes()) {
            params.add(convertType(paramType));
        }
        return new MethodDescriptor(params, convertType(desc.getReturnType()));
    }

    /**
     * Convert a Bombe {@link org.cadixdev.bombe.type.Type Type} into a Hypo {@link JvmType}.
     *
     * @param type The Bombe type to convert.
     * @return The same type, but in the Hypo model.
     */
    public static @NotNull JvmType convertType(final @NotNull org.cadixdev.bombe.type.Type type) {
        // ArrayType
        if (type instanceof org.cadixdev.bombe.type.ArrayType) {
            final org.cadixdev.bombe.type.ArrayType array = (org.cadixdev.bombe.type.ArrayType) type;
            return new ArrayType(convertType(array.getComponent()), array.getDimCount());
            // Primitive (BaseType and VoidType)
        } else if (type instanceof org.cadixdev.bombe.type.PrimitiveType) {
            return PrimitiveType.fromChar(((org.cadixdev.bombe.type.PrimitiveType) type).getKey());
            // ObjectType is the only possibility left
        } else if (type instanceof org.cadixdev.bombe.type.ObjectType) {
            final org.cadixdev.bombe.type.ObjectType obj = (org.cadixdev.bombe.type.ObjectType) type;
            return new ClassType(obj.getClassName());
        } else {
            throw new IllegalStateException("Unknown type: " + type);
        }
    }

    /**
     * {@link Nullable} form of {@link MappingSet#getClassMapping(String)}.
     *
     * @param mappings The mapping set to call.
     * @param obfuscatedName The class name to search for.
     * @return The {@link ClassMapping}, or {@code null} if not found.
     * @see #unwrap(Optional)
     */
    public static @Nullable ClassMapping<?, ?> getClassMapping(
        final @Nullable MappingSet mappings,
        final @NotNull String obfuscatedName
    ) {
        if (mappings == null) {
            return null;
        }
        return unwrap(mappings.getClassMapping(obfuscatedName));
    }

    /**
     * {@link Nullable} form of {@link ClassMapping#getMethodMapping(String, String)}.
     *
     * @param mapping The class mapping to call.
     * @param name The method name to search for.
     * @param desc The method descriptor to search for.
     * @return The {@link MethodMapping}, or {@code null} if not found.
     * @see #unwrap(Optional)
     */
    public static @Nullable MethodMapping getMethodMapping(
        final @Nullable ClassMapping<?, ?> mapping,
        final @NotNull String name,
        final @NotNull String desc
    ) {
        if (mapping == null) {
            return null;
        }
        return unwrap(mapping.getMethodMapping(name, desc));
    }

    /**
     * {@link Nullable} form of {@link ClassMapping#getFieldMapping(String)}.
     *
     * @param mapping The class mapping to call.
     * @param name The field name to search for.
     * @return The {@link FieldMapping} or {@code null} if not found.
     * @see #unwrap(Optional)
     */
    public static @Nullable FieldMapping getFieldMapping(
        final @Nullable ClassMapping<?, ?> mapping,
        final @NotNull String name
    ) {
        if (mapping == null) {
            return null;
        }
        return unwrap(mapping.getFieldMapping(name));
    }

    /**
     * {@link Nullable} form of {@link ClassMapping#getFieldMapping(FieldSignature)}.
     *
     * @param mapping The class mapping to call.
     * @param sig The {@link FieldSignature} to search for.
     * @return The {@link FieldMapping} or {@code null} if not found.
     * @see #unwrap(Optional)
     */
    public static @Nullable FieldMapping getFieldMapping(
        final @Nullable ClassMapping<?, ?> mapping,
        final @NotNull FieldSignature sig
    ) {
        if (mapping == null) {
            return null;
        }
        return unwrap(mapping.getFieldMapping(sig));
    }

    /**
     * {@link Nullable} form of {@link MethodMapping#getParameterMapping(int)}.
     *
     * @param mapping The method mapping to call.
     * @param index The parameter index to search for.
     * @return The {@link MethodParameterMapping}, or {@code null} if not found.
     * @see #unwrap(Optional)
     */
    public static @Nullable MethodParameterMapping getParameterMapping(
        final @Nullable MethodMapping mapping,
        final int index
    ) {
        if (mapping == null) {
            return null;
        }
        return unwrap(mapping.getParameterMapping(index));
    }

    /**
     * The {@link Nullable} form of {@link FieldMapping#getType()}.
     *
     * @param mapping The field mapping to call.
     * @return The {@link FieldType} or {@code null} if not found.
     * @see #unwrap(Optional)
     */
    public static @Nullable FieldType getType(final @NotNull FieldMapping mapping) {
        return unwrap(mapping.getType());
    }

    /**
     * Remove the given {@link ClassMapping} from its parent.
     *
     * @param mapping The class mapping to remove.
     */
    public static void removeClassMapping(final @NotNull ClassMapping<?, ?> mapping) {
        if (mapping instanceof TopLevelClassMapping) {
            removeTopLevelClassMapping(mapping, mapping.getMappings());
        } else if (mapping instanceof InnerClassMapping) {
            removeInnerClassMapping(mapping, ((InnerClassMapping) mapping).getParent());
        }
    }

    /**
     * Remove the given {@link MethodMapping} from its parent.
     *
     * @param mapping The method mapping to remove.
     */
    public static void removeMethodMapping(final @NotNull MethodMapping mapping) {
        removeMethodMapping(mapping, mapping.getParent());
    }

    /**
     * Remove the given {@link FieldMapping} from its parent.
     *
     * @param mapping The field mapping to remove.
     */
    public static void removeFieldMapping(final @NotNull FieldMapping mapping) {
        removeFieldMapping(mapping, mapping.getParent());
    }

    /**
     * Remove the given {@link MethodParameterMapping} from its parent.
     *
     * @param mapping The method parameter mapping to remove.
     */
    public static void removeParamMapping(final @NotNull MethodParameterMapping mapping) {
        removeParamMapping(mapping, mapping.getParent());
    }

    /**
     * Remove the given {@link TopLevelClassMapping} from the given {@link MappingSet}.
     *
     * <p>This method does nothing if {@code topLevelClassMapping} is not a {@link TopLevelClassMapping}.
     *
     * @param topLevelClassMapping The class mapping to remove.
     * @param mappingSet The mapping set to remove the class from.
     */
    public static void removeTopLevelClassMapping(
        final @NotNull ClassMapping<?, ?> topLevelClassMapping,
        final @NotNull MappingSet mappingSet
    ) {
        if (topLevelClassMapping instanceof TopLevelClassMapping) {
            getTopLevelClassesMap(mappingSet).values().remove(topLevelClassMapping);
        }
    }

    /**
     * Remove the given {@link InnerClassMapping} from the given {@link ClassMapping}.
     *
     * <p>This method does nothing if {@code innerClassMapping} is not a {@link InnerClassMapping}.
     *
     * @param innerClassMapping The class mapping to remove.
     * @param mapping The class mapping to remove the class from.
     */
    public static void removeInnerClassMapping(
        final @NotNull ClassMapping<?, ?> innerClassMapping,
        final @NotNull ClassMapping<?, ?> mapping
    ) {
        if (innerClassMapping instanceof InnerClassMapping) {
            getInnerClassesMap(mapping).values().remove(innerClassMapping);
        }
    }

    /**
     * Remove the given {@link FieldMapping} from the given {@link ClassMapping}.
     *
     * @param fieldMapping The field mapping to remove.
     * @param mapping The class mapping to remove the field from.
     */
    public static void removeFieldMapping(
        final @NotNull FieldMapping fieldMapping,
        final @NotNull ClassMapping<?, ?> mapping
    ) {
        getFieldsMap(mapping).values().remove(fieldMapping);
        getFieldsByNameMap(mapping).values().remove(fieldMapping);
    }

    /**
     * Remove the given {@link MethodMapping} from the given {@link ClassMapping}.
     *
     * @param methodMapping The method mapping to remove.
     * @param mapping The class mapping to remove the method from.
     */
    public static void removeMethodMapping(
        final @NotNull MethodMapping methodMapping,
        final @NotNull ClassMapping<?, ?> mapping
    ) {
        getMethodsMap(mapping).values().remove(methodMapping);
    }

    /**
     * Remove the given {@link MethodParameterMapping} from the given {@link MethodMapping}.
     *
     * @param paramMappings The method parameter mapping to remove.
     * @param mapping The method mapping to remove the parameter from.
     */
    public static void removeParamMapping(
        final @NotNull MethodParameterMapping paramMappings,
        final @NotNull MethodMapping mapping
    ) {
        getParamsMap(mapping).values().remove(paramMappings);
    }

    /**
     * Get the internal map which stores top level class mappings from the given {@link MappingSet}.
     *
     * @param mappingSet The mapping set to retrieve the map from.
     * @return The internal map of top level class mappings.
     */
    @Contract(pure = true)
    public static @NotNull Map<String, TopLevelClassMapping> getTopLevelClassesMap(final @NotNull MappingSet mappingSet) {
        return LorenzUtilHelper.INSTANCE.getTopLevelClassesMap(mappingSet);
    }

    /**
     * Get the internal map which stores inner class mappings from the given {@link ClassMapping}.
     *
     * @param mapping The class mapping to retrieve the map from.
     * @return The internal map of inner class mappings.
     */
    @Contract(pure = true)
    public static @NotNull Map<String, InnerClassMapping> getInnerClassesMap(final @NotNull ClassMapping<?, ?> mapping) {
        return LorenzUtilHelper.INSTANCE.getInnerClassesMap(mapping);
    }

    /**
     * Get the internal map which stores field mappings by signature from the given {@link ClassMapping}.
     *
     * @param mapping The class mapping to retrieve the map from.
     * @return The internal map of field mappings by signature.
     */
    @Contract(pure = true)
    public static @NotNull Map<FieldSignature, FieldMapping> getFieldsMap(final @NotNull ClassMapping<?, ?> mapping) {
        return LorenzUtilHelper.INSTANCE.getFieldsMap(mapping);
    }

    /**
     * Get the internal map which stores field mappings by name from the given {@link ClassMapping}.
     *
     * @param mapping The class mapping to retrieve the map from.
     * @return The internal map of field mappings by name.
     */
    @Contract(pure = true)
    public static @NotNull Map<String, FieldMapping> getFieldsByNameMap(final @NotNull ClassMapping<?, ?> mapping) {
        return LorenzUtilHelper.INSTANCE.getFieldsByNameMap(mapping);
    }

    /**
     * Get the internal map which stores method mappings from the given {@link ClassMapping}.
     *
     * @param mapping The class mapping to retrieve the map from.
     * @return The internal map of method mappings.
     */
    @Contract(pure = true)
    public static @NotNull Map<MethodSignature, MethodMapping> getMethodsMap(final @NotNull ClassMapping<?, ?> mapping) {
        return LorenzUtilHelper.INSTANCE.getMethodsMap(mapping);
    }

    /**
     * Get the internal map which stores parameter mappings from the given {@link MethodMapping}.
     *
     * @param mapping The method mapping to retrieve the map from.
     * @return The internal map of parameter mappings.
     */
    @Contract(pure = true)
    public static @NotNull Map<Integer, MethodParameterMapping> getParamsMap(final @NotNull MethodMapping mapping) {
        return LorenzUtilHelper.INSTANCE.getParamsMap(mapping);
    }

    /**
     * Verify the {@link MappingSet} is a {@link MappingSetImpl}.
     *
     * @param mappingSet The {@link MappingSet} to verify.
     * @return The same mapping set, cast to {@link MappingSetImpl}.
     */
    @Contract(value = "_ -> param1", pure = true)
    static @NotNull MappingSetImpl checkType(final @NotNull MappingSet mappingSet) {
        if (!(mappingSet instanceof MappingSetImpl)) {
            throw new IllegalArgumentException("Argument is not an instance of " + MappingSetImpl.class.getName());
        }
        return (MappingSetImpl) mappingSet;
    }

    /**
     * Verify the {@link ClassMapping} is an {@link AbstractClassMappingImpl}.
     *
     * @param mapping The {@link ClassMapping} to verify.
     * @return The same mapping, cast to {@link AbstractClassMappingImpl}.
     */
    @Contract(value = "_ -> param1", pure = true)
    static @NotNull AbstractClassMappingImpl<?, ?> checkType(final @NotNull ClassMapping<?, ?> mapping) {
        if (!(mapping instanceof AbstractClassMappingImpl)) {
            throw new IllegalArgumentException("Argument is not an instance of " + AbstractClassMappingImpl.class.getName());
        }
        return (AbstractClassMappingImpl<?, ?>) mapping;
    }

    /**
     * Verify the {@link MethodMapping} is an {@link MethodMappingImpl}.
     *
     * @param mapping The {@link MethodMapping} to verify.
     * @return The same mapping, cast to {@link MethodMappingImpl}.
     */
    @Contract(value = "_ -> param1", pure = true)
    static @NotNull MethodMappingImpl checkType(final @NotNull MethodMapping mapping) {
        if (!(mapping instanceof MethodMappingImpl)) {
            throw new IllegalArgumentException("Argument is not an instance of " + MethodMappingImpl.class.getName());
        }
        return (MethodMappingImpl) mapping;
    }

    /**
     * Verify {@code t} is not null, throwing a {@link NullPointerException} specifying the field which is null using
     * the given {@code fieldName}.
     *
     * @param t The value to verify is not null.
     * @param fieldName The name of the field the value came from.
     * @param <T> The type of the field.
     * @return The given value as non-null.
     */
    @Contract(value = "null, _ -> fail; !null, _ -> param1", pure = true)
    static <T> @NotNull T notNull(final @Nullable T t, final String fieldName) {
        if (t == null) {
            throw new NullPointerException(AbstractClassMappingImpl.class.getName() + "." + fieldName + " is null");
        }
        return t;
    }
}
