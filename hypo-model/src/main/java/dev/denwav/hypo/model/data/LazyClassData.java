/*
 * Hypo, an extensible and pluggable Java bytecode analytical model.
 *
 * Copyright (C) 2021  Kyle Wood (DenWav)
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

import java.io.IOException;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Base implementation of {@link ClassData} which lazily retrieves and caches individual values of the class data. Each
 * of the original methods of {@link ClassData} are implemented in this class with a corresponding {@code compute}
 * abstract method to implement instead. The compute method will only be called a single time when it is first
 * requested, the value returned by each {@code compute} method will then be cached for any subsequent accesses to that
 * value.
 *
 * <p>The purpose of this class is to improve the performance of methods which require parsing the class structure to build
 * new Hypo objects, such as the field and method access methods, or methods which load other classes. For flexibility
 * and completeness however, this class does implement all methods. Any methods which are not expensive to compute
 * (where the overhead of managing the lazy value may be even higher) can be overridden directly again, and the
 * {@code compute} variant ignored.
 *
 * @see LazyValue
 */
public abstract class LazyClassData extends AbstractClassData {

    /**
     * {@code compute} variant of {@link #name()}.
     *
     * @return The name of the class.
     */
    public abstract @NotNull String computeName();

    /**
     * {@code compute} variant of {@link #outerClass()}.
     *
     * @return The outer class of this class.
     * @throws IOException If an IO error occurs while reading the class.
     */
    public abstract @Nullable ClassData computeOuterClass() throws IOException;

    /**
     * {@code compute} variant of {@link #isStaticInnerClass()}.
     *
     * @return {@code true} if this class is a {@code static} inner class.
     */
    public abstract boolean computeStaticInnerClass();

    /**
     * {@code compute} variant of {@link #isFinal()}.
     *
     * @return {@code true} if this class is {@code final}.
     */
    public abstract boolean computeIsFinal();

    /**
     * {@code compute} variant of {@link #kind()}.
     *
     * @return The {@link ClassKind kind} of this class.
     */
    public abstract @NotNull ClassKind computeClassKind();

    /**
     * {@code compute} variant of {@link #visibility()}.
     *
     * @return The {@link Visibility visibility} of this class.
     */
    public abstract @NotNull Visibility computeVisibility();

    /**
     * {@code compute} variant of {@link #superClass()}.
     *
     * @return This class's super class.
     * @throws IOException If an IO error occurs while reading the class.
     */
    public abstract @Nullable ClassData computeSuperClass() throws IOException;

    /**
     * {@code compute} variant of {@link #interfaces()}.
     *
     * @return This class's interfaces.
     * @throws IOException If an IO error occurs while reading the interface classes.
     */
    public abstract @NotNull Set<ClassData> computeInterfaces() throws IOException;

    /**
     * {@code compute} variant of {@link #fields()}.
     *
     * @return This class's declared fields.
     */
    public abstract @NotNull Set<FieldData> computeFields();

    /**
     * {@code compute} variant of {@link #methods()}.
     *
     * @return This class's declared methods.
     */
    public abstract @NotNull Set<MethodData> computeMethods();

    private final @NotNull LazyValue<String, ?> name = LazyValue.of(this::computeName);
    @Override
    public @NotNull String name() {
        return this.name.getNotNull();
    }

    private final @NotNull LazyValue<ClassData, IOException> outerClass = LazyValue.of(this::computeOuterClass);
    @Override
    public @Nullable ClassData outerClass() throws IOException {
        return this.outerClass.getOrThrow();
    }

    private final LazyValue<Boolean, ?> staticInnerClass = LazyValue.of(this::computeStaticInnerClass);
    @Override
    public boolean isStaticInnerClass() {
        return this.staticInnerClass.getNotNull();
    }

    private final LazyValue<Boolean, ?> isFinal = LazyValue.of(this::computeIsFinal);
    @Override
    public boolean isFinal() {
        return this.isFinal.getNotNull();
    }

    private final @NotNull LazyValue<ClassKind, ?> classKind = LazyValue.of(this::computeClassKind);
    @Override
    public @NotNull ClassKind kind() {
        return this.classKind.getNotNull();
    }

    private final @NotNull LazyValue<@NotNull Visibility, ?> visibility = LazyValue.of(this::computeVisibility);
    @Override
    public @NotNull Visibility visibility() {
        return this.visibility.getNotNull();
    }

    private final @NotNull LazyValue<ClassData, IOException> superClass = LazyValue.of(this::computeSuperClass);
    @Override
    public @Nullable ClassData superClass() throws IOException {
        return this.superClass.getOrThrow();
    }

    private final @NotNull LazyValue<Set<ClassData>, IOException> interfaces = LazyValue.of(this::computeInterfaces);
    @Override
    public @NotNull Set<ClassData> interfaces() throws IOException {
        return this.interfaces.getOrThrowNotNull();
    }

    private final @NotNull LazyValue<Set<FieldData>, ?> fields = LazyValue.of(this::computeFields);
    @Override
    public @NotNull Set<FieldData> fields() {
        return this.fields.getNotNull();
    }

    private final @NotNull LazyValue<Set<MethodData>, ?> methods = LazyValue.of(this::computeMethods);
    @Override
    public @NotNull Set<MethodData> methods() {
        return this.methods.getNotNull();
    }
}
