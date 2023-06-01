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

package dev.denwav.hypo.model.data.types;

import dev.denwav.hypo.model.HypoModelUtil;
import com.google.errorprone.annotations.Immutable;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A standard class or reference type.
 */
@Immutable
public final class ClassType implements JvmType {

    private final @NotNull String className;

    /**
     * Create a new class type from the given class name.
     *
     * @param className The name of the class this type represents.
     */
    public ClassType(final @NotNull String className) {
        this.className = HypoModelUtil.normalizedClassName(className);
    }

    @Override
    public void asReadableName(final @NotNull StringBuilder sb) {
        sb.append(this.className.replace('/', '.'));
    }

    @Override
    public void asInternalName(final @NotNull StringBuilder sb) {
        sb.append('L').append(this.className).append(';');
    }

    /**
     * Returns the primitive type associated with this class type if this is a primitive wrapper class type.
     *
     * @return The primitive type associated with this class type, or {@code null} if this is a primitive wrapper class
     *         type.
     */
    public @Nullable PrimitiveType toPrimitiveType() {
        for (final PrimitiveType value : PrimitiveType.values()) {
            if (value.toWrapperType().equals(this)) {
                return value;
            }
        }
        return null;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof ClassType)) return false;
        final ClassType classType = (ClassType) o;
        return this.className.equals(classType.className);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.className);
    }

    @Override
    public String toString() {
        return this.asReadableName();
    }
}
