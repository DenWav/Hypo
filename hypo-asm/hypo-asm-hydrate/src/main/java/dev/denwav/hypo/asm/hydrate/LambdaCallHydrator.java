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

import dev.denwav.hypo.asm.AsmMethodData;
import dev.denwav.hypo.asm.HypoAsmUtil;
import dev.denwav.hypo.core.HypoContext;
import dev.denwav.hypo.hydrate.HydrationProvider;
import dev.denwav.hypo.hydrate.generic.HypoHydration;
import dev.denwav.hypo.hydrate.generic.LambdaClosure;
import dev.denwav.hypo.model.data.ClassData;
import dev.denwav.hypo.model.data.HypoKey;
import dev.denwav.hypo.model.data.MethodData;
import dev.denwav.hypo.types.desc.MethodDescriptor;
import dev.denwav.hypo.types.desc.TypeDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import static dev.denwav.hypo.asm.HypoAsmUtil.toDescriptor;

/**
 * This is a {@link HydrationProvider} for determining lambda expressions present in methods, and the
 * local variables which they capture. It sets {@link HypoHydration#LAMBDA_CALLS} on both the methods which contain
 * the lambda expressions, and the lambda methods themselves.
 */
public final class LambdaCallHydrator implements HydrationProvider<AsmMethodData> {

    private static final Handle lambdaMetafactoryHandle = new Handle(
        Opcodes.H_INVOKESTATIC,
        "java/lang/invoke/LambdaMetafactory",
        "metafactory",
            /* Provided */ "(Ljava/lang/invoke/MethodHandles$Lookup;" + // caller
            /*    by    */ "Ljava/lang/String;" +                       // interfaceMethodName
            /*    JVM   */ "Ljava/lang/invoke/MethodType;" +            // factoryType
            //----------//
            /*          */ "Ljava/lang/invoke/MethodType;" +            // interfaceMethodType
            /* BSM args */ "Ljava/lang/invoke/MethodHandle;" +          // implementation
            /*          */ "Ljava/lang/invoke/MethodType;" +            // dynamicMethodType
            ")" +
            "Ljava/lang/invoke/CallSite;",
        false
    );

    private LambdaCallHydrator() {}

    /**
     * Create a new instance of {@link LambdaCallHydrator}.
     * @return A new instance of {@link LambdaCallHydrator}.
     */
    @Contract(value = "-> new", pure = true)
    public static @NotNull LambdaCallHydrator create() {
        return new LambdaCallHydrator();
    }

    @Override
    public List<HypoKey<?>> provides() {
        return List.of(HypoHydration.LAMBDA_CALLS);
    }

    @Override
    public @NotNull Class<? extends AsmMethodData> target() {
        return AsmMethodData.class;
    }

    @Override
    public void hydrate(final @NotNull AsmMethodData data, final @NotNull HypoContext context) throws IOException {
        final MethodNode node = data.getNode();

        for (final AbstractInsnNode insn : node.instructions) {
            if (insn.getOpcode() != Opcodes.INVOKEDYNAMIC) {
                continue;
            }

            final InvokeDynamicInsnNode dyn = (InvokeDynamicInsnNode) insn;
            if (!lambdaMetafactoryHandle.equals(dyn.bsm)) {
                continue;
            }

            // This is a lambda CallSite
            if (dyn.bsmArgs.length != 3) {
                // This is invalid bytecode
                continue;
            }

            final Object bsmArgHandle = dyn.bsmArgs[1];
            if (!(bsmArgHandle instanceof Handle)) {
                // This is also invalid bytecode
                continue;
            }
            final Handle handle = (Handle) bsmArgHandle;

            final Object bsmDesc = dyn.bsmArgs[0];
            if (!(bsmDesc instanceof Type)) {
                // This is also invalid bytecode
                continue;
            }
            final Type interfaceDesc = (Type) bsmDesc;

            final ClassData owner;
            if (data.parentClass().name().equals(handle.getOwner())) {
                owner = data.parentClass();
            } else {
                owner = context.getContextProvider().findClass(handle.getOwner());
            }

            if (owner == null) {
                continue;
            }

            final MethodDescriptor desc = MethodDescriptor.parse(dyn.desc);
            final List<? extends @NotNull TypeDescriptor> params = desc.getParameters();
            final int paramsSize = params.size();
            final int[] closureIndices = new int[paramsSize];

            boolean finished = false;
            AbstractInsnNode prevInsn = insn;
            for (int i = paramsSize - 1; i >= 0; i--) {
                prevInsn = prevInsn.getPrevious();

                if (prevInsn.getType() != AbstractInsnNode.VAR_INSN) {
                    break;
                }

                final VarInsnNode var = (VarInsnNode) prevInsn;
                closureIndices[i] = var.var;

                if (i == 0) {
                    finished = true;
                }
            }

            final MethodData targetMethod = owner.method(handle.getName(), MethodDescriptor.parse(handle.getDesc()));
            if (targetMethod == null) {
                continue;
            }

            @Nullable MethodData interfaceMethod = null;
            final ClassData interfaceType = context.getContextProvider().findClass(desc.getReturnType());
            if (interfaceType != null) {
                interfaceMethod = interfaceType.method(dyn.name, toDescriptor(interfaceDesc));
            }

            final LambdaClosure call = new LambdaClosure(
                data,
                interfaceMethod,
                targetMethod,
                finished ? closureIndices : HypoAsmUtil.EMPTY_INT_ARRAY
            );

            final List<LambdaClosure> methodClosures = data.compute(HypoHydration.LAMBDA_CALLS, ArrayList::new);
            synchronized (methodClosures) {
                methodClosures.add(call);
            }

            final List<LambdaClosure> targetCalls = targetMethod.compute(HypoHydration.LAMBDA_CALLS, ArrayList::new);
            synchronized (targetCalls) {
                targetCalls.add(call);
            }

            if (interfaceMethod != null) {
                final List<LambdaClosure> interfaceMethodLambdas = interfaceMethod.compute(HypoHydration.LAMBDA_CALLS, ArrayList::new);
                outer: synchronized (interfaceMethodLambdas) {
                    for (final LambdaClosure lambdaClosure : interfaceMethodLambdas) {
                        if (lambdaClosure.getLambda().equals(targetMethod)) {
                            break outer;
                        }
                    }
                    // only add if the target method isn't already included in a lambda to avoid duplicates
                    interfaceMethodLambdas.add(call);
                }
            }
        }
    }
}
