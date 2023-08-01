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
import org.jetbrains.annotations.Nullable;

/**
 * This class represents a lambda expression in Java code. The meaning of "closure" in this class name refers
 * to a construct in Java code which implicitly captures local variables from the method which contains the construct.
 *
 * <p>This class requires a hydrator to determine where it should be placed corresponding to the code being analyzed. Use
 * {@link HypoHydration#LAMBDA_CALLS}.
 */
public final class LambdaClosure {

    private final @NotNull MethodData containingMethod;
    private final @Nullable MethodData interfaceMethod;
    private final @NotNull MethodData lambda;
    private final int @NotNull [] paramLvtIndices;

    /**
     * Creates a new {@link LambdaClosure}.
     * @param containingMethod The method that contains the lambda expression.
     * @param interfaceMethod The method the lambda expression is implementing.
     * @param lambda The method which is generated as the implementation of the lambda.
     * @param paramLvtIndices The local variable indices from the containing method which are captured by the lambda expression.
     */
    public LambdaClosure(
        final @NotNull MethodData containingMethod,
        final @Nullable MethodData interfaceMethod,
        final @NotNull MethodData lambda,
        final int @NotNull [] paramLvtIndices
    ) {
        this.containingMethod = containingMethod;
        this.interfaceMethod = interfaceMethod;
        this.lambda = lambda;
        this.paramLvtIndices = paramLvtIndices;
    }

    /**
     * Returns the method which contains the lambda expression.
     * @return The method which contains the lambda expression.
     */
    public @NotNull MethodData getContainingMethod() {
        return this.containingMethod;
    }


    /**
     * Returns the single abstract method on the functional interface the {@link #getLambda() lambda} expression implements.
     * @return Tthe single abstract method on the functional interface the {@link #getLambda() lambda} expression implements.
     */
    public @Nullable MethodData getInterfaceMethod() {
        return this.interfaceMethod;
    }

    /**
     * Returns the lambda expression itself. This is the synthetic method which is generated which contains the implementation
     * of the lambda method.
     * @return The lambda expression itself.
     */
    public @NotNull MethodData getLambda() {
        return this.lambda;
    }

    /**
     * Returns the local variable table slots that the {@link #getLambda() lambda} expression captures.
     * @return The local variable table slots that the {@link #getLambda() lambda} expression captures.
     */
    public int @NotNull [] getParamLvtIndices() {
        return this.paramLvtIndices;
    }

    @Override
    public String toString() {
        return "LambdaClosure{" +
            "containingMethod=" + this.containingMethod +
            ", interfaceMethod=" + this.interfaceMethod +
            ", lambda=" + this.lambda +
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
        final LambdaClosure that = (LambdaClosure) o;
        return Objects.equals(this.containingMethod, that.containingMethod)
            && Objects.equals(this.interfaceMethod, that.interfaceMethod)
            && Objects.equals(this.lambda, that.lambda)
            && Arrays.equals(this.paramLvtIndices, that.paramLvtIndices);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(this.containingMethod, this.interfaceMethod, this.lambda);
        result = 31 * result + Arrays.hashCode(this.paramLvtIndices);
        return result;
    }
}
