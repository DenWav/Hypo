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

package dev.denwav.hypo.types;

import dev.denwav.hypo.types.intern.Intern;
import dev.denwav.hypo.types.sig.MethodSignature;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MethodSignatureThreadContentionTest {

    @Test
    void testThreadContention() throws ExecutionException, InterruptedException {
        final AtomicLong counter = new AtomicLong(0);

        final ArrayList<Future<?>> tasks = new ArrayList<>();
        try (final ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 64; i++) {
                tasks.add(pool.submit(() -> {
                    for (int j = 0; j < 1_000_000; j++) {
                        final MethodSignature sig = MethodSignature.parse("()V");
                        Assertions.assertEquals(sig, MethodSignature.parse(sig.asInternal()), "Internal " + sig.asInternal());

                        final long count = counter.incrementAndGet();
                        if (count % 1_000_000 == 0) {
                            System.out.printf("Tested %,d iterations...%n", count);
                            System.out.printf("MethodSignature internment: %,d %n", Intern.internmentSize(MethodSignature.class));
                        }
                    }
                }));
            }
        }

        for (final Future<?> task : tasks) {
            task.get();
        }
    }
}
