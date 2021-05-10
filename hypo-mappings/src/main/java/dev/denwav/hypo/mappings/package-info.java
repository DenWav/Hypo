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
 * The mappings analysis component of Hypo. This module uses the <a href="https://github.com/CadixDev/Lorenz">Lorenz</a>
 * mappings library for mappings analysis and modification.
 *
 * <p>This module represents both a generic API for defining custom mappings change contributors in the
 * {@link dev.denwav.hypo.mappings.contributors} package as well as multiple pre-defined contributors for handling
 * most standard cases.
 *
 * <p>Mappings changes go through the
 * {@link dev.denwav.hypo.mappings.MappingsCompletionManager MappingsCompletionManager} which handles the logic of
 * orchestrating the different components of mappings completion.
 */
package dev.denwav.hypo.mappings;
