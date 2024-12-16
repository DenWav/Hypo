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

import dev.denwav.hypo.types.HypoTypesUtil;
import java.util.function.Consumer;
import java.util.function.Function;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * General utility class used by all of Hypo. As this module, {@code hypo-model}, is the base model in Hypo's module
 * dependency graph, this utility class acts as the base generic utility class for the entire Hypo project as well.
 */
public final class HypoModelUtil {

    private HypoModelUtil() {}

    /**
     * Normalize the given class name into a standard format matching the JVM internal class name format. Class type
     * descriptors may also be passed in (that is, class names that looks like {@code Lcom/example/Main;}) and this
     * method will strip the initial {@code L} and the final {@code ;}.
     *
     * <p>This method does not validate the format of the class name provided, it simply runs a couple standard String
     * functions on it to help increase cache-hit rate and reduce the likelihood of a class name in a slightly different
     * format causing issues.
     *
     * @param className The class name to normalize.
     * @return The normalized class name.
     */
    public static @NotNull String normalizedClassName(final @NotNull String className) {
        return HypoTypesUtil.normalizedClassName(className);
    }

    /**
     * Perform an unsafe, unchecked cast to the type specified by {@link T}. This method does not in any way verify or
     * validate that the cast will succeed, nor does it protect from failed casts from occurring. This method's intended
     * purpose is for handing cases where Java's type system falls short, such as when handling capture types. Any code
     * which uses this method should independently verify that the cast will never fail on their own.
     *
     * @param o The object to cast to {@link T}. Can be {@code null}, if so then {@code null} will be returned.
     * @param <T> The type to cast {@code o} to.
     * @return The given parameter {@code o}, cast to {@link T}.
     */
    @SuppressWarnings("TypeParameterUnusedInFormals")
    @Contract(value = "_ -> param1", pure = true)
    public static <T> @Nullable T cast(final @Nullable Object o) {
        return HypoTypesUtil.cast(o);
    }

    /**
     * Rethrow the given {@link Throwable} {@code x} as if it were an unchecked exception, regardless if {@code x}
     * actually is unchecked or not. This method does not wrap the given exception. This method returns
     * {@link RuntimeException} purely to allow you to re-throw the result to tell the compiler that an unchecked
     * exception will be thrown - in reality this method never returns.
     *
     * <p>For example, the intended pattern for using this method when the compiler needs to know the code path will not
     * continue (such as when initializing variables, etc.) is this:
     *
     * <pre>
     *     try {
     *         ...
     *     } catch (Exception e) {
     *         throw HypoModelUtil.rethrow(e);
     *     }
     * </pre>
     *
     * <p>The actual execution of the code is entirely unchanged regardless of whether the {@code throw} in the above
     * code snippet exists, however including the {@code throw} keyword helps the Java compiler better understand the
     * code.
     *
     * @param t The {@link Throwable} to unconditionally re-throw as unchecked.
     * @param <X> Generic hack to allow this method to trick the Java compiler into allowing this.
     * @return A {@link RuntimeException} to help tell the Java compiler an exception will be thrown.
     * @throws X Generic hack to allow this method to trick the Java compiler into allowing this.
     */
    @Contract("_ -> fail")
    @SuppressWarnings("unchecked")
    public static <X extends Throwable> RuntimeException rethrow(final @NotNull Throwable t) throws X {
        throw (X) t;
    }

    /**
     * Allow the creation of a {@link ThrowingFunction}, which also implements {@link Function}. This can be used to
     * allow checked exceptions inside {@link Function} lambda expressions. Any exceptions thrown in this block will
     * be re-thrown as unchecked using {@link #rethrow(Throwable)}.
     *
     * <p>This method simply returns the {@link ThrowingFunction} directly, this method only acts as a shorthand for the
     * following code (notably not needing to specify generic types):
     *
     * <pre>
     *    {@literal someMethod(HypoModelUtil.wrapFunction(t -> t))}
     *    // Shorthand for:
     *    {@literal someMethod((ThrowingFunction<String, String, ?>) k -> t)}
     * </pre>
     *
     * @param func The function to return.
     * @param <T> The input type of the function.
     * @param <R> The return type of the function.
     * @param <X> The exception the function may throw.
     * @return The given function as-is.
     */
    @Contract(value = "_ -> param1", pure = true)
    public static <T, R, X extends Throwable> ThrowingFunction<T, R, X> wrapFunction(
        final @NotNull ThrowingFunction<T, R, X> func
    ) {
        return func;
    }

