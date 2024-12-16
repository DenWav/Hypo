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
 * <h2><a href="https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.3">Type Descriptors</a></h2>
 *
 * <p>Type descriptors are the internal types used by the JVM to verify classes and fields, and link method overrides.
 * This package breaks type descriptors up into two categories, types and methods.
 *
 * <ul>
 *     <li>{@link dev.denwav.hypo.types.desc.TypeDescriptor}</li>
 *     <li>{@link dev.denwav.hypo.types.desc.MethodDescriptor}</li>
 * </ul>
 *
 * @see dev.denwav.hypo.types.sig Type Signatures
 */
package dev.denwav.hypo.types.desc;
