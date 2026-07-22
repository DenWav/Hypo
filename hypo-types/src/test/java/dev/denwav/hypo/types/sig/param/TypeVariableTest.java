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

package dev.denwav.hypo.types.sig.param;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import dev.denwav.hypo.types.TypeVariableBinder;
import java.util.function.Supplier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import dev.denwav.hypo.types.sig.ClassTypeSignature;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("[types] TypeVariable Tests")
class TypeVariableTest {

    @Test
    @DisplayName("Verify that TypeVariable.ofLazy does not evaluate the supplier during construction, interning, or name queries")
    void testLazySupplierNotEvaluatedPrematurely() {
        final AtomicBoolean evaluated = new AtomicBoolean(false);
        final Supplier<TypeParameter> supplier = () -> {
            evaluated.set(true);
            return TypeParameter.of("T");
        };

        final TypeVariable typeVar = TypeVariable.ofLazy("T", supplier);

        assertFalse(evaluated.get(), "Supplier should not be evaluated during ofLazy construction and interning");
        assertEquals("T", typeVar.getName());
        assertEquals("T", typeVar.asReadable());
        assertEquals("TT;", typeVar.asInternal());
        assertEquals("TT;", typeVar.internKey());
        assertFalse(evaluated.get(), "Supplier should not be evaluated after calling getName/asReadable/asInternal(false)/internKey");

        final TypeParameter def = typeVar.getDefinition();
        assertTrue(evaluated.get(), "Supplier should be evaluated upon calling getDefinition()");
        assertNotNull(def);
        assertEquals("T", def.getName());
    }

    @Test
    @DisplayName("Verify Unbound.bind defers resolution until getDefinition is called")
    void testUnboundBindIsLazyAndSupportsRecursion() {
        final TypeVariable.Unbound unboundVar = TypeVariable.unbound("E");
        final AtomicBoolean binderCalled = new AtomicBoolean(false);

        final TypeVariableBinder lazyBinder = name -> {
            binderCalled.set(true);
            return TypeParameter.of(name);
        };

        final TypeVariable boundVar = unboundVar.bind(lazyBinder);
        assertFalse(binderCalled.get(), "Unbound.bind should defer binder lookup until getDefinition is called");
        assertFalse(boundVar.isUnbound());
        assertEquals("E", boundVar.getName());

        final TypeParameter def = boundVar.getDefinition();
        assertTrue(binderCalled.get(), "binder.bindingFor should be called inside getDefinition()");
        assertEquals("E", def.getName());
    }

    @Test
    @DisplayName("Verify thread-safe double-checked locking of lazy TypeVariable resolution under contention")
    void testLazyResolutionThreadSafety() throws InterruptedException {
        final int numThreads = 16;
        final AtomicInteger evaluationCount = new AtomicInteger(0);
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch doneLatch = new CountDownLatch(numThreads);

        final TypeVariable typeVar = TypeVariable.ofLazy("T", () -> {
            evaluationCount.incrementAndGet();
            try {
                Thread.sleep(10);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return TypeParameter.of("T");
        });

        for (int i = 0; i < numThreads; i++) {
            new Thread(() -> {
                try {
                    startLatch.await();
                    final TypeParameter def = typeVar.getDefinition();
                    assertNotNull(def);
                    assertEquals("T", def.getName());
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            }).start();
        }

        startLatch.countDown();
        doneLatch.await();
        assertEquals(1, evaluationCount.get(), "Supplier should be evaluated exactly once across concurrent threads");
    }

    @Test
    @DisplayName("Verify asInternal(sb, true) and internKey() return {BIND-...} formats without infinite recursion once resolved")
    void testAsInternalAndInternKeyWithBindKeyAndRecursion() {
        final TypeVariable typeVar = TypeVariable.ofLazy("T", () -> TypeParameter.of("T"));
        assertEquals("TT;", typeVar.internKey());

        // Resolve definition
        final TypeParameter def = typeVar.getDefinition();
        assertNotNull(def);
        assertEquals("{BIND:T:Ljava/lang/Object;}TT;", typeVar.internKey());

        final StringBuilder sb = new StringBuilder();
        typeVar.asInternal(sb, true);
        assertEquals("{BIND:T:Ljava/lang/Object;}TT;", sb.toString());
    }

    @Test
    @DisplayName("Verify container signature with unresolved ofLazy TypeVariable defers interning until resolution")
    void testContainerSignatureInterningDeferredUntilResolved() {
        final Supplier<TypeParameter> supplier = () -> TypeParameter.of("T");
        final TypeVariable lazyVar1 = TypeVariable.ofLazy("T", supplier);
        final TypeVariable lazyVar2 = TypeVariable.ofLazy("T", supplier);

        final ClassTypeSignature sig1 = ClassTypeSignature.of(null, "java/util/List", List.of(lazyVar1));
        final ClassTypeSignature sig2 = ClassTypeSignature.of(null, "java/util/List", List.of(lazyVar2));

        assertNotSame(sig1, sig2);

        lazyVar1.getDefinition();
        lazyVar2.getDefinition();

        assertSame(sig1.intern(), sig2.intern());
    }

    @Test
    @DisplayName("Verify TypeVariable.unbound returns interned instances")
    void testUnboundInterning() {
        assertSame(TypeVariable.unbound("X"), TypeVariable.unbound("X"));
    }
}
