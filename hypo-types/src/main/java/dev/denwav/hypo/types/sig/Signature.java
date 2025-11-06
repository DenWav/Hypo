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

import dev.denwav.hypo.types.TypeRepresentable;

/**
 * Signature is a marker interface for {@link TypeSignature}, {@link MethodSignature}, and {@link ClassSignature}. It
 * can be used to differentiate a <i>signature</i> type from a <i>descriptor</i> type of a {@link TypeRepresentable}.
 *
 * @see TypeSignature
 * @see MethodSignature
 * @see ClassSignature
 * @see dev.denwav.hypo.types.desc.Descriptor Descriptor
 */
public sealed interface Signature extends TypeRepresentable
    permits TypeSignature, MethodSignature, ClassSignature {
}
