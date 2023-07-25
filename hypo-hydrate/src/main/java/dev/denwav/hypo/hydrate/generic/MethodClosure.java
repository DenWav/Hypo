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

import dev.denwav.hypo.model.data.MethodData;
import java.util.Arrays;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * This class represents a closure (or potential closure) in Java code. The meaning of "closure" in this case refers
 * to a construct in Java code which implicitly captures local variables from the method which contains the construct.
 *
 * <p>The most common example in modern Java is lambda expressions. Anonymous and local classes (classes defined entirely
 * inside a method) are the other examples.
 *
 * <p>This class requires a hydrator to determine where it should be placed corresponding to the code being analyzed. Use
 * {@link HypoHydration#LAMBDA_CALLS} and {@link HypoHydration#LOCAL_CLASSES}.
 *
 * @param <ContainedType> The type of the closure expression. It is either a {@link MethodData} for lambda expressions, or
 *                        a {@link dev.denwav.hypo.model.data.ClassData ClassData} for anonymous and local classes.
 */
public final class MethodClosure<ContainedType> {

    /**
     * For convenience, an empty {@code int[]} array. This can be used for {@link #getParamLvtIndices() paramLvtIndices} if
     * no variables are captured.
     */
    public static final int[] EMPTY_INT_ARRAY = new int[0];

    private final @NotNull MethodData containingMethod;
    private final @NotNull ContainedType closure;
    private final int @NotNull [] paramLvtIndices;

    /**
     * Constructor for creating a new {@link MethodClosure}.
     *
     * @param containingMethod The method which contains the closure expression.
     * @param closure The closure object, whether it is a class or a method.
     * @param paramLvtIndices The local variable indices from the containing method which are captured by the closure expression.
     */
    public MethodClosure(
        final @NotNull MethodData containingMethod,
        final @NotNull ContainedType closure,
        final int @NotNull [] paramLvtIndices
    ) {
        this.containingMethod = containingMethod;
        this.closure = closure;
        this.paramLvtIndices = paramLvtIndices;
    }

    /**
     * Returns the method which contains the closure expression.
     * @return The method which contains the closure expression.
     */
    public @NotNull MethodData getContainingMethod() {
        return this.containingMethod;
    }

    /**
     * Returns the closure expression itself. Can either be a {@link dev.denwav.hypo.model.data.ClassData ClassData} for anonymous and local classes,
     * or a {@link MethodData} for lambda expressions.
     * @return The closure expression itself.
     */
    public @NotNull ContainedType getClosure() {
        return this.closure;
    }

    /**
     * Returns the local variable table slots that the {@link #getClosure() closure} expression captures.
     * @return The local variable table slots that the {@link #getClosure() closure} expression captures.
     */
    public int @NotNull [] getParamLvtIndices() {
        return this.paramLvtIndices;
    }

    @Override
    public String toString() {
        return "LambdaCall{" +
            "containingMethod=" + this.containingMethod +
            ", closure=" + this.closure +
            ", paramLvtIndices=" + Arrays.toString(this.paramLvtIndices) +
            '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        final MethodClosure<?> that = (MethodClosure<?>) o;
        return Objects.equals(this.containingMethod, that.containingMethod)
            && Objects.equals(this.closure, that.closure)
            && Arrays.equals(this.paramLvtIndices, that.paramLvtIndices);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(this.containingMethod, this.closure);
        result = 31 * result + Arrays.hashCode(this.paramLvtIndices);
        return result;
    }
}
