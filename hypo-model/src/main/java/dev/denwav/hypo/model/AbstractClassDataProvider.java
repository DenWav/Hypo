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

import com.google.errorprone.annotations.ForOverride;
import dev.denwav.hypo.model.data.ClassData;
import dev.denwav.hypo.model.data.ClassKind;
import dev.denwav.hypo.model.data.FieldData;
import dev.denwav.hypo.model.data.HypoKey;
import dev.denwav.hypo.model.data.MethodData;
import dev.denwav.hypo.model.data.Visibility;
import java.io.IOException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Default abstract implementation of {@link ClassDataProvider} based on {@link ClassProviderRoot}. Classes which
 * implement {@link ClassDataProvider} using this class as the base only need to override
 * {@link #parseClassData(byte[])}, this class implements all other requirements of {@link ClassDataProvider}, including
 * caching {@link ClassData} objects.
 *
 * <p>This is the standard method of implementing {@link ClassDataProvider} and automatically makes any implementations
 * based on this class compatible with any {@link ClassProviderRoot} implementations as well. It is, however, <i>not</i>
 * a requirement for classes which implement {@link ClassDataProvider} to extend this class.
 *
 * @see ClassDataProvider
 * @see ClassProviderRoot
 */
public abstract class AbstractClassDataProvider implements ClassDataProvider {

    /**
     * Marker object to place in {@link #cache} to mark a class name as non-existent. This object should <i>never</i> be
     * returned to clients nor should its methods ever be called.
     */
    private static final @NotNull ClassData NULL_DATA = createNullData();

    /**
     * Cache which stores mappings between class names and their subsequent {@link ClassData}. {@link #NULL_DATA} is
     * used as a marker value for classes which cannot be found.
     *
     * <p>All class names should be normalized with {@link HypoModelUtil#normalizedClassName(String)} before being
     * passed to this map.
     */
    private final @NotNull ConcurrentHashMap<String, ClassData> cache = new ConcurrentHashMap<>();
    /**
     * {@link ClassProviderRoot Roots} to use for discovering class file data.
     */
    private final @NotNull List<@NotNull ClassProviderRoot> rootProviders;

    /**
     * Decorator to use to decorate {@link ClassData} objects immediately after they are created. This <i>must</i> be
     * set prior to retrieving any classes from this provider, however it is usually not the responsibility of client
     * code to set this.
     *
     * @see #setDecorator(ClassDataDecorator)
     */
    private @Nullable ClassDataDecorator decorator = null;
    /**
     * @see #isContextClassProvider()
     */
    private boolean isContextClassProvider = false;
    /**
     * @see #isRequireFullClasspath()
     */
    private boolean isRequireFullClasspath = false;

    /**
     * Create a new {@link AbstractClassDataProvider} using the given {@link ClassProviderRoot root providers}.
     *
     * @param rootProviders The root providers to use as the data source in this class data provider.
     */
    protected AbstractClassDataProvider(final @NotNull List<@NotNull ClassProviderRoot> rootProviders) {
        this.rootProviders = rootProviders;
    }

    @Override
    public void setDecorator(final @NotNull ClassDataDecorator decorator) {
        this.decorator = decorator;
    }

    @Override
    public boolean isContextClassProvider() {
        return this.isContextClassProvider;
    }

    @Override
    public void setContextClassProvider(boolean contextClassProvider) {
        this.isContextClassProvider = contextClassProvider;
    }

    @Override
    public boolean isRequireFullClasspath() {
        return this.isRequireFullClasspath;
    }

    @Override
    public void setRequireFullClasspath(boolean requireFullClasspath) {
        this.isRequireFullClasspath = requireFullClasspath;
    }

    @Override
    @Contract("null -> null")
    public @Nullable ClassData findClass(final @Nullable String className) {
        if (className == null) {
            return null;
        }

        final ClassData result = this.cache.computeIfAbsent(this.normalize(className), k -> {
            try {
                final byte[] fileData = this.findFile(k + ".class");
                if (fileData == null) {
                    return NULL_DATA;
                }

                final ClassData res = this.parseClassData(fileData);
                return res != null ? this.decorate(res) : NULL_DATA;
            } catch (final IOException e) {
                throw HypoModelUtil.rethrow(e);
            }
        });
        return result != NULL_DATA ? result : null;
    }

    private byte @Nullable [] findFile(final @NotNull String fileName) throws IOException {
        for (final ClassProviderRoot rootProvider : this.rootProviders) {
            final byte[] data = rootProvider.getClassData(fileName);
            if (data != null) {
                return data;
            }
        }
        return null;
    }

    private @NotNull String normalize(final @NotNull String className) {
        String fullClassName = className.endsWith(".class") ? className.substring(0, className.length() - 6) : className;
        fullClassName = HypoModelUtil.normalizedClassName(fullClassName);
        if (fullClassName.startsWith("/")) {
            fullClassName = fullClassName.substring(1);
        }

        return fullClassName;
    }

    @Override
    public @NotNull Stream<ClassData> stream() throws IOException {
        return this.rootProviders.stream()
            .flatMap(HypoModelUtil.wrapFunction(r -> r.getAllClasses().stream()))
            .map(ref -> this.cache.computeIfAbsent(
                this.normalize(ref.name()),
                HypoModelUtil.wrapFunction(k -> {
                    final byte[] rawData = ref.readData();
                    if (rawData == null) {
                        return NULL_DATA;
                    }
                    final ClassData result = this.parseClassData(rawData);
                    return result != null ? this.decorate(result) : NULL_DATA;
                })
            ));
    }

    /**
     * <p>
     * Given file data, parse it into a new {@link ClassData} object. This method should not attempt to cache the
     * output, as this class already handles caching of {@link ClassData} objects. Instead, this method should solely
     * parse the data into whichever implementation of {@link ClassData} it supports based on the format of the format
     * of the class data, and return it.
     * </p><p>
     * This method should also not be concerned with decorating the parsed class data using {@link #decorator}, the base
     * implementation will also handle that.
     * </p>
     *
     * @param file The raw binary data of the file to parse.
     * @return A new instance of {@link ClassData} corresponding to the given class file data.
     * @throws IOException If an IO error occurs while parsing the class file.
     */
    @ForOverride
    protected abstract @Nullable ClassData parseClassData(final byte @NotNull [] file) throws IOException;

    @Contract("_ -> param1")
    private @NotNull ClassData decorate(final @NotNull ClassData classData) {
        if (this.decorator == null) {
            throw new IllegalStateException("ClassData requested before decorating ClassDataProvider");
        }
        this.decorator.decorate(classData);
        classData.setContextClass(this.isContextClassProvider());
        classData.setRequireFullClasspath(this.isRequireFullClasspath());
        return classData;
    }

    @Override
    public void close() throws IOException {
        IOException thrown = null;
        for (final ClassProviderRoot rootProvider : this.rootProviders) {
            try {
                rootProvider.close();
            } catch (final IOException e) {
                if (thrown == null) {
                    thrown = e;
                } else {
                    thrown.addSuppressed(e);
                }
            }
        }

        if (thrown != null) {
            throw thrown;
        }
    }

    @Override
    public @NotNull Collection<ClassProviderRoot> roots() {
        return this.rootProviders;
    }

    private static @NotNull ClassData createNullData() {
        return new ClassData() {
            @Override
            public void setProvider(final @NotNull ClassDataProvider provider) {
                throw new IllegalStateException();
            }

            @Override
            public boolean isContextClass() {
                throw new IllegalStateException();
            }

            @Override
            public void setContextClass(boolean contextClass) {
                throw new IllegalStateException();
            }

            @Override
            public boolean isRequireFullClasspath() {
                throw new IllegalStateException();
            }

            @Override
            public void setRequireFullClasspath(boolean requireFullClasspath) {
                throw new IllegalStateException();
            }

            @Override
            public @NotNull String name() {
                throw new IllegalStateException();
            }

            @Override
            public @Nullable ClassData outerClass() {
                throw new IllegalStateException();
            }

            @Override
            public boolean isStaticInnerClass() {
                throw new IllegalStateException();
            }

            @Override
            public boolean isFinal() {
                throw new IllegalStateException();
            }

            @Override
            public boolean isSynthetic() {
                throw new IllegalStateException();
            }

            @Override
            public boolean isSealed() {
                throw new IllegalStateException();
            }

            @Override
            public @Nullable List<ClassData> permittedClasses() {
                throw new IllegalStateException();
            }

            @Override
            public @Nullable List<@NotNull FieldData> recordComponents() {
                throw new IllegalStateException();
            }

            @Override
            public @NotNull EnumSet<ClassKind> kinds() {
                throw new IllegalStateException();
            }

            @Override
            public @NotNull Visibility visibility() {
                throw new IllegalStateException();
            }

            @Override
            public @Nullable ClassData superClass() {
                throw new IllegalStateException();
            }

            @Override
            public @NotNull List<ClassData> interfaces() {
                throw new IllegalStateException();
            }

            @Override
            public @NotNull List<FieldData> fields() {
                throw new IllegalStateException();
            }

            @Override
            public @NotNull List<MethodData> methods() {
                throw new IllegalStateException();
            }

            @Override
            public @NotNull Set<@NotNull ClassData> childClasses() {
                throw new IllegalStateException();
            }

            @Override
            public @NotNull Set<@NotNull ClassData> innerClasses() {
                throw new IllegalStateException();
            }

            @Override
            public <T> @Nullable T store(@NotNull HypoKey<T> key, @Nullable T t) {
                throw new IllegalStateException();
            }

            @Override
            public <T> @NotNull T compute(@NotNull HypoKey<T> key, @NotNull Supplier<T> supplier) {
                throw new IllegalStateException();
            }

            @Override
            public <T> @Nullable T get(@NotNull HypoKey<T> key) {
                throw new IllegalStateException();
            }

            @Override
            public boolean contains(@NotNull HypoKey<?> key) {
                throw new IllegalStateException();
            }
        };
    }
}
