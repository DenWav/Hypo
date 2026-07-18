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

package dev.denwav.hypo.hydrate.generic;

import dev.denwav.hypo.model.data.ConstructorData;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Data class representing a {@code super()} or {@code this()} constructor call, and the constructor parameters which
 * were passed to the {@code super()} or {@code this()} call.
 *
 * <p>Note that for the purposes of this class, both {@code super()} and {@code this()} calls are considered super
 * constructor calls.
 *
 * @param thisConstructor  The {@link ConstructorData constructor} which calls to the {@code superConstructor}.
 * @param superConstructor The {@link ConstructorData constructor} referred to by the {@code this()} or
 *                         {@code super()} call.
 * @param params           The {@link SuperCallParameter parameters} which were passed directly from {@code thisConstructor}
 *                         to the {@code superConstructor}.
 */
public record SuperCall(
    @NotNull ConstructorData thisConstructor,
    @NotNull ConstructorData superConstructor,
    @NotNull List<@NotNull SuperCallParameter> params
) {

    /**
     * Returns {@code this} {@link ConstructorData constructor}.
     *
     * @return {@code this} {@link ConstructorData constructor}.
     */
    @Override
    public @NotNull ConstructorData thisConstructor() {
        return this.thisConstructor;
    }

    /**
     * Returns the {@code super} {@link ConstructorData constructor}.
     *
     * @return The {@code super} {@link ConstructorData constructor}.
     */
    @Override
    public @NotNull ConstructorData superConstructor() {
        return this.superConstructor;
    }

    /**
     * Returns the {@link SuperCallParameter parameters} which were passed directly from {@code thisConstructor} to the
     * {@code superConstructor}.
     *
     * @return The {@link SuperCallParameter parameters}.
     */
    @Override
    public @NotNull List<@NotNull SuperCallParameter> params() {
        return this.params;
    }

    /**
     * Get a new {@link SuperCall} object representing the chain of {@code super()} or {@code this()} from a parent
     * super call to another.
     *
     * <p>For example, given the following set of constructor calls:
     * Returns the {@link SuperCallParameter parameters}
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
                if (thisParam.thisIndex() == thatParam.superIndex()) {
                    newParams.add(new SuperCallParameter(thatParam.thisIndex(), thisParam.superIndex()));
                }
            }
        }

        return new SuperCall(that.thisConstructor(), this.superConstructor(), newParams);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof final SuperCall superCall)) {
            return false;
        }
        return this.thisConstructor.equals(superCall.thisConstructor)
            && this.superConstructor.equals(superCall.superConstructor)
            && this.params.equals(superCall.params);
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
     *
     * @param thisIndex  The index for the parameter of the current constructor.
     * @param superIndex The index for the parameter of the super constructor.
     */
    public record SuperCallParameter(int thisIndex, int superIndex) {

        /**
         * Returns the index for the parameter of the current constructor.
         *
         * @return The index for the parameter of the current constructor.
         */
        @Override
        public int thisIndex() {
            return this.thisIndex;
        }

        /**
         * Returns the index for the parameter of the super constructor.
         *
         * @return The index for the parameter of the super constructor.
         */
        @Override
        public int superIndex() {
            return this.superIndex;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof final SuperCallParameter that)) {
                return false;
            }
            return this.thisIndex == that.thisIndex && this.superIndex == that.superIndex;
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
