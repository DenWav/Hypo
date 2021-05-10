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
 * The Hypo model is made up of 2 pieces:
 * <ol>
 *     <li>The Hypo class data producers</li>
 *     <li>The Hypo class data model</li>
 * </ol>
 *
 * <p>The items in this package make up the first part, the items in the {@link dev.denwav.hypo.model.data} package make
 * up the second part.
 *
 * <p>The core piece of the entire Hypo model is the
 * {@link dev.denwav.hypo.model.ClassDataProvider ClassDataProvider}.
 */
package dev.denwav.hypo.model;
