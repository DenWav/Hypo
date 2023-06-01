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

package dev.denwav.hypo.mappings.contributors;

import dev.denwav.hypo.core.HypoContext;
import dev.denwav.hypo.mappings.ChangeRegistry;
import dev.denwav.hypo.model.HypoModelUtil;
import dev.denwav.hypo.model.data.ClassData;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.cadixdev.lorenz.model.ClassMapping;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A {@link ChangeContributor} which wraps multiple contributors. This class simply delegates all work to the collection
 * of contributors passed to {@link #wrap(Collection)}.
 */
public class ChangeContributorSet implements ChangeContributor {

    private final @NotNull List<@NotNull ChangeContributor> delegateContributors;

    private ChangeContributorSet(final @NotNull List<@NotNull ChangeContributor> delegateContributors) {
        this.delegateContributors = delegateContributors;
    }

    /**
     * Wrap the given collection of contributors into a single {@link ChangeContributor}. The given collection will be
     * copied to prevent modification of the given collection from affecting this set.
     *
     * @param delegates The collection of contributors to wrap into a single {@link ChangeContributor}.
     * @return A new {@link ChangeContributor} which wraps all of the given contributors into a single contributor.
     */
    @Contract(value = "_ -> new", pure = true)
    public static @NotNull ChangeContributor wrap(final @NotNull Collection<@NotNull ChangeContributor> delegates) {
        return new ChangeContributorSet(HypoModelUtil.asImmutableList(delegates));
    }

    @Override
    public void contribute(
        final @Nullable ClassData currentClass,
        final @Nullable ClassMapping<?, ?> classMapping,
        final @NotNull HypoContext context,
        final @NotNull ChangeRegistry registry
    ) throws Throwable {
        for (final ChangeContributor delegate : this.delegateContributors) {
            delegate.contribute(currentClass, classMapping, context, registry);
        }
    }

    @Override
    public @NotNull String name() {
        if (this.delegateContributors.size() == 1) {
            return this.delegateContributors.get(0).name();
        } else {
            return this.delegateContributors.stream()
                .map(ChangeContributor::name)
                .collect(Collectors.joining(", ", "Set[", "]"));
        }
    }
}
