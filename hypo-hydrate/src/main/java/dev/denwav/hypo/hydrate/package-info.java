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
 * Hydration is the process where extra data about the Hypo model is computed by walking the entire classpath of the
 * context. Many details about the Java classpath cannot be fully known unless the entire classpath is visited, and many
 * properties of Java classes or methods or fields can't be easily determined directly without looking closer at the
 * bytecode with more complex logic. Hydration is the combination of both of these tasks.
 *
 * <p>{@link dev.denwav.hypo.hydrate.HydrationManager HydrationManager} is the orchestration class which manages the
 * process of Hypo model hydration, and additional arbitrary
 * {@link dev.denwav.hypo.hydrate.HydrationProvider providers} can be implemented in order to provide additional
 * hydration of the model.
 */
package dev.denwav.hypo.hydrate;
