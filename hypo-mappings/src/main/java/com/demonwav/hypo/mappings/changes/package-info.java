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

/**
 * Changes to mappings are scheduled and then executed using
 * {@link com.demonwav.hypo.mappings.MappingsChange MappingsChange} and
 * {@link com.demonwav.hypo.mappings.ClassMappingsChange ClassMappingsChange} implementations. This package holds
 * several default mappings change implementations used by the contributors in
 * {@link com.demonwav.hypo.mappings.contributors}.
 */
package com.demonwav.hypo.mappings.changes;
