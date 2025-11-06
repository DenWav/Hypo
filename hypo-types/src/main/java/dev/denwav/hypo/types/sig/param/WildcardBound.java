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

package dev.denwav.hypo.types.sig.param;

import dev.denwav.hypo.types.TypeRepresentable;
import org.jetbrains.annotations.NotNull;

/**
 * The bound kind for a bounded wildcard type. This enum is used by {@link BoundedTypeArgument}.
 */
public enum WildcardBound implements TypeRepresentable {
    /**
     * The singleton instance of the upper bound, represented in code as {@code ? extends}.
     */
    UPPER("? extends", '+'),
    /**
     * The singleton instance of the lower bound, represented in code as {@code ? super}.
     */
    LOWER("? super", '-'),
    ;

    private final @NotNull String readable;
    private final char internal;

    WildcardBound(final @NotNull String readable, final char internal) {
        this.readable = readable;
        this.internal = internal;
    }

    @Override
    public void asReadable(final @NotNull StringBuilder sb) {
        sb.append(this.readable);
    }

    @Override
    public void asInternal(final @NotNull StringBuilder sb) {
        sb.append(this.internal);
    }

    @Override
    public String toString() {
        return this.asReadable();
    }
}
