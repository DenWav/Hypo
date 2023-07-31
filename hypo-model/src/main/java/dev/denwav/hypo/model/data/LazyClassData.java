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

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
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
     * {@code compute} variant of {@link #isSynthetic()}.
     *
     * @return {@code true} if this class is synthetic.
     */
    public abstract boolean computeIsSynthetic();

    /**
     * {@code compute} variant of {@link #isSealed()}.
     *
     * @return {@code true} if this class is sealed.
     */
    public abstract boolean computeIsSealed();

    /**
     * {@code compute} variant of {@link #permittedClasses()}.
     *
     * @return The set of permitted classes for this sealed class.
     * @throws IOException If an IO error occurs while reading the permitted classes.
     */
    public abstract @Nullable List<ClassData> computePermittedClasses() throws IOException;

    /**
     * {@code compute} variant of {@link #recordComponents()}.
     *
     * @return The list of fields representing the components of this record.
     */
    public abstract @Nullable List<@NotNull FieldData> computeRecordComponents();

    /**
     * {@code compute} variant of {@link #kind()}.
     *
     * @return The {@link ClassKind kind} of this class.
     * @deprecated Because of {@link ClassData#kind()}. Define {@link #computeClassKinds()} instead.
     */
    @Deprecated
    public @NotNull ClassKind computeClassKind() {
        throw new IllegalStateException();
    }

    /**
     * Compute variant of {@link #kinds()}.
     *
     * @return The {@link ClassKind kinds} of this class.
     */
    public abstract @NotNull EnumSet<ClassKind> computeClassKinds();

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
    public abstract @NotNull List<ClassData> computeInterfaces() throws IOException;

    /**
     * {@code compute} variant of {@link #fields()}.
     *
     * @return This class's declared fields.
     */
    public abstract @NotNull List<FieldData> computeFields();

    /**
     * {@code compute} variant of {@link #methods()}.
     *
     * @return This class's declared methods.
     */
    public abstract @NotNull List<MethodData> computeMethods();

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

    private final LazyValue<Boolean, ?> isSynthetic = LazyValue.of(this::computeIsSynthetic);
    @Override
    public boolean isSynthetic() {
        return this.isSynthetic.getNotNull();
    }

    private final LazyValue<Boolean, ?> isSealed = LazyValue.of(this::computeIsSealed);
    @Override
    public boolean isSealed() {
        return this.isSealed.getNotNull();
    }

    private final LazyValue<List<ClassData>, IOException> permittedClasses = LazyValue.of(this::computePermittedClasses);
    @Override
    public @Nullable List<ClassData> permittedClasses() throws IOException {
        return this.permittedClasses.getOrThrow();
    }

    private final LazyValue<List<FieldData>, ?> recordComponents = LazyValue.of(this::computeRecordComponents);
    @Override
    public @Nullable List<@NotNull FieldData> recordComponents() {
        return this.recordComponents.get();
    }

    private final @NotNull LazyValue<EnumSet<ClassKind>, ?> kinds = LazyValue.of(this::computeClassKinds);
    @Override
    public @NotNull EnumSet<ClassKind> kinds() {
        return this.kinds.getNotNull();
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

    private final @NotNull LazyValue<List<ClassData>, IOException> interfaces = LazyValue.of(this::computeInterfaces);
    @Override
    public @NotNull List<ClassData> interfaces() throws IOException {
        return this.interfaces.getOrThrowNotNull();
    }

    private final @NotNull LazyValue<List<FieldData>, ?> fields = LazyValue.of(this::computeFields);
    @Override
    public @NotNull List<FieldData> fields() {
        return this.fields.getNotNull();
    }

    private final @NotNull LazyValue<List<MethodData>, ?> methods = LazyValue.of(this::computeMethods);
    @Override
    public @NotNull List<MethodData> methods() {
        return this.methods.getNotNull();
    }
}
