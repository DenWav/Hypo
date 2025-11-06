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

package dev.denwav.hypo.model.data;

import dev.denwav.hypo.types.desc.TypeDescriptor;
import dev.denwav.hypo.types.sig.TypeSignature;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Java field model.
 *
 * <p>{@link Object#equals(Object) equals()}, {@link Object#hashCode() hashCode()}, and
 * {@link Object#toString() toString()} are all written purely against the {@link #name()} of the parent class, this
 * field's name, and this field's type. This is to prevent poor performance from using {@link FieldData} objects
 * directly in data structures.
 */
public interface FieldData extends MemberData {

    /**
     * The type of this field data.
     *
     * @return The type of this field data.
     * @deprecated Use {@link #descriptor()}.
     */
    @Deprecated(since = "3.0.0")
    default @NotNull TypeDescriptor fieldType() {
        return this.descriptor();
    }

    /**
     * The type of this field data.
     *
     * @return The type of this field data.
     */
    @NotNull TypeDescriptor descriptor();

    /**
     * The descriptor text of this field data.
     *
     * @return The descriptor text of this field data.
     * @see #descriptor()
     */
    default @NotNull String descriptorText() {
        return this.descriptor().asInternal();
    }

    /**
     * The generic type signature of this field data.
     *
     * @return The type signature of this field data.
     */
    @Nullable TypeSignature signature();

    /**
     * The internal JVM text representation of this field's generic type signature.
     *
     * @return The internal JVM text representation of this field's generic type signature.
     * @see #signature()
     */
    default @Nullable String signatureText() {
        final TypeSignature sig = this.signature();
        if (sig != null) {
            return sig.asInternal();
        } else {
            return null;
        }
    }
}
