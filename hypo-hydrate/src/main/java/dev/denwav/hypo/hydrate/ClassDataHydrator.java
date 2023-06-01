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

package dev.denwav.hypo.hydrate;

import dev.denwav.hypo.core.HypoContext;
import dev.denwav.hypo.model.data.ClassData;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;

/**
 * Data model hydrator for {@link ClassData ClassData} objects.
 *
 * <p>Hydration consists of 2 parts:
 *
 * <ol>
 *     <li>Base hydration of class hierarchy data</li>
 *     <li>Additional arbitrary hydrations vis {@link HydrationProvider}</li>
 * </ol>
 *
 * <p>This class is responsible for only the first part, the class hierarchy data hydration.
 *
 * <p>The default implementation of this interface is {@link DefaultClassDataHydrator}, and it is implemented using only
 * the base {@link ClassData ClassData} APIs, so it should be okay for most cases.
 *
 * <p>Use {@link HydrationManager} to handle the orchestration of all of the parts of hydration.
 */
public interface ClassDataHydrator {

    /**
     * Walk over all classes found in {@link HypoContext#getProvider()}, filling in the class hierarchy information
     * into the model.
     *
     * @param context The {@link HypoContext context} to hydrate.
     * @throws IOException If an IO error occurs while reading the class data.
     */
    void hydrate(final @NotNull HypoContext context) throws IOException;

    /**
     * Returns the default implementation of this interface, which should be sufficient for most use cases.
     *
     * <p>The default implementation is {@link DefaultClassDataHydrator}.
     * @return The default implementation of this interface.
     */
    static @NotNull ClassDataHydrator createDefault() {
        return new DefaultClassDataHydrator();
    }
}
