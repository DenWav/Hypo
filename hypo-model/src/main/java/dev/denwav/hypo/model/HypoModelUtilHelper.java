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

import java.util.Collection;
import java.util.List;
import org.jetbrains.annotations.NotNull;

/**
 * Helper class for {@link HypoModelUtil}, with specializations for the currently running version of Java. Access the
 * current instance which is compatible with the currently running JVM with {@link #INSTANCE}.
 */
abstract class HypoModelUtilHelper {

    /**
     * The specialization instance of this class compatible with the currently running JVM.
     */
    public static final HypoModelUtilHelper INSTANCE;

    static {
        HypoModelUtilHelper i;
        try {
            i = Class.forName(HypoModelUtilHelper.class.getName() + "Jdk10")
                .asSubclass(HypoModelUtilHelper.class).getDeclaredConstructor().newInstance();
        } catch (final Throwable t) {
            try {
                i = Class.forName(HypoModelUtilHelper.class.getName() + "Jdk8")
                    .asSubclass(HypoModelUtilHelper.class).getDeclaredConstructor().newInstance();
            } catch (Throwable t2) {
                throw HypoModelUtil.rethrow(HypoModelUtil.addSuppressed(t, t2));
            }
        }

        INSTANCE = i;
    }

    /**
     * Create a copy of the given list and return it as an immutable list.
     *
     * @param list The list to copy as immutable.
     * @param <T> The type param of the list.
     * @return The new immutable list.
     */
    abstract <T> @NotNull List<T> asImmutableList(final @NotNull Collection<T> list);

    /**
     * Create an immutable list from the given array.
     *
     * @param array The arry to return as an immutable list.
     * @return The new immutable list.
     * @param <T> The type param of the list.
     */
    @SuppressWarnings("unchecked")
    abstract <T> @NotNull List<T> immutableListOf(final @NotNull T @NotNull ... array);
}
