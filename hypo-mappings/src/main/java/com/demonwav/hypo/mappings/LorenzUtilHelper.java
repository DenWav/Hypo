/*
 * Hypo, an extensible and pluggable Java bytecode analytical model.
 *
 * Copyright (C) 2021  Kyle Wood (DemonWav)
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

package com.demonwav.hypo.mappings;

import com.demonwav.hypo.model.HypoModelUtil;
import java.util.Map;
import org.cadixdev.bombe.type.signature.FieldSignature;
import org.cadixdev.bombe.type.signature.MethodSignature;
import org.cadixdev.lorenz.MappingSet;
import org.cadixdev.lorenz.model.ClassMapping;
import org.cadixdev.lorenz.model.FieldMapping;
import org.cadixdev.lorenz.model.InnerClassMapping;
import org.cadixdev.lorenz.model.MethodMapping;
import org.cadixdev.lorenz.model.MethodParameterMapping;
import org.cadixdev.lorenz.model.TopLevelClassMapping;
import org.jetbrains.annotations.NotNull;

/**
 * Helper class for {@link LorenzUtil} which delegates logic based on the Java version of the currently running JVM.
 */
abstract class LorenzUtilHelper {

    /**
     * The instance compatible with the currently running JVM.
     */
    static final @NotNull LorenzUtilHelper INSTANCE;

    static {
        LorenzUtilHelper i;
        try {
            i = Class.forName(LorenzUtilHelper.class.getName() + "Jdk9")
                .asSubclass(LorenzUtilHelper.class).getDeclaredConstructor().newInstance();
        } catch (final Throwable t) {
            try {
                i = Class.forName(LorenzUtilHelper.class.getName() + "Jdk8")
                    .asSubclass(LorenzUtilHelper.class).getDeclaredConstructor().newInstance();
            } catch (Throwable t2) {
                throw HypoModelUtil.rethrow(HypoModelUtil.addSuppressed(t, t2));
            }
        }

        INSTANCE = i;
    }

    /**
     * Get the internal map which stores top level class mappings from the given {@link MappingSet}.
     *
     * @param mappingSet The mapping set to retrieve the map from.
     * @return The internal map of top level class mappings.
     */
    abstract @NotNull Map<String, TopLevelClassMapping> getTopLevelClassesMap(final @NotNull MappingSet mappingSet);

    /**
     * Get the internal map which stores inner class mappings from the given {@link ClassMapping}.
     *
     * @param mapping The class mapping to retrieve the map from.
     * @return The internal map of inner class mappings.
     */
    abstract @NotNull Map<String, InnerClassMapping> getInnerClassesMap(final @NotNull ClassMapping<?, ?> mapping);

    /**
     * Get the internal map which stores field mappings by signature from the given {@link ClassMapping}.
     *
     * @param mapping The class mapping to retrieve the map from.
     * @return The internal map of field mappings by signature.
     */
    abstract @NotNull Map<FieldSignature, FieldMapping> getFieldsMap(final @NotNull ClassMapping<?, ?> mapping);

    /**
     * Get the internal map which stores field mappings by name from the given {@link ClassMapping}.
     *
     * @param mapping The class mapping to retrieve the map from.
     * @return The internal map of field mappings by name.
     */
    abstract @NotNull Map<String, FieldMapping> getFieldsByNameMap(final @NotNull ClassMapping<?, ?> mapping);

    /**
     * Get the internal map which stores method mappings from the given {@link ClassMapping}.
     *
     * @param mapping The class mapping to retrieve the map from.
     * @return The internal map of method mappings.
     */
    abstract @NotNull Map<MethodSignature, MethodMapping> getMethodsMap(final @NotNull ClassMapping<?, ?> mapping);

    /**
     * Get the internal map which stores parameter mappings from the given {@link MethodMapping}.
     *
     * @param mapping The method mapping to retrieve the map from.
     * @return The internal map of parameter mappings.
     */
    abstract @NotNull Map<Integer, MethodParameterMapping> getParamsMap(final @NotNull MethodMapping mapping);
}
