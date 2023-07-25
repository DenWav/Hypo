package dev.denwav.hypo.hydrate.generic;

import dev.denwav.hypo.model.data.MethodData;
import java.util.Arrays;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public final class MethodClosure<ContainedType> {

    public static final int[] EMPTY = new int[0];

    private final @NotNull MethodData containingMethod;
    private final @NotNull ContainedType closure;
    private final int @NotNull [] paramLvtIndices;

    public MethodClosure(
        final @NotNull MethodData containingMethod,
        final @NotNull ContainedType closure,
        final int @NotNull [] paramLvtIndices
    ) {
        this.containingMethod = containingMethod;
        this.closure = closure;
        this.paramLvtIndices = paramLvtIndices;
    }

    public @NotNull MethodData getContainingMethod() {
        return this.containingMethod;
    }

    public @NotNull ContainedType getClosure() {
        return this.closure;
    }

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
