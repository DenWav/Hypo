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

package dev.denwav.hypo.types.sig;

import com.google.errorprone.annotations.Immutable;
import dev.denwav.hypo.types.TypeBindable;
import dev.denwav.hypo.types.TypeRepresentable;
import dev.denwav.hypo.types.TypeVariableBinder;
import dev.denwav.hypo.types.sig.param.TypeArgument;
import dev.denwav.hypo.types.sig.param.TypeVariable;
import org.jetbrains.annotations.NotNull;

/**
 * A reference type signature is a subset of {@link TypeSignature} which does not include
 * {@link dev.denwav.hypo.types.PrimitiveType primitive types} or {@link dev.denwav.hypo.types.VoidType void}. This is
 * simply a marker interface to denote those types.
 */
@Immutable
public sealed interface ReferenceTypeSignature
    extends TypeBindable, TypeSignature, TypeArgument, TypeRepresentable
    permits ClassTypeSignature, ArrayTypeSignature, TypeVariable, TypeVariable.Unbound {

    @Override
    @NotNull ReferenceTypeSignature bind(final @NotNull TypeVariableBinder binder);
}
