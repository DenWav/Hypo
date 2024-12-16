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

package dev.denwav.hypo.model.data;

import java.util.Objects;

/**
 * Base abstract implementation of {@link FieldData}. This class implements {@link HypoData} by extending
 * {@link AbstractHypoData}, and implements the standard {@link #equals(Object)}, {@link #hashCode()}, and
 * {@link #toString()} methods to match the contract specified in {@link FieldData}.
 */
public abstract class AbstractFieldData extends AbstractHypoData implements FieldData {

    /**
     * Default constructor.
     */
    public AbstractFieldData() {}

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof final FieldData that)) {
            return false;
        }
        return this.parentClass().equals(that.parentClass()) &&
            this.name().equals(that.name()) &&
            this.descriptor().equals(that.descriptor()) &&
            Objects.equals(this.signature(), that.signature());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.parentClass(), this.name(), this.descriptor(), this.signature());
    }

    @Override
    public String toString() {
        return this.parentClass().name() + "#" + this.name() + " " + this.descriptor();
    }
}
