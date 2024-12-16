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

package dev.denwav.hypo.types;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * General utility class used by {@code hypo-types}.
 */
public final class HypoTypesUtil {

    private HypoTypesUtil() {}

    /**
     * Normalize the given class name into a standard format matching the JVM internal class name format. Class type
     * descriptors may also be passed in (that is, class names that looks like {@code Lcom/example/Main;}) and this
     * method will strip the initial {@code L} and the final {@code ;}.
     *
     * <p>This method does not validate the format of the class name provided, it simply runs a couple standard String
     * functions on it to help increase cache-hit rate and reduce the likelihood of a class name in a slightly different
     * format causing issues.
     *
     * @param className The class name to normalize.
     * @return The normalized class name.
     */
    public static @NotNull String normalizedClassName(final @NotNull String className) {
        final int index = className.endsWith(";") ? 1 : 0;
        return className.substring(index, className.length() - index).replace('.', '/');
    }

    /**
     * Perform an unsafe, unchecked cast to the type specified by {@link T}. This method does not in any way verify or
     * validate that the cast will succeed, nor does it protect from failed casts from occurring. This method's intended
     * purpose is for handing cases where Java's type system falls short, such as when handling capture types. Any code
     * which uses this method should independently verify that the cast will never fail on their own.
     *
     * @param o The object to cast to {@link T}. Can be {@code null}, if so then {@code null} will be returned.
     * @param <T> The type to cast {@code o} to.
     * @return The given parameter {@code o}, cast to {@link T}.
     */
    @SuppressWarnings("TypeParameterUnusedInFormals")
    @Contract(value = "_ -> param1", pure = true)
    public static <T> @Nullable T cast(final @Nullable Object o) {
        @SuppressWarnings("unchecked") final T t = (T) o;
        return t;
    }
}
