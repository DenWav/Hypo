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
 * <h2><a href="https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.7.9.1">Type Signatures</a></h2>
 *
 * <p>Type signatures are the types used by the Java compiler. Unlike type descriptors, they can include generic type
 * information, including type variables. This packages breaks type signatures up into three categories, types, methods,
 * and class signatures.
 *
 * <ul>
 *     <li>{@link dev.denwav.hypo.types.sig.TypeSignature TypeSignature}</li>
 *     <li>{@link dev.denwav.hypo.types.sig.MethodSignature MethodSignature}</li>
 *     <li>{@link dev.denwav.hypo.types.sig.ClassSignature ClassSignature}</li>
 * </ul>
 *
 * <h3>Type Parameters, Variables, and Arguments</h3>
 * <p>Often confused or used interchangeably, the terms "type parameter", "type variable", and "type argument" refer to
 * three distinct concepts in the Java type system. It's important to understand each one if you want to be able to
 * effectively use the APIs in this package.
 *
 * <h4>{@link dev.denwav.hypo.types.sig.param.TypeParameter Type Parameter}</h4>
 * <p>A type <b>parameter</b> is attached to either a method or class signature (note, as in a
 * {@link dev.denwav.hypo.types.sig.ClassSignature class signature}, not a
 * {@link dev.denwav.hypo.types.sig.ClassTypeSignature class type signature}), and represents types which are generic at
 * the declaration of the type parameter holder. Type parameters can be upper or lower bounds defined to limit allowed
 * types. Examples:
 *
 * <pre><code>
 *     class SomeClass&lt;T extends Number&gt; // Class Signature
 *     &lt;T extends Number&gt; void someMethod() // Method Signature
*
 *     &lt;T&gt; void unboundedMethod() // Method Signature
 * </code></pre>
 *
 * <p>In the above examples, both {@code SomeClass} and {@code someMethod} have a defined type parameter, named
 * {@code T}. The bounds of {@code T} is that whatever type {@code T} represents must extend {@code Number}.
 * <p>{@code unboundedMethod} shows an example of a type parameter without a defined bound. In practice, this means the
 * actual bounds of {@code T} is {@code extends Object}.
 *
 * <h4>{@link dev.denwav.hypo.types.sig.param.TypeArgument Type Argument}</h4>
 * <p>A type <b>argument</b> is the call-site equivalent of a type parameter. When a class is extended which requires
 * parameters, or a method is called which requires type parameters, the call-site types specified in the {@code <>}
 * brackets are the type arguments. A type argument always corresponds to some defined type parameter.
 *
 * <pre><code>
 *     class SomeClass&lt;T extends Number&gt; // Class Signature
 *                     ^ type parameter
 *     class ExtendingClass extends SomeClass&lt;Integer&gt; // Class Signature
 *                                            ^ type argument
 * </code></pre>
 *
 * <p>In the above example {@code SomeClass} contains a type parameter {@code T}. {@code ExtendingClass} then extends
 * {@code SomeClass}, and provides an argument for {@code T}, which is {@code Integer} in this instance.
 *
 * <pre><code>
 *     &lt;T extends Number&gt; void someMethod() // Method Signature
 *      ^ type parameter
 *     this.&lt;Integer&gt;someMethod()
 *           ^ type argument
 * </code></pre>
 *
 * <h4>{@link dev.denwav.hypo.types.sig.param.TypeVariable Type Variable}</h4>
 * <p>A type <b>variable</b> is the <u>usage</u> of a type parameter. Type variables can be used as the type for method
 * signatures or field types. They can also be used as the argument for a type parameter.
 *
 * <pre><code>
 *     class SomeClass&lt;T extends Number&gt; // Class Signature
 *                     ^ type parameter
 *     class ExtendingClass&lt;R extends Number&gt; extends SomeClass&lt;R&gt; // Class Signature
 *                          ^ type parameter                    ^ type argument
 *                                                              ^ type variable
 * </code></pre>
 *
 * @see dev.denwav.hypo.types.desc Type Descriptors
 */
package dev.denwav.hypo.types.sig;
