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

import dev.denwav.hypo.types.desc.TypeDescriptor;
import dev.denwav.hypo.types.sig.TypeSignature;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Base implementation of {@link FieldData} which lazily retrieves and caches the field type.
 */
public abstract class LazyFieldData extends AbstractFieldData {

    /**
     * Default constructor.
     */
    public LazyFieldData() {}

    /**
     * {@code compute} variant of {@link #descriptor()}.
     *
     * @return This field's descriptor.
     */
    public abstract @NotNull TypeDescriptor computeDescriptor();

    /**
     * {@code compute} variant of {@link #signature()}.
     *
     * @return This field's generic type signature.
     */
    public abstract @Nullable TypeSignature computeSignature();

    @SuppressWarnings("this-escape")
    private final @NotNull LazyValue<TypeDescriptor, ?> descriptor = LazyValue.of(this::computeDescriptor);
    @Override
    public @NotNull TypeDescriptor descriptor() {
        return this.descriptor.getNotNull();
    }

    @SuppressWarnings("this-escape")
    private final @NotNull LazyValue<TypeSignature, ?> signature = LazyValue.of(this::computeSignature);
    @Override
    public @Nullable TypeSignature signature() {
        return this.signature.get();
    }
}
