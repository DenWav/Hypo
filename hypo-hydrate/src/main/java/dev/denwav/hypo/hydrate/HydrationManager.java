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
import dev.denwav.hypo.model.data.HypoData;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.io.IOException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Manager for the process of data hydration.
 *
 * <p>Hydration consists of 2 parts:
 *
 * <ol>
 *     <li>Base hydration of class hierarchy data</li>
 *     <li>Additional arbitrary hydrations vis {@link HydrationProvider}</li>
 * </ol>
 *
 * <p>Implementations of this interface are responsible for both parts of this process.
 */
public interface HydrationManager {

    /**
     * Returns the default implementation of this interface, which should be sufficient for most use cases.
     *
     * <p>The default implementation is {@link DefaultHydrationManager}.
     * @return The default implementation of this interface.
     */
    static @NotNull HydrationManager createDefault() {
        return new DefaultHydrationManager();
    }

    /**
     * Register an arbitrary {@link HydrationProvider} to run during the second phase of hydration, after class
     * hierarchy data.
     *
     * @param provider The {@link HydrationProvider} to run during hydration.
     * @return {@code this} for chaining.
     */
    @CanIgnoreReturnValue
    @Contract("_ -> this")
    @NotNull HydrationManager register(final @NotNull HydrationProvider<? extends HypoData> provider);

    /**
     * Walk over all classes found in {@link HypoContext#getProvider()}, running both phases of the hydration process
     * on all classes.
     *
     * @param context The {@link HypoContext context} to hydrate.
     * @throws IOException If an IO error occurs while reading the class data.
     */
    void hydrate(final @NotNull HypoContext context) throws IOException;
}
