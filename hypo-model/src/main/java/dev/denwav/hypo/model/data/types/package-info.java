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
 * Java types model for the Hypo class data model defined in {@link dev.denwav.hypo.model.data}.
 *
 * <p>The base type model class is {@link dev.denwav.hypo.model.data.types.JvmType JvmType}, which is the base
 * interface for the 3 categories of types:
 *
 * <ul>
 *     <li>{@link dev.denwav.hypo.model.data.types.PrimitiveType primitive types}</li>
 *     <li>{@link dev.denwav.hypo.model.data.types.ClassType class types}</li>
 *     <li>{@link dev.denwav.hypo.model.data.types.ArrayType array types}</li>
 * </ul>
 */
package dev.denwav.hypo.model.data.types;
