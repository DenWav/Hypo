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
import dev.denwav.hypo.model.data.ClassData;
import dev.denwav.hypo.model.data.HypoKey;
import dev.denwav.hypo.model.data.MethodData;
import dev.denwav.hypo.types.desc.MethodDescriptor;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * {@link HydrationProvider} for determining synthetic bridge method targets and sources on {@link AsmMethodData}
 * objects.
 *
 * <p>This class fills in {@link HypoHydration#SYNTHETIC_SOURCES} and {@link HypoHydration#SYNTHETIC_TARGET} keys on
 * {@link AsmMethodData} objects.
 */
public class BridgeMethodHydrator implements HydrationProvider<AsmMethodData> {

    private BridgeMethodHydrator() {}

    /**
     * Create a new instance of {@link BridgeMethodHydrator}.
     * @return A new instance of {@link BridgeMethodHydrator}.
     */
    @Contract(value = "-> new", pure = true)
    public static @NotNull BridgeMethodHydrator create() {
        return new BridgeMethodHydrator();
    }

    @Override
    public @NotNull Class<? extends AsmMethodData> target() {
        return AsmMethodData.class;
    }

    @Override
    public List<HypoKey<?>> provides() {
        return List.of(HypoHydration.SYNTHETIC_SOURCES, HypoHydration.SYNTHETIC_TARGET);
    }

    @Override
    public void hydrate(@NotNull AsmMethodData data, @NotNull HypoContext context) throws IOException {
        if (!data.isSynthetic() || data.name().indexOf('$') != -1) {
            return;
        }

        State state = State.IN_PARAMS;
        int nextLvt = 0;

        @Nullable MethodInsnNode invokeInsn = null;

        for (final AbstractInsnNode insn : data.getNode().instructions) {
            if (insn instanceof LabelNode || insn instanceof LineNumberNode || insn instanceof TypeInsnNode) {
                continue;
            }

            if (state == State.IN_PARAMS) {
                if (!(insn instanceof VarInsnNode) || ((VarInsnNode) insn).var != nextLvt) {
                    state = State.INVOKE;
                }
            }

            final int opcode = insn.getOpcode();
            switch (state) {
                case IN_PARAMS:
                    nextLvt++;
                    if (opcode == Opcodes.LLOAD || opcode == Opcodes.DLOAD) {
                        nextLvt++;
                    }
                    break;
                case INVOKE:
                    // Must be a virtual or interface or special invoke instruction
                    if (opcode != Opcodes.INVOKEVIRTUAL && opcode != Opcodes.INVOKEINTERFACE && opcode != Opcodes.INVOKESPECIAL) {
                        return;
                    }

                    //noinspection DataFlowIssue
                    invokeInsn = (MethodInsnNode) insn;
                    state = State.RETURN;
                    break;
                case RETURN:
                    // The next instruction must be a return
                    if (opcode < Opcodes.IRETURN || opcode > Opcodes.RETURN) {
                        return;
                    }

                    state = State.OTHER_INSN;
                    break;
                case OTHER_INSN:
                    // We shouldn't see any other instructions
                    return;
            }
        }

        if (invokeInsn == null) {
            return;
        }
        final @NotNull MethodInsnNode invoke = invokeInsn;

        if (invoke.name.indexOf('$') != -1) {
            // not a bridge method
            return;
        }

        // Must be a method in the same class or a super class with a different signature
        final AsmClassData parent = data.parentClass();
        final ClassData grandParent = parent.superClass();
        final ClassData owner;
        if (parent.name().equals(invoke.owner)) {
            owner = parent;
        } else if (grandParent != null && grandParent.name().equals(invoke.owner)) {
            owner = grandParent;
        } else {
            return;
        }
        if (data.name().equals(invoke.name) && data.getNode().desc.equals(invoke.desc)) {
            return;
        }

        // The descriptors need to be the same size
        final MethodDescriptor invokeDesc = MethodDescriptor.parse(invoke.desc);
        if (data.params().size() != invokeDesc.getParameters().size()) {
            return;
        }

        final MethodData targetMethod = owner.method(invoke.name, invokeDesc);
        if (targetMethod == null) {
            return;
        }

        data.store(HypoHydration.SYNTHETIC_TARGET, targetMethod);
        setSynthSource(targetMethod, data);
        final Set<MethodData> sources = targetMethod.compute(HypoHydration.SYNTHETIC_SOURCES, HashSet::new);
        synchronized (sources) {
            sources.add(data);
        }
    }

    @SuppressWarnings("deprecation")
    private static void setSynthSource(final MethodData targetMethod, final MethodData data) {
        targetMethod.store(HypoHydration.SYNTHETIC_SOURCE, data);
    }

    /**
     * State for the parser in {@link #hydrate(AsmMethodData, HypoContext)}, used internally.
     */
    enum State {
        /**
         * Checking method parameter LVT indexes.
         */
        IN_PARAMS,
        /**
         * Found invoke instruction.
         */
        INVOKE,
        /**
         * Found return instruction.
         */
        RETURN,
        /**
         * After return instruction.
         */
        OTHER_INSN
    }
}
