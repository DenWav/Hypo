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

/**
 * Hydration can produce arbitrary data which is stored into {@link dev.denwav.hypo.model.data.HypoData HypoData}
 * objects mapped via a {@link dev.denwav.hypo.model.data.HypoKey HypoKey} as the value key. Arbitrary
 * {@link dev.denwav.hypo.hydrate.HydrationProvider hydration providers} may provide any data they like using any key,
 * but this package provides a few common keys which may be implemented by other hydration providers.
 *
 * <p>These keys are in the {@link dev.denwav.hypo.hydrate.generic.HypoHydration HypoHydration} class, and the default
 * implementation for these keys is in the {@code hypo-asm-hydration} module.
 */
package dev.denwav.hypo.hydrate.generic;
