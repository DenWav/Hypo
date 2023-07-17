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

package dev.denwav.hypo.core;

import java.io.IOException;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Experimental API for writing changes to the model out. Hypo does not provide any API for the modification of the
 * model, but if modifications to the base are still possible, this API allows the use of that mechanism rather than
 * having to resort to an external system for writing changes.
 */
@ApiStatus.Experimental
public interface HypoOutputWriter {

    /**
     * Write out all data from the context according to how this output writer is configured.
     *
     * @param context The context to write.
     * @throws IOException If an IO error occurs while writing the data.
     */
    @ApiStatus.Experimental
    void write(final @NotNull HypoContext context) throws IOException;
}
