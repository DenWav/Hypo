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

package com.demonwav.hypo.model;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.jetbrains.annotations.NotNull;

/**
 * Proxy class for creating system {@link ClassProviderRoot class provider roots}. System roots provide system JDK
 * classes. {@link #newInstance()} returns an instance of {@link ClassProviderRoot} compatible with the currently
 * running JDK.
 */
final class SystemClassProviderRoot {

    private static final @NotNull Constructor<? extends ClassProviderRoot> constructor;

    static {
        Constructor<? extends ClassProviderRoot> c;
        try {
            // Try to load the JDK9+ version
            // If it succeeds then we know we're running at least on Java 9
            // If it fails, fall back to Java 8
            c = Class.forName(SystemClassProviderRoot.class.getName() + "Jdk9")
                .asSubclass(ClassProviderRoot.class).getDeclaredConstructor();
        } catch (final Throwable t) {
            try {
                c = Class.forName(SystemClassProviderRoot.class.getName() + "Jdk8")
                    .asSubclass(ClassProviderRoot.class).getDeclaredConstructor();
            } catch (final Throwable t2) {
                throw new AssertionError(HypoModelUtil.addSuppressed(t, t2));
            }
        }

        constructor = c;
    }

    /**
     * Returns a new instance of the system class provider root compatible with the currently running JVM.
     *
     * @return A new instance of the system class provider root compatible with the currently running JVM.
     * @throws IOException If an IO error occurs while creating the system class provider root.
     */
    static @NotNull ClassProviderRoot newInstance() throws IOException {
        try {
            return constructor.newInstance();
        } catch (final InvocationTargetException e) {
            final Throwable cause = e.getCause();
            // Satisfy this method's `throws` clause to make the compiler happy
            if (cause instanceof IOException) {
                throw (IOException) cause;
            }
            throw HypoModelUtil.rethrow(e.getCause());
        } catch (final InstantiationException | IllegalAccessException e) {
            // impossible
            throw new LinkageError("Failed to load " + SystemClassProviderRoot.class.getName() + " implementation " +
                "for the current Java version", e);
        }
    }

    private SystemClassProviderRoot() {}
}
