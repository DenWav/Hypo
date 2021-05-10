/*
 * Hypo, an extensible and pluggable Java bytecode analytical model.
 *
 * Copyright (C) 2021  Kyle Wood (DenWav)
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

/**
 * The category of a class. This does not include Java 9+ module classes, as those aren't currently handled by Hypo.
 */
public enum ClassKind {
    /**
     * Standard concrete class.
     */
    CLASS,
    /**
     * Abstract class.
     */
    ABSTRACT_CLASS,
    /**
     * Interface.
     */
    INTERFACE,
    /**
     * Enum class.
     */
    ENUM,
    /**
     * Annotation class.
     */
    ANNOTATION
}
