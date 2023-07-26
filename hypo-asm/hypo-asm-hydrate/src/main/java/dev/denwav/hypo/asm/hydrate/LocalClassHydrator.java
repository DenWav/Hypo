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

package dev.denwav.hypo.asm.hydrate;

import dev.denwav.hypo.asm.AsmClassData;
import dev.denwav.hypo.asm.AsmMethodData;
import dev.denwav.hypo.core.HypoContext;
import dev.denwav.hypo.hydrate.HydrationProvider;
import dev.denwav.hypo.hydrate.generic.HypoHydration;
import dev.denwav.hypo.hydrate.generic.MethodClosure;
import dev.denwav.hypo.model.data.ClassData;
import dev.denwav.hypo.model.data.MethodData;
import dev.denwav.hypo.model.data.types.JvmType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import static dev.denwav.hypo.asm.HypoAsmUtil.toJvmType;
import static org.objectweb.asm.Type.getType;

/**
 * This is a {@link HydrationProvider} for determining local and anonymous classes present in methods, and the
 * local variables which they capture. It sets {@link HypoHydration#LOCAL_CLASSES} on both the methods which contain
 * the local or anonymous classes, and the classes themselves.
 */
public final class LocalClassHydrator implements HydrationProvider<AsmMethodData> {

    private LocalClassHydrator() {}

    /**
     * Create a new instace of {@link LocalClassHydrator}.
     * @return A new instace of {@link LocalClassHydrator}.
     */
    @Contract(value = "-> new", pure = true)
    public static @NotNull LocalClassHydrator create() {
        return new LocalClassHydrator();
    }

    @Override
    public @NotNull Class<? extends AsmMethodData> target() {
        return AsmMethodData.class;
    }

    @Override
    public void hydrate(final @NotNull AsmMethodData data, final @NotNull HypoContext context) throws IOException {
        @Nullable ArrayList<AsmClassData> nestedClasses = null;

        final Set<@NotNull ClassData> innerClasses = data.parentClass().innerClasses();
        for (final ClassData innerClass : innerClasses) {
            final ClassNode innerNode = ((AsmClassData) innerClass).getNode();

            if (!data.name().equals(innerNode.outerMethod)) {
                continue;
            }
            if (!data.descriptorText().equals(innerNode.outerMethodDesc)) {
                continue;
            }

            if (nestedClasses == null) {
                nestedClasses = new ArrayList<>();
            }

            nestedClasses.add((AsmClassData) innerClass);
        }

        if (nestedClasses == null) {
            return;
        }

        final HashSet<ClassData> handledNestedClasses = new HashSet<>();

        final MethodNode node = data.getNode();
        for (final AbstractInsnNode insn : node.instructions) {
            if (insn.getOpcode() != Opcodes.INVOKESPECIAL) {
                continue;
            }

            final MethodInsnNode methodInsn = (MethodInsnNode) insn;
            if (!methodInsn.name.equals("<init>")) {
                continue;
            }

            final Iterator<AsmClassData> it = nestedClasses.iterator();
            while (it.hasNext()) {
                final AsmClassData nestedClass = it.next();

                if (handledNestedClasses.contains(nestedClass)) {
                    continue;
                }
                if (!nestedClass.name().equals(methodInsn.owner)) {
                    continue;
                }

                final int @Nullable [] closureIndices = this.handleNestedConst(methodInsn, nestedClass);
                handledNestedClasses.add(nestedClass);

                setCall(data, nestedClass, closureIndices != null ? closureIndices : MethodClosure.EMPTY_INT_ARRAY);
                it.remove();
            }
        }

        if (nestedClasses.isEmpty()) {
            return;
        }

        // If a local or anonymous class is defined in a lambda expression then the `outerMethod` will be
        // the method which contains the lambda, not the lambda itself.
        // At this time we will still mark it, we just won't track LVT. This at least tells us scoping.

        for (final AsmClassData nestedClass : nestedClasses) {
            setCall(data, nestedClass, MethodClosure.EMPTY_INT_ARRAY);
        }
    }

    private static void setCall(final MethodData data, final ClassData nestedClass, final int[] params) {
        final MethodClosure<ClassData> call = new MethodClosure<>(data, nestedClass, params);

        final List<MethodClosure<ClassData>> closures = data.compute(HypoHydration.LOCAL_CLASSES, ArrayList::new);
        synchronized (closures) {
            closures.add(call);
        }

        final List<MethodClosure<ClassData>> targetClosures = nestedClass.compute(HypoHydration.LOCAL_CLASSES, ArrayList::new);
        synchronized (targetClosures) {
            targetClosures.add(call);
        }
    }

    private int @Nullable [] handleNestedConst(final MethodInsnNode insn, final AsmClassData nestedClass) {
        final ArrayList<JvmType> capturedVariables = new ArrayList<>();
        for (final FieldNode field : nestedClass.getNode().fields) {
            if (field.name.startsWith("this")) {
                continue;
            }

            if ((field.access & Opcodes.ACC_SYNTHETIC) == 0) {
                break;
            }
            if (!field.name.startsWith("val$")) {
                break;
            }

            capturedVariables.add(toJvmType(getType(field.desc)));
        }

        final int varSize = capturedVariables.size();
        final int[] closureIndices = new int[varSize];

        AbstractInsnNode prevInsn = insn;
        for (int i = varSize - 1; i >= 0; i--) {
            prevInsn = prevInsn.getPrevious();

            if (prevInsn.getType() != AbstractInsnNode.VAR_INSN) {
                break;
            }

            final VarInsnNode var = (VarInsnNode) prevInsn;
            closureIndices[i] = var.var;

            if (i == 0) {
                return closureIndices;
            }
        }

        return null;
    }
}
