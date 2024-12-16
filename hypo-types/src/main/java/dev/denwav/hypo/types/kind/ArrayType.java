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

package dev.denwav.hypo.types.kind;

import dev.denwav.hypo.types.TypeRepresentable;
import dev.denwav.hypo.types.desc.ArrayTypeDescriptor;
import dev.denwav.hypo.types.sig.ArrayTypeSignature;
import org.jetbrains.annotations.NotNull;

/**
 * ArrayKind is a marker interface for {@link ArrayTypeDescriptor} and {@link ArrayTypeSignature}.
 */
public sealed interface ArrayType
    extends TypeRepresentable
    permits ArrayTypeDescriptor, ArrayTypeSignature {

    /**
     * Get the dimension of this array type.
     * @return The dimension of this array type.
     */
    int getDimension();

    /**
     * Get the base type of this array type.
     * @return The base type of this array type.
     */
    @NotNull ValueType getBaseType();
}
