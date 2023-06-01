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

package dev.denwav.hypo.model;

import dev.denwav.hypo.model.data.ClassData;
import org.jetbrains.annotations.NotNull;

/**
 * This class sets additional data on a {@link ClassData} object immediately after it has been created. The minimum
 * requirement for a class implementing this interface is that it must set the {@link ClassDataProvider provider} the
 * {@link ClassData} object should use for class lookups using {@link ClassData#setProvider(ClassDataProvider)}.
 *
 * <p>A {@link ClassData} object which is not decorated will not have a provider set for resolving other classes, so
 * they are not considered fully created until after the decorator sets that value on the {@link ClassData} file.
 */
public interface ClassDataDecorator {

    /**
     * Set any additional data on the {@link ClassData} object immediately after it has been created.
     *
     * @param classData The {@link ClassData} object to set up.
     */
    void decorate(final @NotNull ClassData classData);
}
