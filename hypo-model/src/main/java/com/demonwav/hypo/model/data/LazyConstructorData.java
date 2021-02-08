/*
 * Hypo, an extensible and pluggable Java bytecode analytical model.
 *
 * Copyright (C) 2021  Kyle Wood (DemonWav)
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

package com.demonwav.hypo.model.data;

import org.jetbrains.annotations.NotNull;

/**
 * Base implementation of {@link ConstructorData} which lazily retrieves and caches the method descriptor.
 */
public abstract class LazyConstructorData extends AbstractConstructorData {

    /**
     * {@code compute} variant of {@link #descriptor()}.
     *
     * @return This constructor's descriptor.
     */

    public abstract @NotNull MethodDescriptor computeDescriptor();

    private final @NotNull LazyValue<MethodDescriptor, ?> descriptor = LazyValue.of(this::computeDescriptor);

    @Override
    public @NotNull MethodDescriptor descriptor() {
        return this.descriptor.getNotNull();
    }
}
