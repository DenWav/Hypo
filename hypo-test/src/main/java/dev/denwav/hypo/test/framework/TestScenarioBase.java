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

package dev.denwav.hypo.test.framework;

import com.google.errorprone.annotations.concurrent.LazyInit;
import dev.denwav.hypo.asm.AsmClassDataProvider;
import dev.denwav.hypo.core.HypoConfig;
import dev.denwav.hypo.core.HypoContext;
import dev.denwav.hypo.hydrate.HydrationManager;
import dev.denwav.hypo.hydrate.HydrationProvider;
import dev.denwav.hypo.mappings.ChangeChain;
import dev.denwav.hypo.mappings.MappingsCompletionManager;
import dev.denwav.hypo.mappings.contributors.ChangeContributor;
import dev.denwav.hypo.model.ClassProviderRoot;
import dev.denwav.hypo.model.HypoModelUtil;
import dev.denwav.hypo.model.data.ClassData;
import dev.denwav.hypo.model.data.MethodData;
import dev.denwav.hypo.types.desc.MethodDescriptor;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import net.fabricmc.lorenztiny.TinyMappingFormat;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;
import org.cadixdev.lorenz.MappingSet;
import org.cadixdev.lorenz.io.MappingFormats;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Base test harness for {@code hypo-model}, {@code hypo-core}, {@code hypo-asm}, {@code hypo-hydrate},
 * {@code hypo-asm-hydrate}, and {@code hypo-mappings} integration tests.
 *
 * <p>This harness works in the following way:
 * <ul>
 *     <li>
 *         Tests are broken up into scenarios and test cases. Scenarios have the following properties:
 *         <ul>
 *             <li>A {@link HypoContext} defining a set of classes to cover</li>
 *             <li>The context is filled in via a jar available at test time through a system property</li>
 *             <li>
 *                 A set of renames may be available, which will be run over the classes before they are sent to the
 *                 context
 *                 <ul>
 *                     <li>This allows scenarios to cover cases which aren't possible out of javac by itself</li>
 *                     <li>Common cases of code produced by proguard can be tested this way</li>
 *                 </ul>
*              </li>
 *             <li>A list of hydrators to run on the context once it's created</li>
 *         </ul>
 *     </li>
 *     <li>
 *         Each scenario may have multiple test cases, with the following properties:
 *         <ul>
 *             <li>The starting set of mappings</li>
 *             <li>A list of mappings change contributors to run against the mappings</li>
 *             <li>
 *                 Either one or the other of:
 *                 <ul>
 *                     <li>The expected ending set of mappings which the system should product</li>
 *                 </ul>
 *             </li>
 *             <li>The expected ending set of mappings which the system should product</li>
 *             <li>A list of mappings to compare at each step of the change contributor process</li>
 *         </ul>
 *     </li>
 * </ul>
 */
@SuppressWarnings("resource")
public abstract class TestScenarioBase {

    @LazyInit private @Nullable HypoContext context;

    /**
     * The {@link Env environment} for this scenario.
     * @return The {@link Env environment} for this scenario.
     */
    public abstract @NotNull Env env();

    /**
     * Retrieve {@link #context} as {@code non-null}. This should be set during test setup, so in practice it should
     * never be {@code null}.
     *
     * @return The current context for the test execution, {@code non-null}.
     */
    public @NotNull HypoContext context() {
        final HypoContext c = this.context;
        if (c != null) {
            return c;
        }
        throw new NullPointerException("Context is not yet set up");
    }

    /**
     * Sets up the test execution context based on the {@link #env() environment} and hydrates it.
     * @throws Exception If an error occurs.
     */
    @BeforeEach
    public void setup() throws Exception {
        Configurator.setRootLevel(Level.DEBUG);
        this.context = this.createContext(this.env().forContext());
        this.hydrate();
    }

    /**
     * Closes the test execution context.
     * @throws Exception If an error occurs.
     */
    @AfterEach
    public void teardown() throws Exception {
        this.context().close();
    }

    public @NotNull ClassData findClass(final String name) {
        try {
            final ClassData classData = this.context().getContextProvider().findClass(name);
            assertNotNull(classData);
            return classData;
        } catch (final IOException e) {
            throw HypoModelUtil.rethrow(e);
        }
    }

    public static @NotNull MethodData findMethod(final ClassData data, final String name) {
        final List<@NotNull MethodData> methods = data.methods(name);
        assertEquals(1, methods.size());
        return methods.getFirst();
    }

