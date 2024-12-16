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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* package */ final class ParserState {

    private final @NotNull String text;

    private int mark = -1;
    private int index;

    private @Nullable Object lastResult = null;

    ParserState(final @NotNull String text, final int from) {
        this.text = text;

        if (from < 0 || from > text.length()) {
            throw new IllegalArgumentException("Invalid start index for string: " + from);
        }
        this.index = from;
    }

    @SuppressWarnings("TypeParameterUnusedInFormals")
    public <T> @Nullable T getLastResultOrNull() {
        final Object res = this.lastResult;
        this.lastResult = null;
        return HypoTypesUtil.cast(res);
    }
    @SuppressWarnings("TypeParameterUnusedInFormals")
    public <T> @NotNull T getLastResult() {
        final T res = this.getLastResultOrNull();
        if (res == null) {
            throw new NullPointerException("No last result set");
        }
        return res;
    }

    public boolean setLastResult(final @Nullable Object lastResult) {
        this.lastResult = lastResult;
        return lastResult != null;
    }

    public boolean advanceIfSet(final @Nullable Object lastResult) {
        if (lastResult != null) {
            this.setLastResult(lastResult);
            this.advance();
            return true;
        }
        return false;
    }

    public int currentIndex() {
        return this.index;
    }

    public boolean isAtStart() {
        return this.index == 0;
    }

    public boolean isAtEnd() {
        return this.index >= this.text.length();
    }

    public char current() {
        final int currentIndex = this.index;
        if (currentIndex == this.text.length()) {
            return '\0';
        } else {
            return this.text.charAt(this.index);
        }
    }

    public void advanceAndMark() {
        this.advance();
        this.mark();
    }

    public char advance() {
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

    public void mark() {
        this.mark = this.currentIndex();
    }

    public int getMark() {
        return this.mark;
    }

    public @NotNull String substringFromMark() {
        return this.text.substring(this.mark, this.index);
    }

    @Override
    public String toString() {
        return this.text;
    }
}
