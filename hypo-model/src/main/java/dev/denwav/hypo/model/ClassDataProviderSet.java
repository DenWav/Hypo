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

package dev.denwav.hypo.model;

import dev.denwav.hypo.model.data.ClassData;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A {@link ClassDataProvider} which wraps multiple providers. This class simple delegates all work to the collection
 * of providers passed to {@link #wrap(Collection)}. This provider closes all wrapped providers when its
 * {@link #close()} method is subsequently called.
 */
public class ClassDataProviderSet implements ClassDataProvider {

    private final @NotNull List<? extends @NotNull ClassDataProvider> delegateProviders;

    private ClassDataProviderSet(final @NotNull List<? extends @NotNull ClassDataProvider> delegateProviders) {
        this.delegateProviders = delegateProviders;
    }

    /**
     * Wrap the given collection of providers into a single {@link ClassDataProvider}. The given collection will be
     * copied to prevent modification of the given collection from affecting this set.
     *
     * @param providers The collection of providers to wrap into a single {@link ClassDataProvider}.
     * @return A new {@link ClassDataProvider} which wraps all of the given providers into a single provider.
     */
    @Contract("_ -> new")
    public static ClassDataProviderSet wrap(
        final @NotNull Collection<? extends @NotNull ClassDataProvider> providers
    ) {
        return new ClassDataProviderSet(HypoModelUtil.asImmutableList(providers));
    }

    @Override
    public void setDecorator(final @NotNull ClassDataDecorator decorator) {
        for (final ClassDataProvider prov : this.delegateProviders) {
            prov.setDecorator(decorator);
        }
    }

    @Override
    public void setContextClassProvider(boolean contextClassProvider) {
        for (final ClassDataProvider delegateProvider : this.delegateProviders) {
            delegateProvider.setContextClassProvider(contextClassProvider);
        }
    }

    @Override
    public boolean isContextClassProvider() {
        boolean isContext = false;
        for (final ClassDataProvider delegateProvider : this.delegateProviders) {
            isContext |= delegateProvider.isContextClassProvider();
        }
        return isContext;
    }

    @Override
    public void setRequireFullClasspath(boolean requireFullClasspath) {
        for (final ClassDataProvider delegateProvider : this.delegateProviders) {
            delegateProvider.setRequireFullClasspath(requireFullClasspath);
        }
    }

    @Override
    public boolean isRequireFullClasspath() {
        boolean isRequireFullClasspath = false;
        for (final ClassDataProvider delegateProvider : this.delegateProviders) {
            isRequireFullClasspath |= delegateProvider.isRequireFullClasspath();
        }
        return isRequireFullClasspath;
    }

    @Override
    @Contract("null -> null")
    public @Nullable ClassData findClass(final @Nullable String className) throws IOException {
        for (final ClassDataProvider prov : this.delegateProviders) {
            final ClassData data = prov.findClass(className);
            if (data != null) {
                return data;
            }
        }
        return null;
    }

    @Override
    public @NotNull Stream<ClassData> stream() throws IOException {
        return this.delegateProviders.stream().flatMap(HypoModelUtil.wrapFunction(ClassDataProvider::stream));
    }

    @Override
    public void close() throws Exception {
        Exception thrown = null;
        for (final ClassDataProvider provider : this.delegateProviders) {
            try {
                provider.close();
            } catch (final Exception e) {
                thrown = HypoModelUtil.addSuppressed(thrown, e);
            }
        }

        if (thrown != null) {
            throw thrown;
        }
    }
}
