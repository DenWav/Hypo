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

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Base abstract implementation of {@link MethodData}. This class implements {@link HypoData} by extending
 * {@link AbstractHypoData} and implements methods used for hydration, as well as the standard {@link #equals(Object)},
 * {@link #hashCode()}, and {@link #toString()} methods to match the contract specified in {@link MethodData}.
 */
public abstract class AbstractMethodData extends AbstractHypoData implements MethodData {

    private final @NotNull AtomicReference<@Nullable MethodData> superMethod = new AtomicReference<>(null);
    private final @NotNull Set<MethodData> childMethods = new LinkedHashSet<>();

    @Override
    public void setSuperMethod(final @Nullable MethodData superMethod) {
        this.superMethod.compareAndSet(null, superMethod);
    }

    @Override
    public @Nullable MethodData superMethod() {
        return this.superMethod.get();
    }

    @Override
    public @NotNull Set<MethodData> childMethods() {
        return this.childMethods;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof MethodData)) return false;
        final MethodData that = (MethodData) o;
        return this.parentClass().equals(that.parentClass()) &&
            this.name().equals(that.name()) &&
            this.descriptor().equals(that.descriptor());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.parentClass(), this.name(), this.descriptor());
    }

    @Override
    public String toString() {
        return this.parentClass().name() + "#" + this.name() + " " + this.descriptor();
    }
}
