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

import dev.denwav.hypo.model.ClassDataProvider;
import dev.denwav.hypo.model.HypoModelUtil;
import dev.denwav.hypo.model.data.types.JvmType;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Core Java class file model. The methods of this class are intended to provide simple structural information about
 * the class file represented by this object. Extra data can always be placed in this model using {@link HypoData}
 * methods.
 *
 * <p>{@link Object#equals(Object) equals()}, {@link Object#hashCode() hashCode()}, and
 * {@link Object#toString() toString()} are all written purely against the {@link #name()} of the class. This is to
 * prevent poor performance from using {@link ClassData} objects directly in data structures, and to match the behavior
 * of Java classes loaded in the JVM - that is to say Java classes are identified by their name. If there are two
 * classes on the classpath with the same name that is undefined behavior both at runtime and in Hypo.
 *
 * <p>To reduce repeated boilerplate and to prevent ambiguity the javadoc in this class will use the term "class data" to
 * refer to the particular class that this object represents.
 */
public interface ClassData extends HypoData {

    /**
     * Set the {@link ClassDataProvider} this should use for additional class lookups. This method is expected to be
     * called immediately after construction by the {@link ClassDataProvider} which creates it, not by client code.
     *
     * @param provider The {@link ClassDataProvider} to use for additional class lookups.
     */
    void setProvider(final @NotNull ClassDataProvider provider);

    /**
     * Set whether this class data was loaded by the
     * {@link ClassDataProvider#setContextClassProvider(boolean)} context provider.
     *
     * @param contextClass {@code true} if this class data was loaded by the context provider.
     * @see ClassDataProvider#setContextClassProvider(boolean)
     */
    void setContextClass(final boolean contextClass);

    /**
     * Returns {@code true} if this class data was loaded by the
     * {@link ClassDataProvider#isContextClassProvider() context provider}.
     *
     * @return {@code true} if this class data was loaded by the context provider.
     * @see ClassDataProvider#isContextClassProvider()
     */
    boolean isContextClass();

    /**
     * Set whether additional class lookups made by this class which fail should result in an exception.
     *
     * @param requireFullClasspath {@code true} if class lookup failures from this class should result in an error.
     * @see ClassDataProvider#setRequireFullClasspath(boolean)
     */
    void setRequireFullClasspath(final boolean requireFullClasspath);

    /**
     * Returns {@code true} if this class will throw an exception for class lookups which fail.
     *
     * @return {@code true} if this class will throw an exception for class lookups which fail.
     * @see ClassDataProvider#isRequireFullClasspath()
     */
    boolean isRequireFullClasspath();

    /**
     * The name of this class data, in JVM internal format.
     *
     * @return The name of the class represented by this object.
     */
    @NotNull String name();

    /**
     * This class data's outer class data, or {@code null} if this class data does not have an outer class.
     *
     * @return This class data's outer class data.
     * @throws IOException If an IO error occurs while reading the outer class.
     */
    @Nullable ClassData outerClass() throws IOException;

    /**
     * Returns {@code true} if this class data is a {@code static} inner class.
     * @return {@code true} if this class data is a {@code static} inner class.
     */
    boolean isStaticInnerClass();

    /**
     * Returns {@code true} if this class data is {@code final}.
     * @return {@code true} if this class data is {@code final}.
     */
    boolean isFinal();

    /**
     * Returns {@code true} if this class data is synthetic.
     *
     * @return {@code true} if this class data is synthetic.
     */
    boolean isSynthetic();

    /**
     * Get the {@link ClassKind kind} of this class data.
     *
     * @return The kind of class this class data is.
     * @see ClassKind
     */
    @NotNull ClassKind kind();

    /**
     * Get the {@link Visibility visibility} of this class data.
     *
     * @return The visibility of this class data.
     * @see Visibility
     */
    @NotNull Visibility visibility();

    /**
     * Get this class data's super class data, or {@code null} if this class data does not have a super class.
     *
     * @return The super class data of this class data.
     * @throws IOException If an IO error occurs while reading the super class data.
     */
    @Nullable ClassData superClass() throws IOException;

    /**
     * Get the set of interface class datas this class data implements. If this class data doesn't implement any
     * interfaces this method returns an empty set.
     *
     * @return The set of classes this class data implements.
     * @throws IOException If an IO error occurs while reading the interfaces.
     */
    @NotNull Set<ClassData> interfaces() throws IOException;

    /**
     * Returns {@code true} if this class data is {@code sealed}.
     * @return {@code true} if this class data is {@code sealed}.
     */
    boolean isSealed();

    /**
     * If this class data {@link #isSealed() represents a sealed class}, return the set of classes which are permitted
     * to extend this one. For any classes which aren't sealed, {@code null} is returned.
     *
     * @return The set of classes which are permitted to extend this sealed class.
     * @throws IOException If an IO error occurs while reading one of the permitted classes.
     */
    @Nullable Set<@NotNull ClassData> permittedClasses() throws IOException;

    /**
     * If this class data {@link ClassKind#RECORD represents a record}, return the set of fields associated with the
     * components of this record. For any classes which aren't records, {@code null} is returned.
     *
     * @return the list of fields reprsenting the components of this record.
     */
    @ApiStatus.Experimental
    @Nullable List<@NotNull FieldData> recordComponents();

    /**
     * Return a {@link Stream} which iterates over all class datas this class data either extends or implements.
     *
     * @return A stream which iterates over all class datas this class data extends or implements.
     * @throws IOException If an IO error occurs while reading one of the super classes.
     */
    default @NotNull Stream<@NotNull ClassData> allSuperClasses() throws IOException {
        final ClassData superClass = this.superClass();
        final Set<ClassData> superInterfaces = this.interfaces();
        if (superClass == null && superInterfaces.isEmpty()) {
            return Stream.empty();
        } else if (superClass == null) {
            return superInterfaces.stream();
        } else if (superInterfaces.isEmpty()) {
            return Stream.of(superClass);
        } else {
            final ClassData[] res = new ClassData[superInterfaces.size() + 1];
            res[0] = superClass;
            int index = 1;
            for (ClassData superInterface : superInterfaces) {
                res[index++] = superInterface;
            }
            return Arrays.stream(res);
        }
    }

    /**
     * Run the given {@link HypoModelUtil.ThrowingConsumer consumer} against each class data this class data either
     * extends or implements.
     *
     * @param consumer The consumer to run against each class data this class data either extends or implements.
     * @param <X> The type of the exception the consumer can throw.
     * @throws IOException If an IO error occurs while reading one of the super classes.
     * @throws X If the consumer throws an exception.
     */
    default <X extends Throwable> void forEachSuperClass(
        final @NotNull HypoModelUtil.ThrowingConsumer<@NotNull ClassData, X> consumer
    ) throws IOException, X {
        final ClassData superClass = this.superClass();
        if (superClass != null) {
            consumer.acceptThrowing(superClass);
        }
        for (final ClassData superInter : this.interfaces()) {
            consumer.acceptThrowing(superInter);
        }
    }

    /**
     * Get a set of all fields this class data declares.
     *
     * @return A set of all fields this class data declares.
     */
    @NotNull Set<FieldData> fields();

    /**
     * Get a set of all methods this class data declares.
     *
     * @return A set of all methods this class data declares.
     */
    @NotNull Set<MethodData> methods();

    /**
     * Return {@code true} if this class data extends the given class data. This method walks up the
     * {@link #superClass()} chain, it does not check interfaces.
     *
     * @param that The class data to check if this class data extends it.
     * @return {@code true} if this class data extends the given class data.
     * @see #doesImplement(ClassData)
     * @see #doesExtendOrImplement(ClassData)
     */
    default boolean doesExtend(final @NotNull ClassData that) {
        if (this.equals(that)) {
            return true;
        }
        try {
            final ClassData superClass = this.superClass();
            if (superClass != null) {
                return superClass.doesExtend(that);
            }
        } catch (final IOException ignored) {
            return false;
        }
        return false;
    }

    /**
     * Return {@code true} if this class data implements the given class data. This method walks up the
     * {@link #interfaces()} chain, it does not check super classes.
     *
     * @param that The class data to check if this class data implements it.
     * @return {@code true} if this class data implements the given class data.
     * @see #doesExtend(ClassData)
     * @see #doesExtendOrImplement(ClassData)
     */
    default boolean doesImplement(final @NotNull ClassData that) {
        if (this.equals(that)) {
            return true;
        }
        try {
            for (final ClassData iface : this.interfaces()) {
                if (iface != null) {
                    return iface.doesImplement(that);
                }
            }
        } catch (final IOException ignored) {
            return false;
        }
        return false;
    }

    /**
     * Return {@code true} if this class data extends or implements the given class data. This method walks up both the
     * {@link #superClass()} and {@link #interfaces()} chain.
     *
     * @param that The class data to check if this class data extends or implements it.
     * @return {@code true} if this class data extends or implements the given class data.
     * @see #doesExtend(ClassData)
     * @see #doesImplement(ClassData)
     */
    default boolean doesExtendOrImplement(final @NotNull ClassData that) {
        if (this.equals(that)) {
            return true;
        }
        try {
            return this.allSuperClasses().anyMatch(s -> s.doesExtendOrImplement(that));
        } catch (final IOException ignored) {
            return false;
        }
    }

    /**
     * Get a set of all fields this class data declares which have the given name.
     *
     * @param name The name of the fields to find.
     * @return The set of fields this class data declares which have the given name.
     */
    @SuppressWarnings("DuplicatedCode")
    default @NotNull Set<@NotNull FieldData> fields(final @NotNull String name) {
        LinkedHashSet<FieldData> result = null;
        FieldData singleResult = null;

        for (final FieldData field : this.fields()) {
            if (field.name().equals(name)) {
                if (singleResult == null) {
                    singleResult = field;
                } else if (result == null) {
                    result = new LinkedHashSet<>();
                    result.add(singleResult);
                    result.add(field);
                } else {
                    result.add(field);
                }
            }
        }
        if (result != null) {
            return Collections.unmodifiableSet(result);
        } else if (singleResult != null) {
            return Collections.singleton(singleResult);
        } else {
            return Collections.emptySet();
        }
    }

    /**
     * Get the field data this class data declares with the name and type given. Returns {@code null} if this class data
     * does not declare a field with the given name and type.
     *
     * @param name The name of the field to find.
     * @param type The type of the field to find.
     * @return The field data this class declares with the given name and type, or {@code null} if it can't be found.
     */
    default @Nullable FieldData field(final @NotNull String name, final @NotNull JvmType type) {
        for (final FieldData field : this.fields()) {
            if (field.name().equals(name) && field.fieldType().equals(type)) {
                return field;
            }
        }
        return null;
    }

    /**
     * Get a set of all methods this class data declares which have the given name.
     *
     * @param name The name of the methods to find.
     * @return The set of methods this class data declares which have the given name.
     */
    @SuppressWarnings("DuplicatedCode")
    default @NotNull Set<@NotNull MethodData> methods(final @NotNull String name) {
        LinkedHashSet<MethodData> result = null;
        MethodData singleResult = null;

        for (final MethodData method : this.methods()) {
            if (method.name().equals(name)) {
                if (singleResult == null) {
                    singleResult = method;
                } else if (result == null) {
                    result = new LinkedHashSet<>();
                    result.add(singleResult);
                    result.add(method);
                } else {
                    result.add(method);
                }
            }
        }

        if (result != null) {
            return Collections.unmodifiableSet(result);
        } else if (singleResult != null) {
            return Collections.singleton(singleResult);
        } else {
            return Collections.emptySet();
        }
    }

    /**
     * Get the method data this class data declares with the name and descriptor given. Returns {@code null} if this
     * class data does not declare a method with the given name and descriptor.
     *
     * @param name The name of the field to find.
     * @param descriptor The type of the field to find.
     * @return The method data this class declares with the given name and descriptor, or {@code null} if it can't be
     *         found.
     */
    default @Nullable MethodData method(final @NotNull String name, final @NotNull MethodDescriptor descriptor) {
        for (final MethodData method : this.methods()) {
            if (method.name().equals(name) && method.descriptor().equals(descriptor)) {
                return method;
            }
        }
        return null;
    }

    // Hydration methods

    /**
     * Get the list of class datas which directly extends this class data. This method will always return an empty list
     * unless this class data has been hydrated.
     *
     * @return The list of classes which directly extend this class data.
     */
    @NotNull Set<@NotNull ClassData> childClasses();

    /**
     * Get the list of class datas which declare this class data as their outer class. This method will always return
     * an empty list unless this class data has been hydrated.
     *
     * @return The list of classes which declare this class data as their outer class.
     */
    @NotNull Set<@NotNull ClassData> innerClasses();
}
