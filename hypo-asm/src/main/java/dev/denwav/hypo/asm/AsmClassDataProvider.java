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

package dev.denwav.hypo.asm;

import dev.denwav.hypo.model.AbstractClassDataProvider;
import dev.denwav.hypo.model.ClassDataProvider;
import dev.denwav.hypo.model.ClassProviderRoot;
import dev.denwav.hypo.model.data.ClassData;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Implementation of {@link ClassDataProvider} based on the {@code asm} library. {@link AsmClassData} is the
 * implementation of {@link ClassData} this provider produces.
 */
public class AsmClassDataProvider extends AbstractClassDataProvider implements ClassDataProvider {

    private AsmClassDataProvider(final @NotNull List<@NotNull ClassProviderRoot> rootProviders) {
        super(rootProviders);
    }

    /**
     * Create a new instance of {@link AsmClassDataProvider} using the given {@link ClassProviderRoot root}.
     *
     * @param root The root to use to resolve Java class files.
     * @return The new {@link AsmClassDataProvider}.
     */
    @Contract(value = "_ -> new", pure = true)
    public static @NotNull AsmClassDataProvider of(final @NotNull ClassProviderRoot root) {
        return new AsmClassDataProvider(Collections.singletonList(root));
    }

    /**
     * Create a new instance of {@link AsmClassDataProvider} using the given {@link ClassProviderRoot roots}.
     *
     * @param roots The list of roots to use to resolve Java class files.
     * @return The new {@link AsmClassDataProvider}.
     */
    @Contract(value = "_ -> new", pure = true)
    public static @NotNull AsmClassDataProvider of(final @NotNull List<@NotNull ClassProviderRoot> roots) {
        return new AsmClassDataProvider(roots);
    }

    @Override
    protected @Nullable ClassData parseClassData(final byte @NotNull [] file) {
        return AsmClassData.readFile(file);
    }
}
