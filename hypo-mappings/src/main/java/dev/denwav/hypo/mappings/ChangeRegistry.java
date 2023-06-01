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

package dev.denwav.hypo.mappings;

import dev.denwav.hypo.mappings.changes.MemberReference;
import dev.denwav.hypo.mappings.contributors.ChangeContributor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.cadixdev.lorenz.MappingSet;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static dev.denwav.hypo.model.HypoModelUtil.cast;

/**
 * Collection of scheduled changes to a {@link MappingSet} in the form of
 * {@link ClassMappingsChange class mapping changes} and {@link MappingsChange member mappings changes}. Class mapping
 * changes are submitted to the registry via {@link #submitChange(ClassMappingsChange)}, and member mapping changes are
 * submitted via {@link #submitChange(MappingsChange)}.
 *
 * <p>{@link #submitChange(MappingsChange)} and {@link #submitChange(ClassMappingsChange)} are thread-safe and may be
 * called concurrently from multiple threads. Once changes have been fully submitted and
 * {@link #applyChanges(MappingSet)} is called however, no more changes may be submitted.
 *
 * <p>The current registry may be re-used by calling {@link #clear()} between iterations of change submission and
 * change application, but in practice it's not any cheaper than to simply create a new instance of this class.
 *
 * <h2>Handling multiple changes to a single target</h2>
 * <p>In either case of {@link #submitChange(ClassMappingsChange) class mappings changes} or
 * {@link #submitChange(MappingsChange) member mappings changes} only a single change may target a class or member per
 * change application. The one exception is if all changes targeting a give member implement
 * {@link MergeableMappingsChange} and they are all mergeable with each other.
 *
 * @see MappingsCompletionManager
 * @see ChangeChain
 */
public final class ChangeRegistry {

    private final @NotNull ConcurrentHashMap<MemberReference, List<MappingsChange>> changes = new ConcurrentHashMap<>();
    private final @NotNull ConcurrentHashMap<String, List<ClassMappingsChange>> classChanges = new ConcurrentHashMap<>();

    private @Nullable String currentContributorName = null;

    /**
     * Submit a planned change to a {@link MappingSet} targeting the {@link MemberReference} referred to by the
     * {@link MappingsChange#target() target()} method of the given {@code change}.
     *
     * @param change The change to submit to this registry.
     */
    public void submitChange(final @NotNull MappingsChange change) {
        this.changes.computeIfAbsent(change.target(), k -> new ArrayList<>()).add(change);
    }

    /**
     * Submit a planned change to a {@link MappingSet} targeting the class name referred to by the
     * {@link ClassMappingsChange#targetClass() targetClass()} method of the given {@code change}.
     *
     * @param change The change to submit to this registry.
     */
    public void submitChange(final @NotNull ClassMappingsChange change) {
        this.classChanges.computeIfAbsent(change.targetClass(), k -> new ArrayList<>()).add(change);
    }

    /**
     * For use by {@link MappingsCompletionManager} to allow for better error messages when mapping changes conflict
     * with each other. If this method is called before {@link #applyChanges(MappingSet)} then the name give here will
     * be included in the error message as the change contributor responsible for the conflicting changes.
     *
     * @param currentContributorName The name of the currently running {@link ChangeContributor ChangeContributor}
     */
    public void setCurrentContributorName(final @Nullable String currentContributorName) {
        this.currentContributorName = currentContributorName;
    }

    /**
     * Returns the map of member mapping changes currently submitted to this registry.
     * @return The map of member mapping changes currently submitted to this registry.
     */
    public @NotNull Map<MemberReference, List<MappingsChange>> getChanges() {
        return this.changes;
    }

    /**
     * Returns the map of class mapping changes currently submitted to this registry.
     * @return The map of class mapping changes currently submitted to this registry.
     */
    public @NotNull ConcurrentHashMap<String, List<ClassMappingsChange>> getClassChanges() {
        return this.classChanges;
    }

