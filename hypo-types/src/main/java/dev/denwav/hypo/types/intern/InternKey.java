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

package dev.denwav.hypo.types.intern;

import org.jetbrains.annotations.NotNull;

/**
 * Classes which can be interned using {@link Intern}.
 */
public interface InternKey {
    /**
     * A string key to uniquely identify this object for interning. Interned
     * objects are {@code value} types, which is to mean they have no
     * (useful<sup>1</sup>) identity. This rule means implementations of this
     * method must guarantee whenever a string returned from this method is equal
     * to another instance, they are always completely interchangeable.
     *
     * <blockquote><p><sup>1</sup> All Java objects contain an identity simply due to the
     * specification of classes in the JVM. The concept here of a "useful"
     * identity simply means that identity has no actual use.</blockquote>
     *
     * @return A string key used to uniquely identify this object for interning.
     */
    @NotNull String internKey();
}
