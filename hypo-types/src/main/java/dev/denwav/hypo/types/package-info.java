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
 * The Java type system, consisting of type descriptors and type signatures.
 *
 * <ul>
 *     <li><a href="https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.3">Type Descriptors</a> -> {@link dev.denwav.hypo.types.desc Hypo Type Descriptors}</li>
 *     <li><a href="https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.7.9.1">Type Signatures</a> -> {@link dev.denwav.hypo.types.sig Hypo Type Signatures}</li>
 * </ul>
 *
 * <p>In addition to implementations for type models, this module provides an API for comparing, matching, and extracting
 * values from types using {@link dev.denwav.hypo.types.pattern patterns}.
 */
package dev.denwav.hypo.types;
