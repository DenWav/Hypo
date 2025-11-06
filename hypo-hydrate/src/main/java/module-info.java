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
 * Hydration fills in additional information to the class data model, the primary entrypoint being
 * {@link dev.denwav.hypo.hydrate.HydrationManager HydrationManager}.
 */
module dev.denwav.hypo.hydrate {
    requires transitive dev.denwav.hypo.core;

    requires static transitive org.jetbrains.annotations;

    requires org.jgrapht.core;

    exports dev.denwav.hypo.hydrate;
    exports dev.denwav.hypo.hydrate.generic;
}
