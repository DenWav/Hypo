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

import dev.denwav.hypo.model.data.types.JvmType;
import dev.denwav.hypo.model.data.types.PrimitiveType;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Java method model.
 *
 * <p>{@link Object#equals(Object) equals()}, {@link Object#hashCode() hashCode()}, and
 * {@link Object#toString() toString()} are all written purely against the {@link #name()} of the parent class, this
 * method's name, and this method's descriptor. This is to prevent poor performance from using {@link MethodData}
 * objects directly in data structures.
 */
public interface MethodData extends MemberData {

    /**
     * The descriptor of this method. This does not include the method's generic signature, this is only the method's
     * basic type descriptor.
     *
     * @return This method's descriptor.
     */
    @NotNull MethodDescriptor descriptor();

    /**
     * The internal JVM text representation of this method's descriptor.
     *
     * @return The internal JVM text representation of this method's descriptor.
     * @see #descriptor()
     */
    default @NotNull String descriptorText() {
        return this.descriptor().toInternalString();
    }

    /**
     * Returns {@code true} if this method is abstract.
     * @return {@code true} if this method is abstract.
     */
    boolean isAbstract();

    /**
     * Returns {@code true} if this is a bridge method.
     * @return {@code true} if this is a bridge method.
     */
    boolean isBridge();

    /**
     * Returns {@code true} if this is a native method.
     * @return {@code true} if this is a native method.
     */
    boolean isNative();

    /**
     * Returns {@code true} if this method is a constructor.
     * @return {@code true} if this method is a constructor.
     */
    default boolean isConstructor() {
        return false;
    }

    /**
     * The method parameters component of the {@link #descriptor() descriptor}.
     *
     * @return This method's list of parameters.
     * @see #descriptor()
     */
    default @NotNull List<@NotNull JvmType> params() {
        return this.descriptor().getParams();
    }

    /**
     * Get the parameter based on the position the parameter appears in the parameter list, starting from 0. The
     * parameter's LVT index is not considered with this method, only the positional index. For LVT index, use
     * {@link #paramLvt(int)}.
     *
     * @param i The positional index of the parameter to find.
     * @return The parameter at the position index given.
     * @throws IndexOutOfBoundsException If the given index is out of bounds.
     * @see #paramLvt(int)
     */
    default @NotNull JvmType param(final int i) {
        final List<@NotNull JvmType> params = this.descriptor().getParams();
        if (i < 0 || i >= params.size()) {
            throw new IndexOutOfBoundsException(
                "Index out of range: " + i + ", list has " + params.size() + " items"
            );
        }
        return params.get(i);
    }

    /**
     * Get the parameter based on the parameter at the LVT index given. The parameter's position index is not considered
     * with this method, only the LVT index. For positional index, use {@link #param(int)}.
     *
     * @param i The LVT index of the parameter to find.
     * @return The parameter at the LVT index given, or {@code null} if no parameter could be found at the given LVT
     *         index.
     * @see #param(int)
     */
    @SuppressWarnings("EmptyCatch")
    default @Nullable JvmType paramLvt(final int i) {
        final List<@NotNull JvmType> params = this.descriptor().getParams();
        // `this`
        int index = this.isStatic() ? 0 : 1;

        for (final JvmType param : params) {
            if (index == i) {
                return param;
            }

            index++;
            if (param == PrimitiveType.LONG || param == PrimitiveType.DOUBLE) {
                index++;
            }
        }

        return null;
    }

    /**
     * The return type component of the {@link #descriptor() descriptor}.
     *
     * @return This method's return type.
     * @see #descriptor()
     */
    default @NotNull JvmType returnType() {
        return this.descriptor().getReturnType();
    }

    /**
     * Returns {@code true} if this method data extends the given method data, or if this method data is the same as the
     * given method data. This checks method modifiers to first determine if the given method is allowed to be
     * overridden, and checks this and the given method data's visibility to ensure they are valid.
     *
     * <p>This method does <i>not</i> handle covariant return types, or methods which override a parent method with a more
     * specific return type. From the JVM's perspective only methods with identical descriptors override each other,
     * so information around method synthetic targets is needed to determine those situations.
     *
     * @param that The method to check if this method extends.
     * @return {@code true} if this method overrides the given method.
     */
    default boolean overrides(final @NotNull MethodData that) {
        if (this.isStatic() || that.isStatic() || that.isFinal()) {
            return false;
        }

        if (!this.visibility().canOverride(that.visibility())) {
            return false;
        }

        if (that.visibility() == Visibility.PACKAGE) {
            final String thisClassName = this.parentClass().name();
            final String thatClassName = that.parentClass().name();
            final int thisLastIndex = thisClassName.lastIndexOf('/');
            final int thatLastIndex = thatClassName.lastIndexOf('/');
            if (thisLastIndex != thatLastIndex) {
                return false;
            }

            if (!thisClassName.regionMatches(0, thatClassName, 0, thatLastIndex)) {
                return false;
            }
        }

        if (!this.name().equals(that.name())) {
            return false;
        }
        if (!this.descriptor().equals(that.descriptor())) {
            return false;
        }

        return this.parentClass().doesExtendOrImplement(that.parentClass());
    }

    // Hydration methods

    /**
     * Set this method's super method. The given method must satisfy the {@link #overrides(MethodData)} method such
     * that the following code returns {@code true}:
     *
     * <pre>
     *     someMethod.setSuperMethod(superMethod);
     *     someMethod.overrides(someMethod.superMethod())
     * </pre>
     *
     * <p>This method is intended only to be called during hydration.
     *
     * @param superMethod The method to set as this method's super method.
     */
    void setSuperMethod(final @Nullable MethodData superMethod);

    /**
     * Returns this method's super method, if one has been set. This method will always return {@code null} unless this
     * method data has been hydrated.
     *
     * @return This method's super method.
     */
    @Nullable MethodData superMethod();

    /**
     * Returns the set of methods which override this method. This method will always return {@code null} unless this
     * method data has been hydrated.
     *
     * @return The set of methods which override this method.
     */
    @NotNull Set<MethodData> childMethods();
}
