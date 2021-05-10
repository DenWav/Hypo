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

package dev.denwav.hypo.hydrate;

import dev.denwav.hypo.core.HypoContext;
import dev.denwav.hypo.model.data.FieldData;
import dev.denwav.hypo.model.data.HypoData;
import dev.denwav.hypo.model.data.ClassData;
import dev.denwav.hypo.model.data.MethodData;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;

/**
 * Arbitrary Hypo hydration provider. An implementation of this interface can target any class which extends
 * {@link HypoData}, as needed.
 *
 * <p>The default implementation of {@link DefaultHydrationManager} runs registered
 * {@link HydrationProvider providers} on {@link ClassData ClassData},
 * {@link MethodData MethodData}, and
 * {@link FieldData FieldData} objects that each provider targets.
 *
 * @param <T> The type of the {@link HypoData} this provider targets.
 */
public interface HydrationProvider<T extends HypoData> {

    /**
     * The {@link Class} this provider targets. This provider will be run against {@link HypoData} objects which extend
     * this class.
     *
     * @return The {@link Class} this provider targets.
     */
    @NotNull Class<? extends T> target();

    /**
     * Hydrate the given {@code data} object in the given {@link HypoContext context}.
     *
     * @param data The {@link HypoData} object to hydrate.
     * @param context The {@link HypoContext context} of this Hypo execution.
     * @throws IOException If an IO error occurs while hydrating the {@code data} object.
     */
    void hydrate(final @NotNull T data, final @NotNull HypoContext context) throws IOException;
}
