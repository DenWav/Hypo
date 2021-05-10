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

package dev.denwav.hypo.mappings;

import dev.denwav.hypo.mappings.changes.MemberReference;
import org.jetbrains.annotations.NotNull;

/**
 * Mergeable variant of {@link MappingsChange}. If multiple mappings changes are submitted which target the same
 * {@link MemberReference MemberReference} they may be merged if they all meet the
 * following conditions:
 * <ol>
 *     <li>All changes are the same class</li>
 *     <li>The class of the changes implement {@link MergeableMappingsChange}</li>
 *     <li>All of changes merge successfully with {@link #mergeWith(MergeableMappingsChange)}</li>
 * </ol>
 *
 * <p>If the first 2 conditions are satisfied but {@link #mergeWith(MergeableMappingsChange)} returns a
 * {@link MergeResult#isFailure() failed merge result} when attempting to merge 2 of the changes, those 2 changes are
 * considered to be incompatible.
 *
 * @param <T> The type of {@code this}.
 */
public interface MergeableMappingsChange<T extends MergeableMappingsChange<T>> extends MappingsChange {

    /**
     * Merge {@code this} with {@code that}, returning a new instance of {@code this} which applies the combination of
     * both of the given changes. If the 2 changes cannot be merged such that both changes are completely satisfied
     * without issues, then this method will return a {@link MergeResult#isFailure() failed merge result}. If this
     * method returns a {@link MergeResult#isSuccess() successful merge result} it will be considered successful.
     *
     * <p>For an example of a change which is compatible, imagine if {@code this} and {@code that} both request a
     * mapping change with a new deobfuscated name, where both changes are requesting the same deobfuscated name. These
     * changes would be compatible with each other as they are requesting the same change.
     *
     * <p>For another example, imagine if {@code this} and {@code that} both request changes to a method mapping, but
     * each change is requesting a change to a different method parameter index. These changes are compatible because
     * they have different targets (different method parameters) but the returned mappings change much apply <i>both</i>
     * sets of changes to be considered properly merged.
     *
     * <p>For an example of an unmergeable case, if the 2 changes target the same member but have different deobfuscated
     * names there is no way to successfully handle both cases, so the merge should fail.
     *
     * @param that The change, which must be the same type as {@code this}, to merge.
     * @return The new mappings change, which must be the same type as {@code this}, which applies the combination of
     *         changes from both mappings changes.
     */
    @NotNull MergeResult<T> mergeWith(final @NotNull T that);
}