    public static @NotNull MethodData findMethod(final ClassData data, final String name, final String desc) {
        final MethodData method = data.method(name, MethodDescriptor.parse(desc));
        assertNotNull(method);
        return method;
    }

    /**
     * Run the given {@link TestCase} on the current test execution context.
     *
     * @param test The {@link TestCase} to run.
     * @throws Exception If an error occurs.
     */
    public void runTest(final @NotNull TestScenarioBase.TestCase test) throws Exception {
        final MappingSet mappings = test.startMappings();

        final var manager = MappingsCompletionManager.create(this.context());

        final ChangeChain chain = ChangeChain.create();
        for (final var contributor : test.changeContributors()) {
            chain.addLink(contributor);
        }
        final MappingSet[] stages = test.stageMappings();
        if (stages != null) {
            chain.addMappingSetListener((i, map) -> {
                try {
                    equals(stages[i], map);
                } catch (final Exception e) {
                    HypoModelUtil.rethrow(e);
                }
            });
        }

        final MappingSet newMappings = chain.applyChain(mappings, manager);

        final MappingSet expectedMappings = test.finishMappings();

        equals(expectedMappings, newMappings);
    }

    /**
     * Parse the given string as {@code TSRG} mappings. Any 4 consecutive spaces will be considered {@code TAB}
     * characters, so it's okay to use a text block with space indents.
     *
     * <p>{@code TSRG} doesn't support method parameter mappings, use {@link #parseTiny(String)} instead if you need
     * parameter mappings.
     *
     * @param text The text of the {@code TSRG} mappings.
     * @return The {@link MappingSet} parsed from the text.
     * @see #parseTiny(String)
     */
    public static @NotNull MappingSet parseTsrg(final @NotNull String text) {
        final var fixed = text.replace("    ", "\t");
        final var input = new ByteArrayInputStream(fixed.getBytes(StandardCharsets.UTF_8));
        try (final var reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
            return MappingFormats.TSRG.createReader(reader).read();
        } catch (final IOException e) {
            throw HypoModelUtil.rethrow(e);
        }
    }

