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

package dev.denwav.hypo.model;

import dev.denwav.hypo.model.data.ClassData;
import dev.denwav.hypo.types.desc.ClassTypeDescriptor;
import dev.denwav.hypo.types.desc.TypeDescriptor;
import java.io.IOException;
import java.util.Collection;
import java.util.stream.Stream;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The core source of {@link ClassData} objects, retrieved from a given class name, and for enumerating all the
 * classes available in the set. This interface does not specify where the class file data is coming from, or even what
 * format they are in - only two things are needed to implement this interface:
 *
 * <ol>
 *     <li>{@link ClassData} objects are returned for a given class name, unless that class is not present.</li>
 *     <li>All classes available to this provider can be enumerated as individual {@link ClassData} objects.</li>
 * </ol>
 *
 * <p>The enumeration of all classes using the {@link #allClasses()} method provides no ordering guarantee.
 *
 * <p>When a {@link ClassData} object is returned either using one of the {@link #findClass(String) findClass()}
 * methods, or through enumerating over {@link #allClasses()}, the same {@link ClassData} instance will be returned
 * again if that class name is requested again such that the following returns {@code true}:
 *
 * <pre>
 *     ClassData data = ...;
 *     data == provider.findClass(data.name())
 * </pre>
 *
 * <p>All implementations of {@link ClassDataProvider} must be read-only and must be safe to be accessed by multiple
 * threads concurrently.
 *
 * <p>As with the rest of Hypo, all class names should be internal JVM names. Names passed to a provider will always be
 * first normalized with {@link HypoModelUtil#normalizedClassName(String)} before use.
 *
 * <p>As providers generally use file system resources of some kind to retrieve class data, this class is also
 * {@link AutoCloseable}, and you must {@link #close() close} it when you are finished.
 *
 * @see AbstractClassDataProvider
 * @see ClassProviderRoot
 */
public interface ClassDataProvider extends AutoCloseable {

    /**
     * Set the {@link ClassDataDecorator} which will be called on every {@link ClassData} object produced by this
     * provider. User code probably shouldn't call this method.
     *
     * @param decorator The {@link ClassDataDecorator} to use.
     */
    void setDecorator(final @NotNull ClassDataDecorator decorator);

    /**
     * Set {@code true} if this provider represents a context provider, {@code false} if not. User code probably
     * shouldn't call this method.
     *
     * @param contextClassProvider {@code true} if this is a context provider, {@code false} if not.
     */
    void setContextClassProvider(final boolean contextClassProvider);

    /**
     * Get whether this is a context provider.
     *
     * @return {@code true} if this is a context provider, {@code false} if not.
     */
    boolean isContextClassProvider();

    /**
     * Set {@code true} if the current context configuration is set to require full classpath to be present. User code
     * probably shouldn't call this method.
     *
     * @param requireFullClasspath {@code true} if the full classpath is required, {@code false} if not.
     */
    void setRequireFullClasspath(final boolean requireFullClasspath);

    /**
     * Get whether the full classpath is required.
     *
     * @return {@code true} if the full classpath is required, {@code false} if not.
     */
    boolean isRequireFullClasspath();

    /**
     * Return the {@link ClassData} object corresponding with the given class name, if it can be found. This method
     * will always return the same instance of {@link ClassData} for a given name such that the following returns
     * {@code true}:
     *
     * <pre>
     *     ClassData data = ...;
     *     data == provider.findClass(data.name())
     * </pre>
     *
     * <p>The class name should be in the internal JVM name format, or something close to it. The name will always be
     * passed first to {@link HypoModelUtil#normalizedClassName(String)}.
     *
     * @param className The internal JVM name of the class.
     * @return The parsed {@link ClassData} object corresponding with the given name, or {@code null} if the class name
     * cannot be found.
     * @throws IOException If an IO error occurs while attempting to read the class file.
     * @see #findClass(TypeDescriptor)
     */
    @Contract("null -> null")
    @Nullable ClassData findClass(final @Nullable String className) throws IOException;

    /**
     * This is a convenience method for resolving the {@link ClassData} object corresponding to a give
     * {@link TypeDescriptor}. The {@link TypeDescriptor} passed to this method must be a {@link ClassTypeDescriptor},
     * or this method will always return {@code null}.
     *
     * <p>This method is implemented by default as:
     *
     * <pre>
     *     return this.findClass(type.asInternalName());
     * </pre>
     *
     * <p>and as such has identical semantics to {@link #findClass(String)}. Any implementations which override this
     * method must match these semantics to fully satisfy this method's contact.
     *
     * @param type The {@link TypeDescriptor} of the class to find. Must be an instance of {@link ClassTypeDescriptor}
     *             or this method will always return {@code null}.
     * @return The {@link ClassData} object corresponding with the given {@link TypeDescriptor}, or {@code null} if the
     *         type is not a {@link ClassTypeDescriptor} or the class name cannot be found.
     * @throws IOException If an IO error occurs while attempting to read the class file.
     * @see #findClass(String)
     */
    @Contract("null -> null")
    default @Nullable ClassData findClass(final @Nullable TypeDescriptor type) throws IOException {
        if (!(type instanceof ClassTypeDescriptor)) {
            return null;
        }
        return this.findClass(type.asInternal());
    }

    /**
     * Returns an {@link Iterable} which will iterate over all classes available in this provider. By default, this
     * method simply calls {@link #stream()} and returns that stream's iterator. The default implementation of
     * {@link #stream()} in {@link AbstractClassDataProvider} is implemented to lazily load {@link ClassData} objects,
     * however this is not a requirement of implementing this method.
     *
     * <p>Classes loaded by this method will also satisfy the identity requirement of this provider. That means classes
     * returned by this iterator will be the same instance as classes requested by the {@link #findClass(String)}
     * method when requesting the same class name such that the following returns {@code true}:
     *
     * <pre>
     *     ClassData data = provider.allClasses().next();
     *     data == provider.findClass(data.name())
     * </pre>
     *
     * <p>This will be true regardless of the order the classes are retrieved, either by the {@link #findClass(String)}
     * method or by this method (or any other method).
     *
     * @return An {@link Iterable} which will iterate over all classes available in this provider.
     */
    default @NotNull Iterable<ClassData> allClasses() {
        return () -> {
            try {
                return this.stream().iterator();
            } catch (final IOException e) {
                throw HypoModelUtil.rethrow(e);
            }
        };
    }

    /**
     * Returns a {@link Stream} which will iterate over all classes available in this provider. The default
     * implementation of this method in {@link AbstractClassDataProvider} is implemented to lazily load
     * {@link ClassData} objects, however this is not a requirement of implementing this method.
     *
     * <p>Classes loaded by this method will also satisfy the identity requirement of this provider. That means classes
     * returned by this iterator will be the same instance as classes requested by the {@link #findClass(String)}
     * method when requesting the same class name such that the following returns {@code true}:
     *
     * <pre>
     *     ClassData data = provider.stream().findFirst().get();
     *     data == provider.findClass(data.name())
     * </pre>
     *
     * <p>This will be true regardless of the order the classes are retrieved, either by the {@link #findClass(String)}
     * method or by this method (or any other method).
     *
     * @return A {@link Stream} which will iterate over all classes available in this provider.
     * @throws IOException If an IO error occurs while reading the classes.
     */
    @NotNull Stream<ClassData> stream() throws IOException;

    /**
     * Returns the collection of {@link ClassProviderRoot roots} used by this provider.
     *
     * @return The collection of {@link ClassProviderRoot roots} used by this provider.
     */
    @NotNull Collection<ClassProviderRoot> roots();

    @Override
    void close() throws IOException;
}
