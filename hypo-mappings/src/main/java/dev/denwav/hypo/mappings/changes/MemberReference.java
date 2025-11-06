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

package dev.denwav.hypo.mappings.changes;

import com.google.errorprone.annotations.Immutable;
import dev.denwav.hypo.model.data.FieldData;
import dev.denwav.hypo.model.data.MethodData;
import java.util.Objects;
import org.cadixdev.bombe.type.FieldType;
import org.cadixdev.lorenz.model.FieldMapping;
import org.cadixdev.lorenz.model.MethodMapping;
import org.cadixdev.lorenz.model.MethodParameterMapping;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static dev.denwav.hypo.mappings.LorenzUtil.getType;

/**
 * A reference to a class or method member.
 *
 * <p>Class member references are made of up 3 parts:
 * <ol>
 *     <li>{@link #className() Class name}</li>
 *     <li>{@link #name() Member name}</li>
 *     <li>{@link #desc() Member desccriptor}</li>
 *     <li>{@link #index() Member index}</li>
 * </ol>
 *
 * <p>The descriptor is optional for fields, but is required for methods.
 * <p>The index specifies parameter references.
 *
 * <p>Member references are immutable.
 */
@Immutable
public final class MemberReference {

    private final @NotNull String className;
    private final @NotNull String memberName;
    private final @Nullable String memberDesc;
    private final int index;

    /**
     * Construct a new {@link MemberReference}.
     *
     * @param className The name of the class.
     * @param memberName The name of the member.
     * @param memberDesc The descriptor of the member.
     */
    public MemberReference(
        final @NotNull String className,
        final @NotNull String memberName,
        final @Nullable String memberDesc
    ) {
        this.className = className;
        this.memberName = memberName;
        this.memberDesc = memberDesc;
        this.index = -1;
    }

    /**
     * Construct a new {@link MemberReference} for a parameter.
     *
     * @param className The name of the class.
     * @param memberName The name of the member.
     * @param memberDesc The descriptor of the member.
     * @param index The index of the parameter.
     */
    public MemberReference(
        final @NotNull String className,
        final @NotNull String memberName,
        final @NotNull String memberDesc,
        final int index
    ) {
        this.className = className;
        this.memberName = memberName;
        this.memberDesc = memberDesc;
        this.index = index;
    }

    /**
     * Create a reference pointing to the given {@link MethodData}.
     *
     * @param method The method the reference should point to.
     * @return The new {@link MemberReference}.
     */
    public static @NotNull MemberReference of(final @NotNull MethodData method) {
        return new MemberReference(method.parentClass().name(), method.name(), method.descriptorText());
    }

    /**
     * Create a reference pointing to the given {@link FieldData}.
     *
     * @param field The field the reference should point to.
     * @return The new {@link MemberReference}.
     */
    public static @NotNull MemberReference of(final @NotNull FieldData field) {
        return new MemberReference(field.parentClass().name(), field.name(), field.descriptorText());
    }

    /**
     * Create a parameter reference pointing to the given {@link MethodData} at the specified index.
     *
     * @param method The method the reference should point to.
     * @param index The parameter index the reference should point to.
     * @return The new {@link MemberReference}.
     */
    public static @NotNull MemberReference of(final @NotNull MethodData method, final int index) {
        return new MemberReference(method.parentClass().name(), method.name(), method.descriptorText(), index);
    }

    /**
     * Create a reference pointing to the obfuscated name of the given {@link MethodMapping}.
     *
     * @param mapping The method mapping the reference should point to.
     * @return The new {@link MemberReference}.
     */
    @Contract(value = "_ -> new", pure = true)
    public static @NotNull MemberReference of(final @NotNull MethodMapping mapping) {
        return new MemberReference(
            mapping.getParent().getFullObfuscatedName(),
            mapping.getObfuscatedName(),
            mapping.getObfuscatedDescriptor()
        );
    }

    /**
     * Create a reference pointing to the obfuscated name of the given {@link FieldMapping}.
     *
     * @param mapping The field mapping the reference should point to.
     * @return The new {@link MemberReference}.
     */
    @Contract(value = "_ -> new", pure = true)
    public static @NotNull MemberReference of(final @NotNull FieldMapping mapping) {
        final FieldType type = getType(mapping);
        return new MemberReference(
            mapping.getParent().getFullObfuscatedName(),
            mapping.getObfuscatedName(),
            type == null ? null : type.toString()
        );
    }

    /**
     * Create a reference pointing to the obfuscated name of the given {@link MethodParameterMapping}.
     *
     * @param mapping The parameter mapping the reference should point to.
     * @return The new {@link MemberReference}.
     */
    public static @NotNull MemberReference of(final @NotNull MethodParameterMapping mapping) {
        final MethodMapping method = mapping.getParent();
        return new MemberReference(
            method.getParent().getFullObfuscatedName(),
            method.getObfuscatedName(),
            method.getObfuscatedDescriptor(),
            mapping.getIndex()
        );
    }

    /**
     * Returns the name of the class the member this refers to is declared in.
     * @return The name of the class the member this refers to is declared in.
     */
    public @NotNull String className() {
        return this.className;
    }

    /**
     * Returns the name of the member.
     * @return The name of the member.
     */
    public @NotNull String name() {
        return this.memberName;
    }

    /**
     * Returns the descriptor of the member.
     * @return The descriptor of the member.
     */
    public @Nullable String desc() {
        return this.memberDesc;
    }

    /**
     * Returns the parameter index of the reference.
     * @return The parameter index of the reference.
     */
    public int index() {
        return this.index;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        final MemberReference that = (MemberReference) o;
        return this.className.equals(that.className)
            && this.memberName.equals(that.memberName)
            && Objects.equals(this.memberDesc, that.memberDesc)
            && this.index == that.index;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.className, this.memberName, this.memberDesc, this.index);
    }

    @Override
    public String toString() {
        if (this.memberDesc != null) {
            if (this.index != -1) {
                return this.className + "#" + this.memberName + this.memberDesc + "##" + this.index;
            } else {
                return this.className + "#" + this.memberName + " " + this.memberDesc;
            }
        } else {
            return this.className + "#" + this.memberName;
        }
    }
}
