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

package dev.denwav.hypo.types.pattern;

import dev.denwav.hypo.types.TypeRepresentable;
import dev.denwav.hypo.types.kind.MethodType;
import dev.denwav.hypo.types.kind.ValueType;
import java.io.Serializable;
import java.util.function.Predicate;
import org.jetbrains.annotations.NotNull;

/**
 * A type pattern matches a {@link TypeRepresentable JVM type definition}, including
 * {@link dev.denwav.hypo.types.desc.Descriptor Descriptors} and {@link dev.denwav.hypo.types.sig.Signature Signatures},
 * and both {@link ValueType field types} as well as
 * {@link MethodType method types}. It can also match
 * {@link dev.denwav.hypo.types.sig.ClassSignature ClassSignatures}.
 *
 * <p>For this API the term "match" means to return {@code true} from the
 * {@link #test(TypeMatchContext, TypeRepresentable) test method} if a given {@link TypeRepresentable} satisfies the
 * condition of that pattern. This is a similar concept to the {@link java.util.regex.Pattern Pattern} class, but for
 * Java types rather than Strings.
 *
 * <p>Like Pattern, TypePatterns can also define {@link TypeCapture captures} to extract components of a type pattern.
 * For example, a type pattern could be defined to match against a {@code List} type, and a capture could be defined on
 * the generic parameter to extract the type of the elements the List contains.
 *
 * <p>TypePatterns can be created directly by simply implementing
 * {@link #test(TypeMatchContext, TypeRepresentable) test}, but the intended method of using this interface is to use
 * the already defined TypePattern factories provided with this API:
 *
 * <table style="border: 1px solid;">
 *     <caption>
 *         Mapping between {@link TypeRepresentable} objects and their corresponding {@link TypePattern} factories.
 *     </caption>
 *     <tr>
 *         <th style="border: 1px solid;">Target Type</th>
 *         <th style="border: 1px solid;">Type Pattern Factories</th>
 *     </tr>
 *     <tr>
 *         <td style="border: 1px solid;">{@link dev.denwav.hypo.types.desc.TypeDescriptor TypeDescriptor}</td>
 *         <td style="border: 1px solid;" rowspan="2">{@link TypePatterns}</td>
 *     </tr>
 *     <tr>
 *         <td style="border: 1px solid;">{@link dev.denwav.hypo.types.sig.TypeSignature TypeSignature}</td>
 *     </tr>
 *     <tr>
 *         <td style="border: 1px solid;">{@link dev.denwav.hypo.types.desc.MethodDescriptor TypeDescriptor}</td>
 *         <td style="border: 1px solid;" rowspan="2">{@link MethodPatterns}</td>
 *     </tr>
 *     <tr>
 *         <td style="border: 1px solid;">{@link dev.denwav.hypo.types.sig.MethodSignature TypeSignature}</td>
 *     </tr>
 *     <tr>
 *         <td style="border: 1px solid;">{@link dev.denwav.hypo.types.sig.param.TypeParameter TypeParameter}</td>
 *         <td style="border: 1px solid;" rowspan="2">{@link TypeParameterPatterns}</td>
 *     </tr>
 *     <tr>
 *         <td style="border: 1px solid;">{@link dev.denwav.hypo.types.sig.ClassSignature ClassSignature}</td>
 *         <td style="border: 1px solid;" rowspan="2">{@link ClassSignaturePatterns}</td>
 *     </tr>
 * </table>
 *
 * @see TypeCapture
 * @see TypePatterns
 * @see MethodPatterns
 * @see TypeParameterPatterns
 * @see ClassSignaturePatterns
 */
public interface TypePattern extends Serializable {

    /**
     * Test if the given {@code type} matches this pattern. This method will return {@code false} when the given type
     * does not match, though that can also be due to the kind of {@code type} also being incompatible. For example,
     * if this pattern is testing the parameters of a method type and a field type is passed in, it will always return
     * {@code false}.
     *
     * <p>This method can be called directly, but it is primarily an internal API, due to the {@code ctx} parameter.
     * Users should typically prefer to use {@link #match(TypeRepresentable)} instead.
     *
     * @param ctx The {@link TypeMatchContext context}.
     * @param type The {@link TypeRepresentable type} to test.
     * @return {@code true} if the given type satisfies this pattern.
     */
    boolean test(final @NotNull TypeMatchContext ctx, final @NotNull TypeRepresentable type);

