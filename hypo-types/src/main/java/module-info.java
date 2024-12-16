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

/**
 * This module contains value classes representing all aspects of the Java type
 * system, including descriptors and signatures, and covering value types as
 * well as method types. This module also includes Java type parsing utilities
 * as well as {@link dev.denwav.hypo.types.pattern pattern matching} utilities
 * for inspecting types.
 *
 * @see dev.denwav.hypo.types
 */
module dev.denwav.hypo.types {
    requires static transitive org.jetbrains.annotations;
    requires static transitive com.google.errorprone.annotations;
    requires jdk.jdi;

    exports dev.denwav.hypo.types;
    exports dev.denwav.hypo.types.desc;
    exports dev.denwav.hypo.types.intern;
    exports dev.denwav.hypo.types.kind;
    exports dev.denwav.hypo.types.parsing;
    exports dev.denwav.hypo.types.pattern;
    exports dev.denwav.hypo.types.sig;
    exports dev.denwav.hypo.types.sig.param;
}
