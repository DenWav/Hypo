/*
 * Hypo, an extensible and pluggable Java bytecode analytical model.
 *
 * Copyright (C) 2021  Kyle Wood (DemonWav)
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

package com.demonwav.hypo.model.data;

import com.demonwav.hypo.model.HypoModelUtil;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Default implementation of {@link HypoData}. Classes which extend from this class will automatically fully implement
 * {@link HypoData}.
 */
public abstract class AbstractHypoData implements HypoData {

    /**
     * Data store for this object. Must to synchronized as there is no non-locking concurrent identity hashmap.
     */
    private final @NotNull Map<HypoKey<?>, ?> hypoData = Collections.synchronizedMap(new IdentityHashMap<>());

    @Override
    public <T> @Nullable T store(final @NotNull HypoKey<T> key, final @Nullable T t) {
        if (t == null) {
            this.hypoData.remove(key);
        } else {
            this.hypoData.put(key, HypoModelUtil.cast(t));
        }
        return t;
    }

    @Override
    public <T> @NotNull T compute(final @NotNull HypoKey<T> key, final @NotNull Supplier<T> supplier) {
        final Object o = this.hypoData.computeIfAbsent(key, k -> HypoModelUtil.cast(supplier.get()));
        Objects.requireNonNull(o, "Result of supplier must not be null");
        return HypoModelUtil.cast(o);
    }

    @Override
    public <T> @Nullable T get(final @NotNull HypoKey<T> key) {
        return HypoModelUtil.cast(this.hypoData.get(key));
    }

    @Override
    public boolean contains(final @NotNull HypoKey<?> key) {
        return this.hypoData.containsKey(key);
    }
}
