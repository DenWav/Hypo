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
 * This package represents the default implementation of the Hypo model, based on the
 * <a href="https://asm.ow2.io/">ASM</a> library.
 *
 * <p>This package provides an implementation of {@link com.demonwav.hypo.model.ClassDataProvider ClassDataProvider}
 * with {@link com.demonwav.hypo.asm.AsmClassDataProvider AsmClassDataProvider}, and implementations of each of the
 * components of the Hypo data model.
 */
package com.demonwav.hypo.asm;
