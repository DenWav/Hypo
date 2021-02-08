/*
 * Hypo, an extensible and pluggable Java bytecode analytical model.
 *
 * Copyright (C) 2021  Kyle Wood (DemonWav)
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

package com.demonwav.hypo.mappings;

import com.google.errorprone.annotations.CheckReturnValue;
import java.util.Objects;
import org.cadixdev.lorenz.MappingSet;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A representation of the result of a
 * {@link MergeableMappingsChange#mergeWith(MergeableMappingsChange) MergeableMappingsChange merge}. The result can only
 * be in one of 2 states: {@link #success(MergeableMappingsChange) success} or {@link #failure(String) failure}.
 *
 * <p>The error message provided for {@link #failure(String) failed} changes is used for the error message presented
 * when the {@link ChangeRegistry} attempts to {@link ChangeRegistry#applyChanges(MappingSet) apply} the changes.
 *
 * @param <T> The type of the merge result.
 */
public final class MergeResult<T extends MergeableMappingsChange<T>> {

    private final @Nullable T merged;
    private final @Nullable String errorMessage;

    private MergeResult(final @Nullable T merged, final @Nullable String errorMessage) {
        this.merged = merged;
        this.errorMessage = errorMessage;
    }

    /**
     * Create a new successful merge result with the given {@code merged} change.
     *
     * @param merged The {@link MergeableMappingsChange} which represents the merging of the 2 original changes.
     * @param <T> The type of the changes which have been merged.
     * @return A new {@link MergeResult} representing a successful merge.
     */
    @CheckReturnValue
    @Contract(value = "_ -> new", pure = true)
    public static <T extends MergeableMappingsChange<T>> @NotNull MergeResult<T> success(final @NotNull T merged) {
        return new MergeResult<>(merged, null);
    }

    /**
     * Create a new failed merge result with the given {@code errorMessage}.
     *
     * <p>The error message provided is used for the error message presented when the {@link ChangeRegistry} attempts to
     * {@link ChangeRegistry#applyChanges(MappingSet) apply} the changes.
     *
     * @param errorMessage The error message explaining why the merge failed.
     * @param <T> The type of the changes which have been merged (not used here, though).
     * @return A new {@link MergeResult} representing a failed merge.
     */
    @CheckReturnValue
    @Contract(value = "_ -> new", pure = true)
    public static <T extends MergeableMappingsChange<T>> MergeResult<T> failure(final @NotNull String errorMessage) {
        return new MergeResult<>(null, errorMessage);
    }

    /**
     * Get the merge result if and only if this is a {@link #isSuccess() successful} merge result.
     *
     * @return The merge result.
     * @throws NullPointerException If this is not a {@link #isSuccess() successful} merge result.
     */
    public @NotNull T getMerged() {
        return Objects.requireNonNull(this.merged, "Failed merge result");
    }

    /**
     * Get the error message if and only if this is a {@link #isFailure() failed} merge result.
     *
     * @return The error message.
     * @throws NullPointerException If this is not a {@link #isFailure() failed} merge result.
     */
    public @NotNull String getErrorMessage() {
        return Objects.requireNonNull(this.errorMessage, "Succeeded merge result");
    }

    /**
     * Returns {@code true} if this merge result represents a successful merge.
     *
     * <p>This method will always return the opposite of {@link #isFailure()}.
     *
     * @return {@code true} if this merge result represents a successful merge.
     */
    public boolean isSuccess() {
        return this.merged != null;
    }

    /**
     * Returns {@code true} if this merge result represents a failed merge.
     *
     * <p>This method will always return the opposite of {@link #isSuccess()}.
     *
     * @return {@code true} if this merge result respresents a failed merge.
     */
    public boolean isFailure() {
        return !this.isSuccess();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        final MergeResult<?> that = (MergeResult<?>) o;
        return Objects.equals(this.merged, that.merged) && Objects.equals(this.errorMessage, that.errorMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.merged, this.errorMessage);
    }

    @Override
    public String toString() {
        return "ChangeResult{" +
            "merged=" + this.merged +
            ", errorMessage='" + this.errorMessage + '\'' +
            '}';
    }
}
