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

package dev.denwav.hypo.hydrate.generic;

import dev.denwav.hypo.model.data.HypoKey;
import dev.denwav.hypo.model.data.MethodData;
import java.util.List;

/**
 * Core common data keys for hydration. Hydrators for these keys are implemented in the {@code hypo-asm} module.
 */
public final class HypoHydration {

    private HypoHydration() {}

    /**
     * The {@link MethodData method} the synthetic method this {@link HypoKey} is set on targets.
     */
    public static final HypoKey<MethodData> SYNTHETIC_TARGET = HypoKey.create("Synthetic Target");
    /**
     * The synthetic {@link MethodData method} which calls the method this {@link HypoKey} is set on.
     */
    public static final HypoKey<MethodData> SYNTHETIC_SOURCE = HypoKey.create("Synthetic Source");

    /**
     * The list of {@link SuperCall super calls} which calls the constructor this {@link HypoKey} is set on.
     */
    public static final HypoKey<List<SuperCall>> SUPER_CALLER_SOURCES = HypoKey.create("Constructor Super Call Sources");
    /**
     * The {@link SuperCall super call} the constructor this {@link HypoKey} is set on calls.
     */
    public static final HypoKey<SuperCall> SUPER_CALL_TARGET = HypoKey.create("Constructor Super Call Target");
}
