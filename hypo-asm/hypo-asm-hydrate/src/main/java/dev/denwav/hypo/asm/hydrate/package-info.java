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

/**
 * Default implementation of the keys in {@link dev.denwav.hypo.hydrate.generic.HypoHydration HypoHydration} for the
 * {@code hypo-asm} module. These hydrators require {@link dev.denwav.hypo.asm.AsmMethodData AsmMethodData} and
 * {@link dev.denwav.hypo.asm.AsmConstructorData AsmConstructorData} in the model in order to work.
 */
package dev.denwav.hypo.asm.hydrate;
