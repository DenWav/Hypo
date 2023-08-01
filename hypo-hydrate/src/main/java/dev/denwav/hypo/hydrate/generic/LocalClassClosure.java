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

package dev.denwav.hypo.hydrate.generic;

import dev.denwav.hypo.model.data.ClassData;
import dev.denwav.hypo.model.data.MethodData;
import java.util.Arrays;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * This class represents a local class in Java code. The meaning of "closure" in this class name refers
 * to a construct in Java code which implicitly captures local variables from the method which contains the construct.
 *
 * <p>This class requires a hydrator to determine where it should be placed corresponding to the code being analyzed. Use
 * {@link HypoHydration#LOCAL_CLASSES}.
 */
public final class LocalClassClosure {

    private final @NotNull MethodData containingMethod;
    private final @NotNull ClassData localClass;
    private final int @NotNull [] paramLvtIndices;

    /**
     * Creates a new {@link LocalClassClosure}.
     * @param containingMethod The method that contains the local class.
     * @param localClass The local class which is contained in the method.
     * @param paramLvtIndices The local variable indices from the containing method which are captured by the local class.
     */
    public LocalClassClosure(
        final @NotNull MethodData containingMethod,
        final @NotNull ClassData localClass,
        final int @NotNull [] paramLvtIndices
    ) {
        this.containingMethod = containingMethod;
        this.localClass = localClass;
        this.paramLvtIndices = paramLvtIndices;
    }

    /**
     * Returns the method which contains the local class.
     * @return The method which contains the local class.
     */
    public @NotNull MethodData getContainingMethod() {
        return this.containingMethod;
    }

    /**
     * Returns the local class itself.
     * @return The local class itself.
     */
    public @NotNull ClassData getLocalClass() {
        return this.localClass;
    }

    /**
     * Returns the local variable table slots that the {@link #getLocalClass() local class} captures.
     * @return The local variable table slots that the {@link #getLocalClass() local class} captures.
     */
    public int @NotNull [] getParamLvtIndices() {
        return this.paramLvtIndices;
    }

    @Override
    public String toString() {
        return "LocalClassClosure{" +
            "containingMethod=" + this.containingMethod +
            ", localClass=" + this.localClass +
            ", paramLvtIndices=" + Arrays.toString(this.paramLvtIndices) +
            '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final LocalClassClosure that = (LocalClassClosure) o;
        return Objects.equals(this.containingMethod, that.containingMethod)
            && Objects.equals(this.localClass, that.localClass)
            && Arrays.equals(this.paramLvtIndices, that.paramLvtIndices);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(this.containingMethod, this.localClass);
        result = 31 * result + Arrays.hashCode(this.paramLvtIndices);
        return result;
    }
}
