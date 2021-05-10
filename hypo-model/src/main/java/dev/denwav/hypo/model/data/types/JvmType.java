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

package dev.denwav.hypo.model.data.types;

import com.google.errorprone.annotations.Immutable;
import org.jetbrains.annotations.NotNull;

/**
 * A type in the Java language. There are 3 general categories of types in the JVM:
 * <ul>
 *     <li>{@link PrimitiveType Primitive types}</li>
 *     <li>{@link ClassType Class types}</li>
 *     <li>{@link ArrayType Array types}</li>
 * </ul>
 */
@Immutable
public interface JvmType {

    /**
     * Print this type name as a human-readable name to the given {@link StringBuilder}.
     *
     * @param sb The {@link StringBuilder} to print this type's human-readable name to.
     */
    void asReadableName(final @NotNull StringBuilder sb);

    /**
     * Print this type name as an internal JVM name to the given {@link StringBuilder}.
     *
     * @param sb The {@link StringBuilder} to print this type's internal JVM name to.
     */
    void asInternalName(final @NotNull StringBuilder sb);

    /**
     * Returns this type's human-readable name.
     * @return This type's human-readable name.
     */
    default @NotNull String asReadableName() {
        final StringBuilder sb = new StringBuilder();
        this.asReadableName(sb);
        return sb.toString();
    }

    /**
     * Returns this type's internal JVM name.
     * @return This type's internal JVM name.
     */
    default @NotNull String asInternalName() {
        final StringBuilder sb = new StringBuilder();
        this.asInternalName(sb);
        return sb.toString();
    }
}
