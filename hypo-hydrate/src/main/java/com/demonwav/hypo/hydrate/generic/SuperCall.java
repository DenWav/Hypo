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

package com.demonwav.hypo.hydrate.generic;

import com.demonwav.hypo.model.data.ConstructorData;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Data class representing a {@code super()} or {@code this()} constructor call, and the constructor parameters which
 * were passed to the {@code super()} or {@code this()} call.
 *
 * <p>Note that for the purposes of this class, both {@code super()} and {@code this()} calls are considered super
 * constructor calls.
 */
public final class SuperCall {
    private final @NotNull ConstructorData thisConstructor;
    private final @NotNull ConstructorData superConstructor;
    private final @NotNull List<@NotNull SuperCallParameter> params;

    /**
     * Construct a new {@link SuperCall}.
     *
     * @param thisConstructor The {@link ConstructorData constructor} which calls to the {@code superConstructor}.
     * @param superConstructor The {@link ConstructorData constructor} referred to by the {@code this()} or
     *                         {@code super()} call.
     * @param params The {@link SuperCallParameter parameters} which were passed directly from {@code thisConstructor}
     *               to the {@code superConstructor}.
     */
    public SuperCall(
        final @NotNull ConstructorData thisConstructor,
        final @NotNull ConstructorData superConstructor,
        final @NotNull List<@NotNull SuperCallParameter> params
    ) {
        this.thisConstructor = thisConstructor;
        this.superConstructor = superConstructor;
        this.params = params;
    }

    /**
     * Returns {@code this} {@link ConstructorData constructor}.
     * @return {@code this} {@link ConstructorData constructor}.
     */
    public @NotNull ConstructorData getThisConstructor() {
        return this.thisConstructor;
    }

    /**
     * Returns the {@code super} {@link ConstructorData constructor}.
     * @return The {@code super} {@link ConstructorData constructor}.
     */
    public @NotNull ConstructorData getSuperConstructor() {
        return this.superConstructor;
    }

    /**
     * Returns the {@link SuperCallParameter parameters} which were passed directly from {@code thisConstructor} to the
     * {@code superConstructor}.
     * @return The {@link SuperCallParameter parameters}.
     */
    public @NotNull List<@NotNull SuperCallParameter> getParams() {
        return this.params;
    }

    /**
     * Get a new {@link SuperCall} object representing the chain of {@code super()} or {@code this()} from a parent
     * super call to another.
     *
     * <p>For example, given the following set of constructor calls:
     *Returns the {@link SuperCallParameter parameters}
     * <pre>
     *     public SomeClass(int i) {
     *     }
     *
     *     public SomeClass(int i, int j) {
     *         this(i);
     *     }
     *
     *     public SomeClass(int i, int j, int k) {
     *         this(i, j);
     *     }
     * </pre>
     *
     * <p>If {@code firstSuperCall} {@link SuperCall} represents the super call from the second constructor to the
     * first, and the given {@link SuperCall} represents the super call from the third constructor to the second, then:
     *
     * <pre>
     *     firstSuperCall.chain(secondSuperCall);
     * </pre>
     *
     * <p>Would result in a {@link SuperCall} object representing the combination of {@code this()} calls from the third
     * constructor to the first constructor.
     *
     * @param that The {@link SuperCall} to chain {@code this} {@link SuperCall} with.
     * @return The new chained {@link SuperCall}.
     */
    @Contract("_ -> new")
    public @NotNull SuperCall chain(final @NotNull SuperCall that) {
        final ArrayList<SuperCallParameter> newParams = new ArrayList<>();

        for (final SuperCallParameter thisParam : this.params) {
            for (final SuperCallParameter thatParam : that.params) {
                if (thisParam.getThisIndex() == thatParam.getSuperIndex()) {
                    newParams.add(new SuperCallParameter(thatParam.getThisIndex(), thisParam.getSuperIndex()));
                }
            }
        }

        return new SuperCall(that.getThisConstructor(), this.getSuperConstructor(), newParams);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof SuperCall)) return false;
        final SuperCall superCall = (SuperCall) o;
        return this.thisConstructor.equals(superCall.thisConstructor)
            && this.superConstructor.equals(superCall.superConstructor)
            && this.params.equals(superCall.params);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.thisConstructor, this.superConstructor, this.params);
    }

    @Override
    public String toString() {
        return "SuperCall{" +
            "thisConstructor=" + this.thisConstructor +
            ", superConstructor=" + this.superConstructor +
            ", params=" + this.params +
            '}';
    }

    /**
     * The parameter mapping for a {@link SuperCall}. This class represents a single mapping of method parameter indexes
     * from the current constructor to the super constructor. For example:
     *
     * <pre>
     *     public SomeClass(int i) {
     *     }
     *
     *     public SomeClass(int i, int j) {
     *         this(i);
     *     }
     * </pre>
     *
     * <p>Then the {@link SuperCall} object representing the {@code this()} call would have a single
     * {@link SuperCallParameter super call parameter} with the {@code thisIndex} of {@code 0} and {@code superIndex}
     * of {@code 0}.
     *
     * <p>Given another example:
     *
     * <pre>
     *     public SomeClass(long k, long i) {
     *     }
     *
     *     public SomeClass(int i, int k) {
     *         this((long) k, (long) i)); // Notice the order is swapped!
     *     }
     * </pre>
     *
     * <p>The {@link SuperCall} object representing the {@code this()} call would have 2
     * {@link SuperCallParameter super call parameters}:
     *
     * <ol>
     *     <li>{@code thisIndex} of {@code 0} and {@code superIndex} of {@code 1}</li>
     *     <li>{@code thisIndex} of {@code 1} and {@code superIndex} of {@code 0}</li>
     * </ol>
     */
    public static final class SuperCallParameter {
        private final int thisIndex;
        private final int superIndex;

        /**
         * Construct a new super call parameter mapping between the given {@code thisIndex} and {@code superIndex}.
         *
         * @param thisIndex The index for the parameter of the current constructor.
         * @param superIndex The index for the parameter of the super constructor.
         */
        public SuperCallParameter(final int thisIndex, final int superIndex) {
            this.thisIndex = thisIndex;
            this.superIndex = superIndex;
        }

        /**
         * Returns the index for the parameter of the current constructor.
         * @return The index for the parameter of the current constructor.
         */
        public int getThisIndex() {
            return this.thisIndex;
        }

        /**
         * Returns the index for the parameter of the super constructor.
         * @return The index for the parameter of the super constructor.
         */
        public int getSuperIndex() {
            return this.superIndex;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (!(o instanceof SuperCallParameter)) return false;
            final SuperCallParameter that = (SuperCallParameter) o;
            return this.thisIndex == that.thisIndex && this.superIndex == that.superIndex;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.thisIndex, this.superIndex);
        }

        @Override
        public String toString() {
            return "SuperCallParameter{" +
                "thisIndex=" + this.thisIndex +
                ", superIndex=" + this.superIndex +
                '}';
        }
    }
}