    /**
     * Apply the submitted {@link #submitChange(MappingsChange) member mapping} and
     * {@link #submitChange(ClassMappingsChange) class mapping} changes to the given {@link MappingSet}.
     *
     * <p>This method does not modify the given {@link MappingSet}, instead it is {@link MappingSet#copy() copied} and
     * the changes are applied to the copy instead, which is then returned.
     *
     * @param input The {@link MappingSet} to apply the changes to. This {@link MappingSet} is not modified.
     * @return A copy of {@code input} with the submitted changes applied to it.
     * @throws IllegalStateException If there are conflicting changes targeting to the same member or class.
     */
    @Contract(value = "_ -> new", pure = true)
    public @NotNull MappingSet applyChanges(final @NotNull MappingSet input) {
        final List<Map.Entry<MemberReference, List<MappingsChange>>> duplicates = this.changes.entrySet().stream()
            .filter(e -> e.getValue().size() > 1)
            .filter(e -> {
                final List<MappingsChange> changes = e.getValue();
                Class<? extends MappingsChange> clazz = null;
                for (final MappingsChange change : changes) {
                    Class<? extends MappingsChange> changeClazz = change.getClass();
                    if (clazz == null) {
                        clazz = changeClazz;
                    }
                    if (clazz != changeClazz) {
                        return true;
                    }
                    if (!(change instanceof MergeableMappingsChange)) {
                        return true;
                    }
                }
                return false;
            })
            .collect(Collectors.toList());

        if (!duplicates.isEmpty()) {
            final String dupes = duplicates.stream()
                .map(e -> "\t" + e.getKey() + "\n" + e.getValue().stream().map(r -> "\t\t" + r).collect(Collectors.joining("\n")))
                .collect(Collectors.joining("\n"));
            throw new IllegalStateException("Multiple changes registered (via " + this.currentContributorName +
                " contributor) for the following member mappings:\n" + dupes);
        }

        List<String> failures = null;

        final MappingSet result = input.copy();

        for (final List<MappingsChange> changes : this.changes.values()) {
            MappingsChange change = changes.get(0);
            if (changes.size() == 1) {
                change.applyChange(result);
            } else if (change instanceof MergeableMappingsChange) {
                boolean shouldApply = true;

                for (int i = 1; i < changes.size(); i++) {
                    final MappingsChange nextChange = changes.get(i);
                    if (nextChange == null) {
                        continue;
                    }

                    final MergeResult<?> mergeResult = ((MergeableMappingsChange<?>) change).mergeWith(cast(nextChange));
                    if (mergeResult.isFailure()) {
                        if (failures == null) {
                            failures = new ArrayList<>();
                        }

                        failures.add("\tCannot merge changes: " + mergeResult.getErrorMessage() + "\n\t\t" + change + "\n\t\t" + nextChange);
                        shouldApply = false;
                    } else if (mergeResult.isSuccess()) {
                        change = mergeResult.getMerged();
                    } else {
                        throw new IllegalStateException("Merge result is somehow neither success nor failure: " +
                            mergeResult);
                    }
                }

                if (shouldApply) {
                    change.applyChange(result);
                }
            } else {
                throw new IllegalStateException("Cannot handle multiple non-mergeable changes: " + changes);
            }
        }

        if (failures != null && !failures.isEmpty()) {
            throw new IllegalStateException("Failed to apply mapping set changes from " + this.currentContributorName +
                " contributor:\n" + String.join("\n", failures));
        }

        // Apply all class changes
        final List<Map.Entry<String, List<ClassMappingsChange>>> multiClassChanges = this.classChanges.entrySet().stream()
            .filter(e -> e.getValue().size() > 1)
            .collect(Collectors.toList());

        if (!multiClassChanges.isEmpty()) {
            final String dupes = multiClassChanges.stream()
                .map(e -> "\t" + e.getKey() + "\n" + e.getValue().stream().map(r -> "\t\t" + r).collect(Collectors.joining("\n")))
                .collect(Collectors.joining("\n"));
            throw new IllegalStateException("Multiple class changes registered (via " + this.currentContributorName +
                " contributor):\n" + dupes);
        }

        for (final List<ClassMappingsChange> changes : this.classChanges.values()) {
            if (changes.isEmpty()) {
                continue;
            }
            changes.get(0).applyChange(result);
        }

        return result;
    }

    /**
     * Clear all changes submitted to this registry.
     */
    public void clear() {
        this.changes.clear();
        this.classChanges.clear();
    }
}
