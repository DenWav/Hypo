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

package dev.denwav.hypo.model.data;

import java.util.Objects;

/**
 * Base abstract implementation of {@link ConstructorData}. This class implements {@link HypoData} by extending
 * {@link AbstractHypoData} and implements the standard {@link #equals(Object)}, {@link #hashCode()}, and
 * {@link #toString()} methods to match the contract specified in {@link ConstructorData}.
 */
public abstract class AbstractConstructorData extends AbstractHypoData implements ConstructorData {

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof ConstructorData)) return false;
        final ConstructorData that = (ConstructorData) o;
        return this.parentClass().equals(that.parentClass()) && this.descriptor().equals(that.descriptor());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.parentClass(), this.descriptor());
    }

    @Override
    public String toString() {
        return this.parentClass().name() + "#" + this.name() + " " + this.descriptor();
    }
}
