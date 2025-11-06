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

package dev.denwav.hypo.types.sig.param;

import com.google.errorprone.annotations.Immutable;
import dev.denwav.hypo.types.intern.Intern;
import dev.denwav.hypo.types.TypeRepresentable;
import dev.denwav.hypo.types.TypeVariableBinder;
import dev.denwav.hypo.types.sig.ReferenceTypeSignature;
import dev.denwav.hypo.types.sig.TypeSignature;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link TypeArgument} which is a wildcard type with either an upper or lower bound. For a wildcard type argument
 * with no bound, use {@link WildcardArgument}.
 *
 * <p>Type arguments can have either an {@link WildcardBound#UPPER upper} or {@link WildcardBound#LOWER lower} bound.
 * These correspond to {@code ? extends} and {@code ? super} respectively. To put another way, the type argument for
 * the following {@code List} type is a bounded type argument with its upper bound set to be {@code Serializable}:
 * <pre><code>
 *     List&lt;? extends Serializable&gt;
 * </code></pre>
 *
 * <p>And the type argument for the follow {@code List} type is a bounded type argument with its lower bound set to be
 * {@code String}:
 * <pre><code>
 *     List&lt;? super String&gt;
 * </code></pre>
 *
 * <p>The bound type does not need to be a concrete class type, it can be a type variable instead, such as:
 * <pre><code>
 *     List&lt;? extends T&gt;
 * </code></pre>
 *
 * @see WildcardArgument
 */
@Immutable
public final class BoundedTypeArgument extends Intern<BoundedTypeArgument> implements TypeArgument, TypeRepresentable {

    private final @NotNull WildcardBound bounds;
    private final @NotNull ReferenceTypeSignature signature;

    /**
     * Create a new instance of {@link BoundedTypeArgument}.
     *
     * @param bounds The bound for this type argument.
     * @param signature The corresponding bound type.
     * @return The new {@link BoundedTypeArgument}.
     */
    public static @NotNull BoundedTypeArgument of(
        final @NotNull WildcardBound bounds,
        final @NotNull ReferenceTypeSignature signature
    ) {
        return new BoundedTypeArgument(bounds, signature).intern();
    }

    private BoundedTypeArgument(
        final @NotNull WildcardBound bounds,
        final @NotNull ReferenceTypeSignature signature
    ) {
        this.bounds = bounds;
        this.signature = signature;
    }

    @Override
    public void asReadable(final @NotNull StringBuilder sb) {
        this.bounds.asReadable(sb);
        sb.append(' ');

        this.signature.asReadable(sb);
    }

    @Override
    public void asInternal(final @NotNull StringBuilder sb) {
        this.asInternal(sb, false);
    }

    @Override
    public void asInternal(final @NotNull StringBuilder sb, final boolean withBindKey) {
        this.bounds.asInternal(sb);
        this.signature.asInternal(sb, withBindKey);
    }

    @Override
    public @NotNull BoundedTypeArgument bind(final @NotNull TypeVariableBinder binder) {
        return BoundedTypeArgument.of(this.bounds, this.signature.bind(binder));
    }

    @Override
    public boolean isUnbound() {
        return this.signature.isUnbound();
    }

    /**
     * Return the {@link WildcardBound bounds} for this type argument.
     * @return The {@link WildcardBound bounds} for this type argument.
     */
    public @NotNull WildcardBound getBounds() {
        return this.bounds;
    }

    /**
     * Return the {@link TypeSignature bound type} for this type argument.
     * @return The {@link TypeSignature bound type} for this type argument.
     */
    public @NotNull ReferenceTypeSignature getSignature() {
        return this.signature;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof final BoundedTypeArgument that)) {
            return false;
        }
        return this.bounds == that.bounds
            && Objects.equals(this.signature, that.signature);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.bounds, this.signature);
    }

    @Override
    public String toString() {
        return this.asReadable();
    }
}
