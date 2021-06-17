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

/**
 * Generic unchecked exception for Hypo execution errors.
 */
public class HypoException extends RuntimeException {

    /**
     * Constructor for {@link HypoException} with a message.
     *
     * @param message The message for this exception.
     */
    public HypoException(String message) {
        super(message);
    }

    /**
     * Constructor for {@link HypoException} with a message and a cause.
     *
     * @param message The message for this exception.
     * @param cause The cause of this exception.
     */
    public HypoException(String message, Throwable cause) {
        super(message, cause);
    }
}