    /**
     * Allow the creation of a {@link ThrowingConsumer}, which also implements {@link Consumer}. This can be used to
     * allow checked exceptions inside {@link Consumer} lambda expressions. Any exceptions thrown in this block will
     * be re-thrown as unchecked using {@link #rethrow(Throwable)}.
     *
     * <p>This method simply returns the {@link ThrowingConsumer} directly, this method only acts as a shorthand for the
     * following code (notably not needing to specify generic types):
     *
     * <pre>
     *    {@literal someMethod(HypoModelUtil.wrapConsumer(t -> {}))}
     *    // Shorthand for:
     *    {@literal someMethod(ThrowingFunction<String, ?>) t -> {}}
     * </pre>
     *
     * @param consumer The consumer to return.
     * @param <T> The input type of the consumer.
     * @param <X> The exception the consumer may throw.
     * @return The given consumer as-is.
     */
    @Contract(value = "_ -> param1", pure = true)
    public static <T, X extends Throwable> ThrowingConsumer<T, X> wrapConsumer(
        final @NotNull ThrowingConsumer<T, X> consumer
    ) {
        return consumer;
    }

    /**
     * Simplifies the logic of handling suppressed exceptions when multiple exceptions may be chained. If the
     * {@code base} exception is {@code null}, {@code thrown} will be returned directly. Otherwise, {@code thrown} will
     * be passed to {@code base's} {@link Throwable#addSuppressed(Throwable)} method, and {@code base} will be returned.
     * The typical pattern this method is intended for is as follows:
     *
     * <pre>
     *     Throwable thrown = null;
     *     try {
     *         ...
     *     } catch (Exception e) {
     *         thrown = addSuppressed(thrown, e);
     *     }
     *     try {
     *         ...
     *     } catch (Exception e) {
     *         thrown = addSuppressed(thrown, e);
     *     }
     *     if (thrown != null) {
     *         // do something
     *     }
     * </pre>
     *
     * @param base The current base exception, can be {@code null}.
     * @param thrown The new thrown exception, must not be {@code null}.
     * @param <T1> The type of the base exception.
     * @param <T2> The type of the new exception, must be assignable to {@link T1}.
     * @return The exception to act as the new base exception.
     */
    @Contract("null, _ -> param2")
    public static <T1 extends Throwable, T2 extends T1> @NotNull T1 addSuppressed(
        final @Nullable T1 base,
        final @NotNull T2 thrown
    ) {
        if (base == null) {
            return thrown;
        }

        base.addSuppressed(thrown);
        return base;
    }

    /**
     * Version of {@link Function} which allows throwing checked exception inside the method implementation. When the
     * {@link Function#apply(Object)} method is invoked it will call {@link #applyThrowing(Object)}, rethrowing any
     * exceptions thrown as unchecked using {@link #rethrow(Throwable)}.
     *
     * @param <T> The input type of the function.
     * @param <R> The return type of the function.
     * @param <X> The exception the function may throw.
     */
    @FunctionalInterface
    public interface ThrowingFunction<T, R, X extends Throwable> extends Function<T, R> {

        /**
         * The same as {@link #apply(Object)}, but also allowing checked exceptions to be thrown.
         *
         * @param t The function argument
         * @return The function result
         * @throws X The type of the exception which this method throws
         */
        R applyThrowing(T t) throws X;

        @SuppressWarnings("FunctionalInterfaceMethodChanged")
        @Override
        default R apply(T t) {
            try {
                return this.applyThrowing(t);
            } catch (final Throwable x) {
                throw HypoModelUtil.rethrow(x);
            }
        }
    }

    /**
     * Version of {@link Consumer} which allows throwing checked exception inside the method implementation. When the
     * {@link Consumer#accept(Object)} method is invoked it will call {@link #acceptThrowing(Object)}, rethrowing any
     * exceptions thrown as unchecked using {@link #rethrow(Throwable)}.
     *
     * @param <T> The input type of the consumer.
     * @param <X> The exception the consumer may throw.
     */
    @FunctionalInterface
    public interface ThrowingConsumer<T, X extends Throwable> extends Consumer<T> {

        /**
         * The same as {@link #accept(Object)}, but also allowing checked exceptions to be thrown.
         *
         * @param t The input argument
         * @throws X The type of the exception which this method throws
         */
        void acceptThrowing(T t) throws X;

        @SuppressWarnings("FunctionalInterfaceMethodChanged")
        @Override
        default void accept(T t) {
            try {
                this.acceptThrowing(t);
            } catch (final Throwable x) {
                throw HypoModelUtil.rethrow(x);
            }
        }
    }
}
