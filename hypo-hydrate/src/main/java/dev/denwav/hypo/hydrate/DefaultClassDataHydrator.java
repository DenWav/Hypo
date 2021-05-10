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

package dev.denwav.hypo.hydrate;

import dev.denwav.hypo.core.HypoContext;
import dev.denwav.hypo.model.HypoModelUtil;
import dev.denwav.hypo.model.data.ClassData;
import dev.denwav.hypo.model.data.MethodData;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jgrapht.Graph;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListenerAdapter;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.GraphIterator;
import org.jgrapht.traverse.TopologicalOrderIterator;

/**
 * Default implementation of {@link ClassDataHydrator}.
 */
public class DefaultClassDataHydrator implements ClassDataHydrator {

    @Override
    public void hydrate(final @NotNull HypoContext context) throws IOException {
        final ExecutorService executor = context.getExecutor();

        // Build the class inheritance graph
        final Graph<ClassData, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);

        for (final ClassData classData : context.getProvider().allClasses()) {
            g.addVertex(classData);
            addEdge(classData.superClass(), classData, g);
            // For simplicity we'll re-use this same graph for outer classes too
            // This graph impl will ignore duplicate edges
            // Below when we hydrate the class data we need to remember to check which is which
            addEdge(classData.outerClass(), classData, g);
            for (final ClassData interData : classData.interfaces()) {
                addEdge(interData, classData, g);
            }
        }

        // Walk graph to build out downward relationships
        final GraphIterator<ClassData, DefaultEdge> iter = new TopologicalOrderIterator<>(g);
        iter.addTraversalListener(new TraversalListenerAdapter<ClassData, DefaultEdge>() {
            @Override
            public void edgeTraversed(EdgeTraversalEvent<DefaultEdge> e) {
                final ClassData source = g.getEdgeSource(e.getEdge());
                final ClassData target = g.getEdgeTarget(e.getEdge());

                try {
                    if (Objects.equals(target.outerClass(), source)) {
                        source.innerClasses().add(target);
                    } else {
                        source.childClasses().add(target);
                    }
                } catch (final IOException ex) {
                    HypoModelUtil.rethrow(ex);
                }
            }
        });
        iter.setReuseEvents(true);

        final ArrayList<Future<?>> futures = new ArrayList<>();
        while (iter.hasNext()) {
            final ClassData nextClass = iter.next();
            futures.add(executor.submit((Callable<Void>) () -> {
                fillMethods(nextClass);
                return null;
            }));
        }

        for (final Future<?> future : futures) {
            try {
                future.get();
            } catch (final InterruptedException | ExecutionException e) {
                HypoModelUtil.rethrow(e);
            }
        }
    }

    private static <T> void addEdge(final @Nullable T source, final @NotNull T target, final @NotNull Graph<T, ?> g) {
        if (source == null) {
            return;
        }
        g.addVertex(source);
        g.addEdge(source, target);
    }

    private static void fillMethods(final @NotNull ClassData classData) throws IOException {
        for (final MethodData method : classData.methods()) {
            if (!method.isConstructor()) {
                classData.forEachSuperClass(p -> checkMethods(method, p));
            }
        }
    }

    private static void checkMethods(
        final @NotNull MethodData baseMethod,
        final @Nullable ClassData parentClass
    ) throws IOException {
        if (parentClass == null) {
            return;
        }
        parentClass.forEachSuperClass(p -> checkMethods(baseMethod, p));

        for (final MethodData method : parentClass.methods()) {
            if (method.isConstructor()) {
                continue;
            }

            // sanity check
            if (baseMethod.equals(method)) {
                continue;
            }

            if (baseMethod.overrides(method)) {
                baseMethod.setSuperMethod(method);

                final Set<MethodData> chi = method.childMethods();
                //noinspection SynchronizationOnLocalVariableOrMethodParameter
                synchronized (chi) {
                    chi.add(baseMethod);
                }
            }
        }
    }
}
