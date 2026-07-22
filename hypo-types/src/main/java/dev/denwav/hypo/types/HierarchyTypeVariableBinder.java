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

package dev.denwav.hypo.types;

import dev.denwav.hypo.types.sig.Signature;
import dev.denwav.hypo.types.sig.TypeParameterHolder;
import dev.denwav.hypo.types.sig.param.TypeParameter;
import java.util.Arrays;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A {@link TypeVariableBinder} implementation that resolves type variables based on a hierarchy
 * of signatures (such as classes and methods).
 *
 * <p>Scoping rules apply based on the order of the signatures provided: parameters defined in
 * earlier signatures (typically inner scopes like methods) will shadow parameters defined in
 * later signatures (typically outer scopes like class definitions).
 */
public class HierarchyTypeVariableBinder implements TypeVariableBinder {

    private final @Nullable TypeParameterHolder root;
    private final @NotNull List<TypeParameterHolder> holders;
    private final @Nullable TypeVariableBinder delegate;

    private HierarchyTypeVariableBinder(
        final @Nullable TypeParameterHolder root,
        final @NotNull List<TypeParameterHolder> parameters,
        final @Nullable TypeVariableBinder delegate
    ) {
        this.root = root;
        this.holders = parameters;
        this.delegate = delegate;
    }

    /**
     * Create a new blank {@link HierarchyTypeVariableBinder}.
     *
     * @return A new {@link HierarchyTypeVariableBinder} instance.
     */
    public static @NotNull HierarchyTypeVariableBinder of() {
        return HierarchyTypeVariableBinder.of(null, List.of());
    }

    /**
     * Create a new {@link HierarchyTypeVariableBinder} from the given hierarchy of {@link TypeParameterHolder}s.
     *
     * @param root The root type parameter holder.
     * @param holders The type parameter holders forming the type hierarchy, ordered from innermost to outermost.
     * @return A new {@link HierarchyTypeVariableBinder} instance.
     */
    public static @NotNull HierarchyTypeVariableBinder of(
        final @Nullable TypeParameterHolder root,
        final @NotNull List<TypeParameterHolder> holders
    ) {
        return new HierarchyTypeVariableBinder(root, holders, null);
    }

    /**
     * Create a new {@link HierarchyTypeVariableBinder} from the given hierarchy of {@link TypeParameterHolder}s.
     *
     * @param root The root type parameter holder.
     * @param delegate The delegate type variable binder.
     * @return A new {@link HierarchyTypeVariableBinder} instance.
     */
    public static @NotNull HierarchyTypeVariableBinder of(
        final @Nullable TypeParameterHolder root,
        final @NotNull TypeVariableBinder delegate
    ) {
        return new HierarchyTypeVariableBinder(root, List.of(), delegate);
    }

    /**
     * Create a new {@link HierarchyTypeVariableBinder} from the given varargs array of {@link Signature}s.
     *
     * @param holders The type parameter holders forming the type hierarchy, ordered from innermost to outermost.
     * @return A new {@link HierarchyTypeVariableBinder} instance.
     */
    public static @NotNull HierarchyTypeVariableBinder of(final TypeParameterHolder @NotNull ... holders) {
        return switch (holders.length) {
            case 0 -> HierarchyTypeVariableBinder.of();
            case 1 -> HierarchyTypeVariableBinder.of(holders[0], List.of());
            default -> HierarchyTypeVariableBinder.of(holders[0], List.of(Arrays.copyOfRange(holders, 1, holders.length)));
        };
    }

    /**
     * Look up and return the first {@link TypeParameter} matching the given name.
     *
     * @param name The variable name.
     * @return The first matching {@link TypeParameter}, or {@code null} if not found.
     */
    @Override
    public @Nullable TypeParameter bindingFor(final @NotNull String name) {
        if (this.root != null) {
            for (final TypeParameter param : this.root.getTypeParameters()) {
                if (param.getName().equals(name)) {
                    return param;
                }
            }
        }
        for (final TypeParameterHolder holder : this.holders) {
            for (final TypeParameter param : holder.getTypeParameters()) {
                if (param.getName().equals(name)) {
                    return param;
                }
            }
        }
        if (this.delegate != null) {
            return this.delegate.bindingFor(name);
        }
        return null;
    }
}
