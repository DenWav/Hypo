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
import dev.denwav.hypo.types.desc.MethodDescriptor;
import dev.denwav.hypo.types.sig.MethodSignature;
import java.util.List;
import org.jetbrains.annotations.NotNull;

/**
 * MethodType is a marker interface for {@link MethodDescriptor} and {@link MethodSignature}.
 */
public sealed interface MethodType
    extends TypeRepresentable
    permits MethodDescriptor, MethodSignature {

    /**
     * Get the list of parameter types for this method type. The returned list is immutable.
     *
     * @return The list of parameter types for this method type.
     */
    @NotNull List<? extends ValueType> getParameters();

    /**
     * Get the return type for this method type.
     *
     * @return The return type for this method type.
     */
    @NotNull ValueType getReturnType();
}