    /**
     * Test if the given {@code type} matches this pattern. The returned match object will contain both the match state
     * and any captured values if {@link TypeMatch#matches()} is {@code true}.
     *
     * @param type The {@link TypeRepresentable type} to test.
     * @return {@code true} if the given type satisfies this pattern.
     */
    default @NotNull TypeMatch match(final @NotNull TypeRepresentable type) {
        final TypeMatchContext ctx = new TypeMatchContext();
        final boolean matches = this.test(ctx, type);
        return new TypeMatch(matches, matches ? ctx.getCapturedTypes() : null);
    }

    /**
     * Transform this pattern into a {@link Predicate} for use in standard Java APIs.
     *
     * @return A new {@link Predicate} which wraps this pattern and checks for {@link TypeMatch#matches()}.
     */
    default @NotNull Predicate<TypeRepresentable> asPredicate() {
        return t -> this.match(t).matches();
    }

    /**
     * A named capture of a delegate pattern. Captured values are only held if the whole containing type pattern
     * matches, which includes the given delegate pattern. The {@code name} of the capture is used to store and retrieve
     * the type object which matches the given delegate pattern.
     *
     * <p>When a {@link TypeMatch} both {@link TypeMatch#matches() matches} and was generated from a {@link TypePattern}
     * containing a capture of name {@code N}, then {@link TypeMatch#get(String) TypeMatch.get(N)} is guaranteed to
     * return the value for the corresponding captured object <b>unless</b> the capture was inside an optional
     * component of the {@link TypePattern}, such as {@link TypePattern#or(TypePattern)}.
     *
     * <p>This API does not check for name conflicts of already defined captures, defining a type pattern with multiple
     * captures of the same name is undefined behavior.
     *
     * <p>Named and anonymous captures can be used together in the same type pattern.
     *
     * <p>Example:
     * <pre><code>
     *     // TypePattern which matches Lists and captures the generic type of the list
     *     final TypePattern pattern = TypePatterns.isClass("java/util/List")
     *         .and(TypePatterns.hasTypeArguments(TypePattern.capture("typeArg", TypePattern.any())));
     *     // Example type to tests
     *     final TypeSignature testType = TypeSignature.parse("Ljava/util/List&lt;Ljava/lang/String;&gt;;");
     *     // typeArg is "Ljava/lang/String;"
     *     final TypeSignature typeArg = (TypeSignature) pattern.match(testType).get("typeArg");
     * </code></pre>
     *
     * @param name The name of the capture, to be used with {@link TypeMatch#get(String)}
     * @param delegate The type pattern the captured type object must satisfy.
     * @return A new pattern which wraps the given delegate and captures the matching value.
     * @see #capture(TypePattern)
     */
    static TypePattern capture(final String name, final @NotNull TypePattern delegate) {
        return (ctx, type) -> {
            if (delegate.test(ctx, type)) {
                ctx.capture(name, type);
                return true;
            }
            return false;
        };
    }

