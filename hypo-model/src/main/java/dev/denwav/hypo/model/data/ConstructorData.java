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
 * Java constructor model. This is a specialization of {@link MethodData} for constructors, as constructors all share
 * a set of common properties.
 *
 * <p>{@link Object#equals(Object) equals()}, {@link Object#hashCode() hashCode()}, and
 * {@link Object#toString() toString()} are all written purely against the {@link #name()} of the parent class and this
 * constructor's descriptor. This is to prevent poor performance from using {@link ConstructorData} objects directly in
 * data structures.
 */
public interface ConstructorData extends MethodData {

    @Override
    default @NotNull String name() {
        return "<init>";
    }

    @Override
    default boolean isConstructor() {
        return true;
    }

    // Methods which don't apply to constructors
    @Override
    default boolean isStatic() {
        return false;
    }
    @Override
    default boolean isAbstract() {
        return false;
    }
    @Override
    default boolean isFinal() {
        return false;
    }
    @Override
    default boolean isBridge() {
        return false;
    }
    @Override
    default boolean isNative() {
        return false;
    }
}
