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

package dev.denwav.hypo.types.parsing;

import dev.denwav.hypo.types.HypoTypesUtil;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Internal state used by {@link JvmTypeParser}.
 */
@ApiStatus.Internal
/* package */ final class ParserState {

    private final @NotNull String text;

    private int mark = -1;
    private int index;

    private @Nullable Object lastResult = null;

    /**
     * Constructor for {@link ParserState}.
     * @param text Text to parse.
     * @param from Index to start parsing from.
     */
    /* package */ ParserState(final @NotNull String text, final int from) {
        this.text = text;

        if (from < 0 || from > text.length()) {
            throw new IllegalArgumentException("Invalid start index for string: " + from);
        }
        this.index = from;
    }

    /**
     * Get the last result, if set.
     * @param <T> The type of the last result.
     * @return The last result, or {@code null} if no result has been set.
     */
    @SuppressWarnings("TypeParameterUnusedInFormals")
    /* package */ <T> @Nullable T getLastResultOrNull() {
        final Object res = this.lastResult;
        this.lastResult = null;
        return HypoTypesUtil.cast(res);
    }

    /**
     * Get the last result, fail if no result is set.
     * @param <T> The type of the last result.
     * @return The last result.
     * @throws NullPointerException If no result has been set.
     */
    @SuppressWarnings("TypeParameterUnusedInFormals")
    /* package */ <T> @NotNull T getLastResult() {
        final T res = this.getLastResultOrNull();
        if (res == null) {
            throw new NullPointerException("No last result set");
        }
        return res;
    }

    /**
     * Set the result for {@link #getLastResult()}.
     * @param lastResult The result.
     * @return {@code true} if the given result was not {@code null}.
     */
    /* package */ boolean setLastResult(final @Nullable Object lastResult) {
        this.lastResult = lastResult;
        return lastResult != null;
    }

    /**
     * {@link #advance() Advance} 
     * @param lastResult The result.
     * @return if the given resultw as not {@code null}.
     */
    /* package */ boolean advanceIfSet(final @Nullable Object lastResult) {
        if (lastResult != null) {
            this.setLastResult(lastResult);
            this.advance();
            return true;
        }
        return false;
    }

    /**
     * Returns the current parser index.
     * @return The current parser index.
     */
    /* package */ int currentIndex() {
        return this.index;
    }

    /**
     * Returns {@code true} if the parser is at the start of the input.
     * @return {@code true} if the parser is at the start of the input.
     */
    /* package */ boolean isAtStart() {
        return this.index == 0;
    }

    /**
     * Returns {@code true} if the parser is at the end of the input.
     * @return {@code true} if the parser is at the end of the input.
     */
    /* package */ boolean isAtEnd() {
        return this.index >= this.text.length();
    }

    /**
     * Returns the character at the current parser index of the input. If the parser index is at the end of the input
     * then {@code NULL} ({@code \0}) is returned.
     * @return The character at the current parser index of the input.
     */
    /* package */ char current() {
        final int currentIndex = this.index;
        if (currentIndex == this.text.length()) {
            return '\0';
        } else {
            return this.text.charAt(this.index);
        }
    }

    /**
     * Call {@link #advance()} followed by {@link #mark()}
     */
    /* package */ void advanceAndMark() {
        this.advance();
        this.mark();
    }

    /**
     * Advance the parser to the next character and return it. {@code NULL} ({@code \0}) is returned when the parser has
     * reached the end of the input.
     * @return The next character, or {@code \0}.
     */
    /* package */ char advance() {
        if (this.isAtEnd()) {
            return '\0';
        }
        final int next = ++this.index;
        if (this.isAtEnd()) {
            return '\0';
        } else {
            return this.text.charAt(next);
        }
    }

    /**
     * Mark the current index of the parser, to be used later by {@link #substringFromMark()}.
     */
    /* package */ void mark() {
        this.mark = this.currentIndex();
    }

    /**
     * Returns the current marked index from {@link #mark()}.
     * @return The current marked index.
     */
    /* package */ int getMark() {
        return this.mark;
    }

    /**
     * Return the substring made from the current marked index (from {@link #mark()}) to the current parser index.
     * @return The substring from the marked index to the current parser index.
     */
    /* package */ @NotNull String substringFromMark() {
        return this.text.substring(this.mark, this.index);
    }

    @Override
    public String toString() {
        return this.text;
    }
}