    /**
     * An anonymous capture of a delegate pattern. Captured values are only held if the whole containing type pattern
     * matches, which includes the given delegate pattern. Anonymous captures are managed using the returned
     * {@link TypeCapture} object, which itself is also a {@link TypePattern}.
     *
     * <p>When a {@link TypeMatch} {@code M} both {@link TypeMatch#matches() matches} and was generated from a {@link TypePattern}
     * containing a capture {@code C}, then {@link TypeCapture#get(TypeMatch) C.get(M)} is guaranteed to return the
     * value for the corresponding captured object <b>unless</b> the capture was inside an optional
     * component of the {@link TypePattern}, such as {@link TypePattern#or(TypePattern)}.
     *
     * <p>Named and anonymous captures can be used together in the same type pattern.
     *
     * <p>Example:
     * <pre><code>
     *     // Define the TypeCapture as a separate reference so it can be used later
     *     final TypeCapture capture = TypePattern.capture(TypePattern.any());
     *     // TypePattern which matches Lists and captures the generic type of the list
     *     final TypePattern pattern = TypePatterns.isClass("java/util/List")
     *         .and(TypePatterns.hasTypeArguments(capture));
     *     // Example type to tests
     *     final TypeSignature testType = TypeSignature.parse("Ljava/util/List&lt;java/lang/String;&gt;;");
     *     // typeArg is "Ljava/lang/String;"
     *     final TypeSignature typeArg = (TypeSignature) capture.get(pattern.match(testType));
     * </code></pre>
     *
     <p>Depending on preference, you can also move the TypeCapture definition to the call site:
     * <pre><code>
     *     // Define the TypeCapture as a separate reference so it can be used later
     *     final TypeCapture capture;
     *     // TypePattern which matches Lists and captures the generic type of the list
     *     final TypePattern pattern = TypePatterns.isClass("java/util/List")
     *         // (ab)use the fact that in Java assignments are also expressions
     *         .and(TypePatterns.hasTypeArguments(capture = TypePattern.capture(TypePattern.any())));
     *     // Example type to tests
     *     final TypeSignature testType = TypeSignature.parse("Ljava/util/List&lt;Ljava/lang/String;&gt;;");
     *     // typeArg is "Ljava/lang/String;"
     *     final TypeSignature typeArg = (TypeSignature) capture.get(pattern.match(testType));
     * </code></pre>
     *
     * @param delegate The type pattern the captured type object must satisfy.
     * @return A new pattern which wraps the given delegate and captures the matching value.
     * @see #capture(String, TypePattern)
     */
    static TypeCapture capture(final @NotNull TypePattern delegate) {
        final String key = TypeCapture.randomKey();
        return new TypeCapture(key, capture(key, delegate));
    }

    /**
     * Create a new pattern which requires both {@code this} and the given {@code other} pattern to match the same
     * type object in order for the whole pattern to match.
     *
     * @param other The other pattern which must also match.
     * @return A new pattern that wraps both {@code this} and {@code other} and requires both to match.
     */
    default @NotNull TypePattern and(final @NotNull TypePattern other) {
        return (ctx, t) -> this.test(ctx, t) && other.test(ctx, t);
    }

    /**
     * Create a new pattern which requires only one of {@code this} and the given {@code other} pattern to match a type
     * object in order for the whole pattern to match.
     *
     * <p>Note that any {@link #capture(TypePattern) captures} defined in either of these patterns are no longer
     * guaranteed to be present in the returned {@link TypeMatch} object if the other side of the {@code OR} matched and
     * the side of the pattern the capture was defined in did not match.
     *
     * <p>Also note that the pattern is executed lazily by first checking {@code this}, then {@code other}. If both
     * {@code this} and {@code other} match the given type, but the capture is only defined in {@code other}, then that
     * capture will also not be present, as {@code other} never executed after {@code this} matched.
     *
     * @param other The other pattern which may also match.
     * @return A new pattern that wraps both {@code this} and {@code other} and requires only one to match.
     */
    default @NotNull TypePattern or(final @NotNull TypePattern other) {
        return (ctx, t) -> this.test(ctx, t) || other.test(ctx, t);
    }

    /**
     * Invert {@code this} pattern and return a new pattern which only matches when {@code this} does <b>not</b> match.
     *
     * @return A new pattern that negates {@code this} pattern.
     */
    default @NotNull TypePattern not() {
        return (ctx, t) -> !this.test(ctx, t);
    }

    /**
     * A basic pattern which wraps a given predicate.
     *
     * @param predicate The predicate to test against the type object.
     * @return A type pattern wrapping the given predicate.
     */
    static @NotNull TypePattern is(final @NotNull Predicate<? super TypeRepresentable> predicate) {
        return (ctx, t) -> predicate.test(t);
    }

    /**
     * A pattern which always matches.
     * @return A pattern which always matches.
     */
    static @NotNull TypePattern any() {
        return (ctx, t) -> true;
    }

    /**
     * A pattern which never matches.
     * @return A pattern which never matches.
     */
    static @NotNull TypePattern none() {
        return (ctx, t) -> false;
    }
}
