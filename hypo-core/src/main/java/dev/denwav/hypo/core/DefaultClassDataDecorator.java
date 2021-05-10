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

package dev.denwav.hypo.core;

import dev.denwav.hypo.model.ClassDataDecorator;
import dev.denwav.hypo.model.ClassDataProvider;
import dev.denwav.hypo.model.data.ClassData;
import org.jetbrains.annotations.NotNull;

/**
 * The default implementation of {@link ClassDataDecorator}. The only thing this does is set the provider for the
 * {@link ClassData} object based on the {@link ClassDataProvider provider} passed to this class in the constructor.
 */
public class DefaultClassDataDecorator implements ClassDataDecorator {

    private final @NotNull ClassDataProvider provider;

    /**
     * Create a new instance of this class which will pass the given provider on to each {@link ClassData} object's
     * {@link ClassData#setProvider(ClassDataProvider)} method.
     *
     * @param provider The provider to pass on to each {@link ClassData} object.
     */
    public DefaultClassDataDecorator(final @NotNull ClassDataProvider provider) {
        this.provider = provider;
    }

    @Override
    public void decorate(final @NotNull ClassData classData) {
        classData.setProvider(this.provider);
    }
}
