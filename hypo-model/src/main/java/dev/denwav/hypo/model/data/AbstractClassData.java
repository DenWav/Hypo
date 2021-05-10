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

import dev.denwav.hypo.model.ClassDataProvider;
import com.google.errorprone.annotations.concurrent.LazyInit;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Base abstract implementation of {@link ClassData}. This class implements {@link HypoData} by extending
 * {@link AbstractHypoData} and implements methods used for hydration, as well as the standard {@link #equals(Object)},
 * {@link #hashCode()}, and {@link #toString()} methods to match the contract specified in {@link ClassData}.
 */
public abstract class AbstractClassData extends AbstractHypoData implements ClassData {

    /**
     * The {@link ClassDataProvider provider} to use for class lookups. This field is lazily initialized, so it is
     * considered nullable but shouldn't be {@code null} in practice. Use {@link #prov()} instead to remove unnecessary
     * {@code null} checking.
     */
    @LazyInit protected @Nullable ClassDataProvider provider = null;

    private boolean isContextClass = false;

    private final @NotNull Set<@NotNull ClassData> childClasses = new LinkedHashSet<>();
    private final @NotNull Set<@NotNull ClassData> innerClasses = new LinkedHashSet<>();

    @Override
    public void setProvider(final @NotNull ClassDataProvider provider) {
        this.provider = provider;
    }

    @Override
    public void setContextClass(boolean contextClass) {
        this.isContextClass = contextClass;
    }

    @Override
    public boolean isContextClass() {
        return this.isContextClass;
    }

    @Override
    public @NotNull Set<@NotNull ClassData> childClasses() {
        return this.childClasses;
    }

    @Override
    public @NotNull Set<@NotNull ClassData> innerClasses() {
        return this.innerClasses;
    }

    /**
     * Return the {@link ClassDataProvider}, assuming it has already been set. This method will throw an exception if
     * it has not already been set.
     *
     * @return The non-null {@link ClassDataProvider}.
     */
    protected @NotNull ClassDataProvider prov() {
        if (this.provider == null) {
            throw new NullPointerException("ClassDataProvider not set on " + this.name());
        }
        return this.provider;
    }

    /*
     * equals()/hashCode() are primarily used for data structures (such as in hydration).
     * We don't want these calls to be expensive or to accidentally be recursive (accessing fields which end up
     * accessing back to `this` again).
     * The idea here is similar to how the JVM operates - the first class which matches a given name is loaded,
     * duplicates are either an error or ignored depending on the situation. Here they are ignored. All classes
     * with the same name should be considered the same class, as the name is what uniquely identifies the class.
     */

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof ClassData)) return false;
        final ClassData that = (ClassData) o;
        return this.name().equals(that.name());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name());
    }

    @Override
    public String toString() {
        return this.name();
    }
}
