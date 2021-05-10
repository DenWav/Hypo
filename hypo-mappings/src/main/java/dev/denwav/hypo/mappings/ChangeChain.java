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

import dev.denwav.hypo.core.HypoException;
import dev.denwav.hypo.mappings.contributors.ChangeContributor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.cadixdev.lorenz.MappingSet;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Chain of {@link ChangeContributor} application steps (called "links" here to complete the metaphor) to be executed
 * one after the other, rather than together.
 *
 * <p>Each link may have multiple {@link ChangeContributor contributors}, and there may be an arbitrary number of links
 * as well. Any link which has multiple contributors defined should make sure the contributors in that link will never
 * produce conflicting mappings changes.
 *
 * <p>Use {@link #create()} to create a new instance of this class.
 * {@link #applyChain(MappingSet, MappingsCompletionManager)} will apply each link one at a time and return the final
 * {@link MappingSet}. Listeners may be registered to respond to events during the chain process with
 * {@link #addLinkCompletedListener(LinkCompletionListener)} and {@link #addMappingSetListener(LinkMappingsListener)}.
 *
 * @see MappingsCompletionManager
 * @see ChangeRegistry
 */
public final class ChangeChain {

    private final @NotNull List<List<ChangeContributor>> links = new ArrayList<>();

    private @Nullable LinkCompletionListener completedLinkListener = null;
    private @Nullable LinkMappingsListener newMappingSetListener = null;

    private ChangeChain() {}

    /**
     * Create a new instance of {@link ChangeChain}.
     *
     * @return The new instance of {@link ChangeChain}.
     */
    @Contract(value = "-> new", pure = true)
    public static @NotNull ChangeChain create() {
        return new ChangeChain();
    }

    /**
     * Add a link to this chain with the list of {@link ChangeContributor change contributors} to run in this link.
     *
     * @param changes The list of contributors to run for this link.
     * @return {@code this} for chaining.
     */
    @Contract("_ -> this")
    public @NotNull ChangeChain addLink(final @NotNull List<ChangeContributor> changes) {
        this.links.add(changes);
        return this;
    }

    /**
     * Add a link to this chain with a single {@link ChangeContributor change contributor} to run in this link..
     *
     * @param change The contributor to run for this link.
     * @return {@code this} for chaining.
     */
    @Contract("_ -> this")
    public @NotNull ChangeChain addLink(final @NotNull ChangeContributor change) {
        this.links.add(Collections.singletonList(change));
        return this;
    }

    /**
     * Add a link to this chain with the list of {@link ChangeContributor change contributors} to run in this link.
     *
     * @param changes The contributors to run for this link.
     * @return {@code this} for chaining.
     */
    @Contract("_ -> this")
    public @NotNull ChangeChain addLink(final @NotNull ChangeContributor @NotNull ... changes) {
        this.links.add(Arrays.asList(changes));
        return this;
    }

    /**
     * Add a listener which will be called at the end of each link, but before the {@link ChangeRegistry} is applied to
     * the current {@link MappingSet}.
     *
     * <p>Only a single link completion listener may be registered at a time, calling this method multiple times will
     * overwrite the previous setting.
     *
     * @param listener The listener to run on the completion of a link before the changes are applied to the
     *                 {@link MappingSet}.
     * @return {@code this} for chaining.
     * @see #addMappingSetListener(LinkMappingsListener)
     */
    @Contract("_ -> this")
    public @NotNull ChangeChain addLinkCompletedListener(final @NotNull LinkCompletionListener listener) {
        this.completedLinkListener = listener;
        return this;
    }

    /**
     * Add a listener which will be called after the changes in each link have been applied to the {@link MappingSet}.
     *
     * <p>Only a single mapping set listener mayh be registered at a time, calling this method multiple times will
     * overwrite the previous setting.
     *
     * @param listener The listener to run after the {@link MappingSet} for each link has been modified.
     * @return {@code this} for chaining.
     * @see #addLinkCompletedListener(LinkCompletionListener)
     */
    @Contract("_ -> this")
    public @NotNull ChangeChain addMappingSetListener(final @NotNull LinkMappingsListener listener) {
        this.newMappingSetListener = listener;
        return this;
    }

    /**
     * Apply the links registered in this chain to the given {@link MappingSet} for the given
     * {@link MappingsCompletionManager}. The given {@link MappingSet} will not be modified.
     *
     * @param mappings The mappings to apply the chain to.
     * @param manager The completion manager to use for applying each link.
     * @return The modified {@link MappingSet} returned from the final ink in this chain.
     * @throws HypoException If the {@link MappingsCompletionManager} fails to complete the mappings for one of the
     *                       links.
     */
    public @NotNull MappingSet applyChain(
        final @NotNull MappingSet mappings,
        final @NotNull MappingsCompletionManager manager
    ) throws HypoException {
        final LinkCompletionListener linkListener = this.completedLinkListener;
        final LinkMappingsListener mappingListener = this.newMappingSetListener;

        int counter = 0;

        MappingSet currentMappings = mappings;
        for (final List<ChangeContributor> link : this.links) {
            final ChangeRegistry registry = manager.completeMappings(currentMappings, link);
            if (linkListener != null) {
                linkListener.accept(counter, currentMappings, registry);
            }

            currentMappings = registry.applyChanges(currentMappings);
            if (mappingListener != null) {
                mappingListener.accept(counter, currentMappings);
            }

            counter++;
        }

        return currentMappings;
    }

    /**
     * Functional interface for {@link #addLinkCompletedListener(LinkCompletionListener)}.
     */
    @FunctionalInterface
    public interface LinkCompletionListener {

        /**
         * Method which will be called at the end of each link.
         *
         * @param index The link index for this change, starting at {@code 0}.
         * @param mappingSet The current {@link MappingSet}, before changes have been applied. This mapping set will not
         *                   be modified, instead it will be {@link MappingSet#copy() copied} and the changes will be
         *                   applied to the copy.
         * @param registry The {@link ChangeRegistry} with the changes which are about to be applied to the
         *                 {@code mappingSet}.
         */
        void accept(final int index, final @NotNull MappingSet mappingSet, final @NotNull ChangeRegistry registry);
    }

    /**
     * Functional interface for {@link #addMappingSetListener(LinkMappingsListener)}.
     */
    @FunctionalInterface
    public interface LinkMappingsListener {

        /**
         * Method which will be called after the {@link MappingSet} has been modified for each link.
         *
         * @param index The link index for this change, starting at {@code 0}.
         * @param mappingSet The new {@link MappingSet} after changes were applied.
         */
        void accept(final int index, final @NotNull MappingSet mappingSet);
    }
}
