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
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Context class which stores capture data for a type match while it is executing. This API is not intended to be used
 * directly, it is used by {@link TypePattern}.
 */
@ApiStatus.Internal
public final class TypeMatchContext {

    private @Nullable Map<String, TypeRepresentable> capturedTypes;

    /**
     * Create a new instance of {@link TypeMatchContext}.
     */
    /* package */ TypeMatchContext() {}

    private @NotNull Map<String, TypeRepresentable> capturedTypes() {
        Map<String, TypeRepresentable> c = this.capturedTypes;
        if (c != null) {
            return c;
        }

        synchronized (this) {
            c = this.capturedTypes;
            if (c != null) {
                return c;
            }

            this.capturedTypes = c = new HashMap<>();
            return c;
        }
    }

    /**
     * Register a new captured object, associating it with {@code name}.
     *
     * @param name The name of the captured type object.
     * @param type The type object that is to be captured.
     */
    public void capture(final @NotNull String name, final @NotNull TypeRepresentable type) {
        this.capturedTypes().put(name, type);
    }

    /**
     * Get the map of captured type objects.
     * @return The map of captured type objects.
     */
    /* package */ @Nullable Map<String, TypeRepresentable> getCapturedTypes() {
        return this.capturedTypes;
    }
}
