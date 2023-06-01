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

import org.jetbrains.annotations.NotNull;

/**
 * Java member model. Java members include {@link FieldData fields} and {@link MethodData methods}, other classes are
 * not considered members.
 */
public interface MemberData extends HypoData {

    /**
     * Get the {@link Visibility visibility} of this member data.
     *
     * @return The visibility of this member data.
     * @see Visibility
     */
    @NotNull Visibility visibility();

    /**
     * The name of this member data, this only includes the member's identifier, no type info or descriptors are
     * included.
     *
     * @return The name of this member data.
     */
    @NotNull String name();

    /**
     * The {@link ClassData class} which declares this member.
     *
     * @return The {@link ClassData class} which declares this member.
     */
    @NotNull ClassData parentClass();

    /**
     * Returns {@code true} if this member data is {@code static}.
     *
     * @return {@code true} if this member data is {@code static}.
     */
    boolean isStatic();
    /**
     * Returns {@code true} if this member data is {@code final}.
     *
     * @return {@code true} if this member data is a {@code final} member.
     */
    boolean isFinal();

    /**
     * Returns {@code true} if this member data is synthetic.
     *
     * @return {@code true} if this member data is a synthetic member.
     */
    boolean isSynthetic();
}
