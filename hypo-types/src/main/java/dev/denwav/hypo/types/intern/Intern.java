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

package dev.denwav.hypo.types.intern;

import dev.denwav.hypo.types.HypoTypesUtil;
import dev.denwav.hypo.types.TypeRepresentable;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interning base class which enables types classes to be interned, that is to say, prevent multiple copies of equal
 * values to exist on the heap. This is useful for two primary reasons: first, all type classes are pure data classes,
 * their identity is meaningless. Second, and more importantly, when analyzing large jar files with many classes many of
 * the same type descriptors will be encountered over and over again, and it would be an inefficient use of memory to
 * store separate copies for each type when they are identical. Common examples include core Java types just as
 * {@code Ljava/lang/Object;} and {@code Ljava/lang/String;}, but this also applies to extremely common method
 * descriptors and signatures, such as {@code ()V}, etc.
 *
 * <p>This is thread-safe, and utilizes a concurrent hashmap to enable lock-less handling of value instances.
 *
 * <p>The internal state of the internment can be checked via two static helper methods on this class,
 * {@link #tryFind(Class, String)} and {@link #internmentSize(Class)}.
 *
 * @param <T> The type of {@code this}. Any other type will result in a {@link ClassCastException} at runtime.
 */
public abstract class Intern<T extends Intern<T>> implements InternKey {

    private static final IdentityHashMap<ConcurrentHashMap<String, WeakReference<?>>, AtomicLong> interns = new IdentityHashMap<>();
    private static final List<ConcurrentHashMap<String, WeakReference<?>>> newInterns = new ArrayList<>();
    static {
        final Thread t = new Thread(() -> {
            while (true) {
                try {
                    //noinspection BusyWait
                    Thread.sleep(500);
                } catch (final InterruptedException e) {
                    break;
                }
                for (final var entry : interns.entrySet()) {
                    final var map = entry.getKey();
                    final AtomicLong lastSize = entry.getValue();
                    if (Math.abs(map.mappingCount() - lastSize.get()) < 10_000) {
                        continue;
                    }
                    map.values().removeIf(r -> r.get() == null);
                    lastSize.set(map.mappingCount());
                }

                // Prevent CME
                if (!newInterns.isEmpty()) {
                    synchronized (newInterns) {
                        for (final var newIntern : newInterns) {
                            interns.put(newIntern, new AtomicLong(0));
                        }
                        newInterns.clear();
                    }
                }
            }
        }, "Interning Cleanup");
        t.setDaemon(true);
        t.start();
    }

    private static final class InternClassValue extends ClassValue<ConcurrentHashMap<String, WeakReference<?>>> {
        @Override
        protected ConcurrentHashMap<String, WeakReference<?>> computeValue(final @NotNull Class<?> type) {
            final ConcurrentHashMap<String, WeakReference<?>> map = new ConcurrentHashMap<>();
            synchronized (newInterns) {
                newInterns.add(map);
            }
            return map;
        }
    }

    private static final InternClassValue internment = new InternClassValue();

    private static final boolean interningDisabled = Boolean.getBoolean("hypo.interning.disabled");

    /**
     * {@code protected} constructor as this class must only be used by extending.
     */
    protected Intern() {
    }

    /**
     * Return a class which is guaranteed to have the same value as {@code this}, but may be a different instance. An
     * interned value satisfies the following rule:
     * <ul>
     *     <li>{@code obj.intern().equals(obj)}</li>
     *     <li>{@code obj.equals(obj.intern())}</li>
     * </ul>
     *
     * If two objects exist, {@code o1} and {@code o2}, where {@code o1.equals(o2)}, then the following will also be true:
     * <ul>
     *     <li>{@code o1.intern() == o2.intern()}</li>
     * </ul>
     *
     * @return Return the interned version of this object.
     */
    public final T intern() {
        if (interningDisabled) {
            return HypoTypesUtil.cast(this);
        }

        final T t = HypoTypesUtil.cast(this);
        try {
            final ConcurrentHashMap<String, WeakReference<?>> map = internment.get(this.getClass());
            final String key = t.internKey();
            final WeakReference<T> ref = new WeakReference<>(t);
            final T res = HypoTypesUtil.cast(map.computeIfAbsent(key, k -> ref).get());
            if (res != null) {
                return res;
            }

            // Edge case, handle scenarios where the reference returned from `map` was garbage collected but was still
            // present in the map. When that occurs, we replace the entry with our new valid entry and return it,
            // guaranteeing this method will never return `null`.
            //
            // Note that this will not result in "leaks" of this value (even though such leaks would be harmless) as
            // the above case can only happen when the stored value was already considered weakly reachable. When that
            // happens, it was already being GCed and no longer exists on the heap.
            map.put(key, ref);
            return t;
        } finally {
            Reference.reachabilityFence(t);
        }
    }

    /**
     * Attempt to find the given object of the type {@code c} in the internment map by its string value, {@code key}.
     * The key for interned objects is always {@link TypeRepresentable#asInternal()}. Returns {@code null} if the given
     * key does not exist in the map.
     *
     * @param c The {@link Class} object of the type to check.
     * @param key The {@link TypeRepresentable#asInternal()} key of the value to check.
     * @return The currently interned value for the given key, if it exists, otherwise {@code null}.
     * @param <T> The type of the interned value.
     */
    public static <T> @Nullable T tryFind(final @NotNull Class<T> c, final @NotNull String key) {
        final WeakReference<?> ref = internment.get(c).get(key);
        if (ref == null) {
            return null;
        }
        final Object r = ref.get();
        try {
            if (r != null) {
                return HypoTypesUtil.cast(r);
            }
            return null;
        } finally {
            Reference.reachabilityFence(r);
        }
    }

    /**
     * Get the current estimated size of the internment map for the given class type.
     *
     * @param c The class to check.
     * @return The current estimated size of the internment map.
     */
    public static long internmentSize(final @NotNull Class<?> c) {
        return internment.get(c).mappingCount();
    }
}
