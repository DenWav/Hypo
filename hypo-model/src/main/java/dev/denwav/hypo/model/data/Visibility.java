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

package dev.denwav.hypo.model.data;

import java.util.Locale;
import org.jetbrains.annotations.NotNull;

/**
 * The visibility of Java elements. This applies to all Java members (fields and methods) as well as all Java classes.
 */
public enum Visibility {
    /**
     * Public, accessible anywhere.
     */
    PUBLIC,
    /**
     * Package-private, also known as "default".
     */
    PACKAGE,
    /**
     * Protected, this only applies to class members: methods, fields, and inner classes.
     */
    PROTECTED,
    /**
     * Private, this only applies to class members: methods, fields, and inner classes.
     */
    PRIVATE;

    @Override
    public String toString() {
        return super.toString().toLowerCase(Locale.ENGLISH);
    }

    /**
     * Returns {@code true} if a method with the visibility of {@code this} is allowed to override a super method will
     * the given visibility.
     *
     * @param superVisibility The visibility of the super method to check.
     * @return {@code true} if this visibility would be allowed to override the given visibility.
     */
    public boolean canOverride(final @NotNull Visibility superVisibility) {
        switch (superVisibility) {
            case PUBLIC:
                return this == PUBLIC;
            case PACKAGE:
                return this != PRIVATE;
            case PROTECTED:
                return this == PUBLIC || this == PROTECTED;
            case PRIVATE:
            default:
                return false;
        }
    }
}
