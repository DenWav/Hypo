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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Generic implementation of {@link ClassProviderRoot.FileDataReference} for {@link Path} objects.
 */
final class PathFileDataReference implements ClassProviderRoot.FileDataReference {

    private final @NotNull String name;
    private final @NotNull Path path;

    /**
     * Constructor for {@link PathFileDataReference}.
     *
     * @param name The name of the class this is a reference to.
     * @param path The {@link Path} object referring to the class file.
     */
    PathFileDataReference(final @NotNull String name, final @NotNull Path path) {
        this.name = name;
        this.path = path;
    }

    @Override
    public @NotNull String name() {
        return this.name;
    }

    @Override
    public byte @Nullable [] readData() throws IOException {
        if (Files.notExists(this.path)) {
            return null;
        }
        return Files.readAllBytes(this.path);
    }
}
