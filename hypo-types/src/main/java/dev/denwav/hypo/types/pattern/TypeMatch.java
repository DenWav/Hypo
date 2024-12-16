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

package dev.denwav.hypo.types.pattern;

import dev.denwav.hypo.types.TypeRepresentable;
import java.util.Map;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The result of a {@link TypePattern#match(TypeRepresentable) TypePattern match test}. Contains two distinct values:
 * the {@link #matches() match state} and the set of {@link #captures() captures}. {@link #get(String)} and
 * {@link #getOrNull(String)} can be used to directly retrieve named captures.
 *
 * <p>When using anonymous captures retrieve the value using the {@link TypeCapture#get(TypeMatch)} and
 * {@link TypeCapture#getOrNull(TypeMatch)} methods on the {@link TypeCapture} class.
 *
 * @param matches {@code true} if the type pattern matched, {@code false} otherwise.
 * @param captures The map of captures and their associated type objects.
 */
public record TypeMatch(
    boolean matches,
    @NotNull Map<String, TypeRepresentable> captures
) {

    /**
     * Constructor for {@link TypeMatch}. {@code captures} may be {@code null}, which results in this {@link TypeMatch}
     * containing an empty map of captures.
     *
     * @param matches Whether the match succeeded.
     * @param captures The map of captures and their associated type objects.
     */
    public TypeMatch(boolean matches, @Nullable Map<String, TypeRepresentable> captures) {
        this.matches = matches;
        this.captures = captures != null ? Map.copyOf(captures) : Map.of();
    }

    /**
     * {@code true} if the type pattern matched successfully.
     * @return {@code true} if the type pattern matched successfully.
     */
    @Override
    public boolean matches() {
        return this.matches;
    }

    /**
     * The map of captures and their associated type objects. The key of the map is the name of the capture, which will
     * be a random string when using anonymous captures.
     *
     * @return The map of captures and their associated type objects.
     */
    @Override
    public @NotNull Map<String, TypeRepresentable> captures() {
        return this.captures;
    }

    /**
     * Retrieve the named capture from this match. This method will throw a {@link NullPointerException} if the named
     * capture does not exist in this match.
     *
     * @param name The name of the capture.
     * @return The type object associated with the named capture.
     */
    public @NotNull TypeRepresentable get(final String name) {
        return Objects.requireNonNull(this.getOrNull(name), "No capture registered for name: " + name);
    }

    /**
     * Retrieve the named capture from this match, or {@code null} if no capture by the given name exists.
     *
     * @param name The name of the capture.
     * @return The type object associated with the named capture, or {@code null} if it does not exist.
     */
    public @Nullable TypeRepresentable getOrNull(final String name) {
        return this.captures.get(name);
    }
}
