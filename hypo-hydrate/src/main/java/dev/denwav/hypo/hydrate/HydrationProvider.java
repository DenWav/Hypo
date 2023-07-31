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
import dev.denwav.hypo.model.HypoModelUtil;
import dev.denwav.hypo.model.data.FieldData;
import dev.denwav.hypo.model.data.HypoData;
import dev.denwav.hypo.model.data.ClassData;
import dev.denwav.hypo.model.data.HypoKey;
import dev.denwav.hypo.model.data.MethodData;
import java.io.IOException;
import java.util.List;
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

    /**
     * Optionally marks which {@link HypoKey HypoKeys} this hydration providers provides. This is only really applicable
     * for hydration provider dependency checking. An empty list does not imply this hydration provider does not produce
     * any keys, it just means the provider does not report what it produces.
     *
     * @return The {@link HypoKey HypoKeys} this hydration provider provides, if it reports this information.
     */
    default List<HypoKey<?>> provides() {
        return HypoModelUtil.immutableListOf();
    }

    /**
     * Marks which {@link HypoKey HypoKeys} this hydration provider would like to have available during the hydration
     * process, but are not required. This is used for determining the order the hydration providers should run in.
     * @return Which {@link HypoKey HypoKeys} this hydration provider would like to have available during hydration.
     */
    default List<HypoKey<?>> dependsOn() {
        return HypoModelUtil.immutableListOf();
    }
}