    /**
     * Parse the given string as {@code Tiny} mappings. Any 4 consecutive spaces will be considered {@code TAB}
     * characters, so it's okay to use a text block with space indents.
     *
     * <p> This method will insert the header line for you, do not include a header line.
     *
     * <p>{@code Tiny} is useful for method parameter mappings, but {@code TSRG} can be more readable if they aren't
     * needed. You can use {@link #parseTsrg(String)} instead if parameter mappings aren't needed.
     *
     * @param text The text of the {@code Tiny} mappings.
     * @return The {@link MappingSet} parsed from the text.
     * @see #parseTsrg(String)
     */
    public static @NotNull MappingSet parseTiny(final @NotNull String text) {
        final var header = "tiny\t2\t0\tfrom\tto\n";

        final var commentsRemoved = text.replaceAll("\\s+#.*", "");
        final var fixed = commentsRemoved.replace("    ", "\t");

        Path tempFile = null;
        try {
            tempFile = Files.createTempFile("hypo", "tiny");
            Files.writeString(tempFile, header + fixed);

            return TinyMappingFormat.STANDARD.read(tempFile, "from", "to");
        } catch (final IOException e) {
            throw HypoModelUtil.rethrow(e);
        } finally {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (final IOException e) {
                    HypoModelUtil.rethrow(e);
                }
            }
        }
    }

    private @NotNull HypoContext createContext(final @NotNull String testCase) throws Exception {
        final String testData = System.getProperty(testCase);
        assertNotNull(testData);

        ClassProviderRoot root = ClassProviderRoot.fromJar(Paths.get(testData));
        final var renames = this.env().renames();
        if (renames != null) {
            root = new BytecodeRenamer(renames, root);
        }

        final HypoContext.Builder builder = HypoContext.builder()
            .withProvider(AsmClassDataProvider.of(root));

        if (this.env().includeJdk()) {
            builder.withContextProvider(AsmClassDataProvider.of(ClassProviderRoot.ofJdk()));
        }
        builder.withConfig(this.env().config().setRequireFullClasspath(false).build());
        return builder.build();
    }

    private void hydrate() throws Exception {
        final HydrationManager manager = HydrationManager.createDefault();
        for (final HydrationProvider<?> provider : this.env().hydration()) {
            manager.register(provider);
        }
        manager.hydrate(this.context());
    }

    private static void equals(final @NotNull MappingSet expected, final @NotNull MappingSet actual) throws Exception {
        final var expectedBytes = new ByteArrayOutputStream();
        final var actualBytes = new ByteArrayOutputStream();

        try (
            final var expectedWriter = new OutputStreamWriter(expectedBytes, StandardCharsets.UTF_8);
            final var actualWriter = new OutputStreamWriter(actualBytes, StandardCharsets.UTF_8);
            final var expectedMappingsWriter = TinyMappingFormat.STANDARD.createWriter(expectedWriter, "from", "to");
            final var actualMappingsWriter = TinyMappingFormat.STANDARD.createWriter(actualWriter, "from", "to")
        ) {
            expectedMappingsWriter.write(expected);
            actualMappingsWriter.write(actual);
        }

        Assertions.assertEquals(
            expectedBytes.toString(StandardCharsets.UTF_8),
            actualBytes.toString(StandardCharsets.UTF_8)
        );
    }

    /**
     * Settings for the test execution environment of this scenario.
     *
     * <p>{@link #forContext()} is required, but the rest only need to be overridden in order to change the default
     * values.
     */
    @FunctionalInterface
    public interface Env {
        /**
         * The name of the system property the test scenario should use to find the jar file for this scenario's
         * context. This should be the same name as the source set.
         *
         * @return The system property which refers to the jar file for this scenario's context.
         */
        @NotNull String forContext();

        /**
         * The {@link HypoConfig} to use when building the {@link HypoContext}.
         *
         * <p>Defualts to {@code HypoConfig.builder().build()}.
         *
         * @return The {@link HypoConfig} to use when building the {@link HypoContext}.
         */
        default @NotNull HypoConfig.Builder config() {
            return HypoConfig.builder();
        }

        /**
         * The list of hydration providers to run on the context after it is created.
         *
         * <p>Defaults to no providers.
         *
         * @return The list of hydration providers to run on the context after it is created.
         */
        default @NotNull Iterable<HydrationProvider<?>> hydration() {
            return List.of();
        }

        /**
         * A primitive set of method renames to run on the classes before they are sent to the context.
         *
         * <p>This is for simulating specific scenarios proguard can produce which isn't possible to recreate directly
         * using Java source code with javac.
         *
         * <p>The map format is:
         * <pre>
         *    {@literal Map<ClassName, Map<[MethodName][MethodDescriptor], NewName>>}
         * </pre>
         *
         * <p>The {@code []} around {@code MethodName} and {@code MethodDescriptor} are for separation clarity only,
         * they shouldn't actually be present in the string.
         *
         * @return The map of method renames to run on the classes before they are sent to the context.
         */
        default @Nullable Map<String, Map<String, String>> renames() {
            return null;
        }

        /**
         * Whether the JDK should be included as a context provider in the {@link HypoContext}. By default, this is
         * {@code false} as it's usually not needed. Including the JDK in the context may slow down the hydration process.
         *
         * @return {@code true} if the JDK should be included as a context provider.
         */
        default boolean includeJdk() {
            return false;
        }
    }

    /**
     * An individual test case to run in the current test scenario environment.
     */
    public interface TestCase {

        /**
         * The change contributors to run on the given mappings.
         *
         * @return The change contributors to run on the given mappings.
         */
        @NotNull Iterable<ChangeContributor> changeContributors();

        /**
         * The initial set of mappings to give to the change contributors.
         * @return The initial set of mappings to give to the change contributors.
         */
        @NotNull MappingSet startMappings();

        /**
         * The final set of mappings expected to be the result of running the given change contributors against the
         * starting mappings in the current test scenario environment.
         *
         * <p>This only tests the final output set of mappings, use {@link #stageMappings()} to test the result at every
         * stage of the change contributor process.
         *
         * @return The final set of mappings expected to be the result of running the given change contributors again
         *         the starting mappings in the current test scenario environment.
         * @see #stageMappings()
         */
        default @NotNull MappingSet finishMappings() {
            final MappingSet[] stages = this.stageMappings();
            if (stages == null) {
                throw new IllegalStateException("No final mappings set");
            }
            return stages[stages.length - 1];
        }

        /**
         * An array of mappings expected to be the result of each step of the change contributors process. The array
         * returned here must have the same length as the number of contributors returned in
         * {@link #changeContributors()}.
         *
         * <p>If only the final set of mappings needs to be tested, {@link #finishMappings()} can be used instead. If
         * this method is implemented {@link #finishMappings()} does not also need to be implemented.
         *
         * @return An array of mappings expected to be the result of each step of the change contributors process.
         * @see #finishMappings()
         */
        @SuppressWarnings("MultipleNullnessAnnotations")
        default @NotNull MappingSet @Nullable [] stageMappings() {
            return null;
        }
    }
}
