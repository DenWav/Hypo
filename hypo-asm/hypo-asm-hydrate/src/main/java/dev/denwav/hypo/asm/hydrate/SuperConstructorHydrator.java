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

package dev.denwav.hypo.asm.hydrate;

import dev.denwav.hypo.asm.AsmConstructorData;
import dev.denwav.hypo.core.HypoContext;
import dev.denwav.hypo.hydrate.HydrationProvider;
import dev.denwav.hypo.hydrate.generic.HypoHydration;
import dev.denwav.hypo.hydrate.generic.SuperCall;
import dev.denwav.hypo.model.HypoModelUtil;
import dev.denwav.hypo.model.data.ClassData;
import dev.denwav.hypo.model.data.ConstructorData;
import dev.denwav.hypo.model.data.MethodData;
import dev.denwav.hypo.model.data.MethodDescriptor;
import dev.denwav.hypo.model.data.types.JvmType;
import dev.denwav.hypo.model.data.types.PrimitiveType;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * {@link HydrationProvider} for determining the target of {@code this()} and {@code super()} constructor calls, as well
 * as which parameters are directly passed to it. This is implemented for the {@code hypo-asm} module and requires
 * {@link AsmConstructorData} to work.
 *
 * <p>This class fills in {@link HypoHydration#SUPER_CALL_TARGET} and {@link HypoHydration#SUPER_CALLER_SOURCES} keys on
 * {@link AsmConstructorData} objects.
 */
public class SuperConstructorHydrator implements HydrationProvider<AsmConstructorData> {

    private SuperConstructorHydrator() {}

    /**
     * Create a new instance of {@link SuperConstructorHydrator}.
     * @return A new instance of {@link SuperConstructorHydrator}.
     */

    @Contract(value = "-> new", pure = true)
    public static @NotNull SuperConstructorHydrator create() {
        return new SuperConstructorHydrator();
    }

    @Override
    public @NotNull Class<? extends AsmConstructorData> target() {
        return AsmConstructorData.class;
    }

    @Override
    public void hydrate(@NotNull AsmConstructorData data, @NotNull HypoContext context) throws IOException {
        try {
            this.hydrate0(data);
        } catch (final IllegalStateException e) {
            // TODO remove
            System.out.println(e.getMessage());
        }
    }

    private void hydrate0(final @NotNull AsmConstructorData data) throws IOException {
        final MethodCall superCall = buildSuperCall(data);
        if (superCall == null) {
            return;
        }

        final ClassData thisClass = data.parentClass();
        final String owner = superCall.owner;
        final String desc = superCall.desc;
        if (owner == null || desc == null) {
            throw new IllegalStateException("Could not determine owner or desc of super method");
        }
        final String normalizedOwner = HypoModelUtil.normalizedClassName(owner);

        final ClassData targetClass;
        final ClassData superClass = thisClass.superClass();
        if (thisClass.name().equals(normalizedOwner)) {
            targetClass = thisClass;
        } else if (superClass != null && superClass.name().equals(normalizedOwner)) {
            targetClass = superClass;
        } else if (superClass == null) {
            throw new IllegalStateException("Could not find owner of super method");
        } else {
            throw new IllegalStateException("Could not determine owner of super method");
        }

        final MethodData targetMethod = targetClass.method("<init>", MethodDescriptor.parseDescriptor(desc));
        if (!(targetMethod instanceof ConstructorData)) {
            throw new IllegalStateException("Target constructor is not an instance of " + ConstructorData.class.getName());
        }
        final ConstructorData targetConstructor = (ConstructorData) targetMethod;

        final ArrayList<SuperCall.SuperCallParameter> superCallParams = new ArrayList<>();
        final SuperCall superCallData = new SuperCall(data, targetConstructor, superCallParams);

        // Store our value
        data.store(HypoHydration.SUPER_CALL_TARGET, superCallData);
        final List<SuperCall> superCallers = targetConstructor.compute(HypoHydration.SUPER_CALLER_SOURCES, ArrayList::new);
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (superCallers) {
            superCallers.add(superCallData);
        }

        // Determine output param indexes
        final int[] thisParamIndices = buildParamIndexMapping(data);
        final int[] targetParamIndices = buildParamIndexMapping(targetConstructor);

        // Do what we can to match parameters
        int index = -1;
        for (final MethodCallArgument arg : superCall.args) {
            index++;
            // We only match simple cases
            final Variable varArgument;

            if (arg instanceof Variable) {
                varArgument = (Variable) arg;
            } else if (arg instanceof MethodCall) {
                // We will still match the name if a constructor parameter is the only argument passed to a method
                // This could be for example where the sub-class calls a method to transform the input, but it's
                // still the same input. For example maybe something like:
                //
                //     public SomeClass(String s) {
                //         super(toUppercase(s));
                //     }
                //
                // or
                //
                //     public SomeClass(String s) {
                //         super(s.toUppercase());
                //     }
                final MethodCall subCall = (MethodCall) arg;
                if (subCall.args.size() != 1 && !(subCall.receiver instanceof Variable)) {
                    continue;
                }

                if (subCall.receiver instanceof Variable) {
                    // This is potentially an instance method on a method argument, keep it
                    varArgument = (Variable) subCall.receiver;
                } else {
                    final MethodCallArgument first = subCall.args.getFirst();
                    if (!(first instanceof Variable)) {
                        continue;
                    }
                    // This is potentially a method which only takes in a variable as input
                    varArgument = (Variable) first;
                }
            } else {
                continue;
            }

            final int varIndex = varArgument.index;
            if (varIndex > thisParamIndices[thisParamIndices.length - 1]) {
                // This isn't a parameter
                continue;
            }

            superCallParams.add(new SuperCall.SuperCallParameter(varIndex, targetParamIndices[index]));
        }
    }

    /**
     * This method handles most typical constructor {@code super()} and {@code this()} calls. It walks the method
     * instructions and records how each instruction affects the stack. Once the {@code super()} or {@code this()} call
     * is found (and verifying the call comes from an {@code INVOKESPECIAL} instruction) the built {@link MethodCall} is
     * returned.
     *
     * <p>This method does not handle invalid bytecode or weird bytecode. It is intended solely to  process bytecode
     * generated by standard {@code javac} which requires {@code super()} calls to be the first statement in a
     * constructor. Any constructor which may have bytecode different from this pattern, or more complex constructors,
     * will not be supported by this method.
     *
     * @param data The constructor to build the super call data from.
     * @return The build {@link MethodCall} corresponding to this constructor's {@code super()} call, if it's possible
     *         to build.
     */
    private static @Nullable MethodCall buildSuperCall(final @NotNull AsmConstructorData data) {
        final MethodCall superCall = new MethodCall();

        loop: for (AbstractInsnNode insn = data.getNode().instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if ((insn instanceof LabelNode) || (insn instanceof LineNumberNode) || (insn instanceof FrameNode)) {
                continue;
            }

            final int opcode = insn.getOpcode();
            switch (opcode) {
                case Opcodes.INVOKEDYNAMIC:
                    final InvokeDynamicInsnNode dynNode = (InvokeDynamicInsnNode) insn;
                    final int dynArgCount = Type.getArgumentTypes(dynNode.desc).length;
                    superCall.collapseInvoke(dynArgCount, true, null, dynNode.desc);
                    continue;
                case Opcodes.INVOKESPECIAL:
                case Opcodes.INVOKESTATIC:
                case Opcodes.INVOKEVIRTUAL:
                case Opcodes.INVOKEINTERFACE:
                    final MethodInsnNode methodNode = (MethodInsnNode) insn;
                    final int argCount = Type.getArgumentTypes(methodNode.desc).length;
                    final boolean isStatic = opcode == Opcodes.INVOKESTATIC;
                    superCall.collapseInvoke(argCount, isStatic, methodNode.owner, methodNode.desc);
                    if (superCall.receiver != null) {
                        if (opcode != Opcodes.INVOKESPECIAL) {
                            throw new IllegalStateException("Super call collapsed on non-INVOKESPECIAL instruction: " + opcode);
                        }
                        break loop;
                    }
                    continue;
                case Opcodes.NEW:
                    superCall.args.add(new NewCall(((TypeInsnNode) insn).desc));
                    continue;
                case Opcodes.NEWARRAY:
                case Opcodes.ANEWARRAY:
                    superCall.args.removeLast();
                    superCall.args.addLast(NewArray.INSTANCE);
                    continue;
                case Opcodes.DUP:
                    final MethodCallArgument dupStack = superCall.args.peekLast();
                    if (dupStack != null) {
                        superCall.args.addLast(dupStack);
                    }
                    continue;
                case Opcodes.POP2:
                    superCall.args.removeLast();
                    // fallthrough
                case Opcodes.POP:
                    superCall.args.removeLast();
                    continue;
                case Opcodes.SWAP:
                    final MethodCallArgument swap1 = superCall.args.removeLast();
                    final MethodCallArgument swap2 = superCall.args.removeLast();
                    superCall.args.addLast(swap1);
                    superCall.args.addLast(swap2);
                    continue;
                case Opcodes.ILOAD:
                case Opcodes.LLOAD:
                case Opcodes.FLOAD:
                case Opcodes.DLOAD:
                case Opcodes.ALOAD:
                    superCall.args.add(new Variable(((VarInsnNode) insn).var));
                    continue;
                case Opcodes.IASTORE:
                case Opcodes.LASTORE:
                case Opcodes.FASTORE:
                case Opcodes.DASTORE:
                case Opcodes.AASTORE:
                case Opcodes.BASTORE:
                case Opcodes.CASTORE:
                case Opcodes.SASTORE:
                    superCall.args.removeLast();
                    superCall.args.removeLast();
                    // fallthrough
                case Opcodes.ISTORE:
                case Opcodes.LSTORE:
                case Opcodes.FSTORE:
                case Opcodes.DSTORE:
                case Opcodes.ASTORE:
                    superCall.args.removeLast();
                    continue;
                case Opcodes.GETSTATIC:
                case Opcodes.LDC:
                case Opcodes.ICONST_M1:
                case Opcodes.ICONST_0:
                case Opcodes.ICONST_1:
                case Opcodes.ICONST_2:
                case Opcodes.ICONST_3:
                case Opcodes.ICONST_4:
                case Opcodes.ICONST_5:
                case Opcodes.LCONST_0:
                case Opcodes.LCONST_1:
                case Opcodes.FCONST_0:
                case Opcodes.FCONST_1:
                case Opcodes.FCONST_2:
                case Opcodes.DCONST_0:
                case Opcodes.DCONST_1:
                case Opcodes.ACONST_NULL:
                case Opcodes.BIPUSH:
                case Opcodes.SIPUSH:
                    superCall.args.add(Constant.INSTANCE);
                    continue;
                // operators
                case Opcodes.IADD:
                case Opcodes.LADD:
                case Opcodes.FADD:
                case Opcodes.DADD:
                case Opcodes.ISUB:
                case Opcodes.LSUB:
                case Opcodes.FSUB:
                case Opcodes.DSUB:
                case Opcodes.IMUL:
                case Opcodes.LMUL:
                case Opcodes.FMUL:
                case Opcodes.DMUL:
                case Opcodes.IDIV:
                case Opcodes.LDIV:
                case Opcodes.FDIV:
                case Opcodes.DDIV:
                case Opcodes.IREM:
                case Opcodes.LREM:
                case Opcodes.FREM:
                case Opcodes.DREM:
                case Opcodes.INEG:
                case Opcodes.LNEG:
                case Opcodes.FNEG:
                case Opcodes.DNEG:
                case Opcodes.ISHL:
                case Opcodes.LSHL:
                case Opcodes.ISHR:
                case Opcodes.LSHR:
                case Opcodes.IUSHR:
                case Opcodes.LUSHR:
                case Opcodes.IAND:
                case Opcodes.LAND:
                case Opcodes.IOR:
                case Opcodes.LOR:
                case Opcodes.IXOR:
                case Opcodes.LXOR:
                case Opcodes.LCMP:
                case Opcodes.FCMPL:
                case Opcodes.FCMPG:
                case Opcodes.DCMPL:
                case Opcodes.DCMPG:
                // array access
                case Opcodes.IALOAD:
                case Opcodes.LALOAD:
                case Opcodes.FALOAD:
                case Opcodes.DALOAD:
                case Opcodes.AALOAD:
                case Opcodes.BALOAD:
                case Opcodes.CALOAD:
                case Opcodes.SALOAD:
                    superCall.args.removeLast();
                    superCall.args.removeLast();
                    superCall.args.addLast(Constant.INSTANCE);
                    continue;
                case Opcodes.I2L:
                case Opcodes.I2F:
                case Opcodes.I2D:
                case Opcodes.L2I:
                case Opcodes.L2F:
                case Opcodes.L2D:
                case Opcodes.F2I:
                case Opcodes.F2L:
                case Opcodes.F2D:
                case Opcodes.D2I:
                case Opcodes.D2L:
                case Opcodes.D2F:
                case Opcodes.I2B:
                case Opcodes.I2C:
                case Opcodes.I2S:
                case Opcodes.CHECKCAST:
                case Opcodes.INSTANCEOF:
                    //  Doesn't affect the stack in any way that matters to us
                    continue;
                case Opcodes.PUTFIELD:
                    // inner class
                    // This will consume the previous 2 var instructions
                    superCall.args.removeLast();
                    superCall.args.removeLast();
                    continue;
                case Opcodes.GETFIELD:
                    final FieldAccess fieldAccess = new FieldAccess(superCall.args.removeLast());
                    superCall.args.addLast(fieldAccess);
                    continue;
                case Opcodes.ARRAYLENGTH:
                    superCall.args.removeLast();
                    superCall.args.addLast(Constant.INSTANCE);
                    continue;
                case Opcodes.IF_ICMPEQ:
                case Opcodes.IF_ICMPNE:
                case Opcodes.IF_ICMPLT:
                case Opcodes.IF_ICMPGE:
                case Opcodes.IF_ICMPGT:
                case Opcodes.IF_ICMPLE:
                case Opcodes.IF_ACMPEQ:
                case Opcodes.IF_ACMPNE:
                    // Take the default path for jumps, which is to do nothing
                    superCall.args.removeLast();
                    // fallthrough
                case Opcodes.IFEQ:
                case Opcodes.IFNE:
                case Opcodes.IFLT:
                case Opcodes.IFGE:
                case Opcodes.IFGT:
                case Opcodes.IFLE:
                case Opcodes.IFNULL:
                case Opcodes.IFNONNULL:
                    superCall.args.removeLast();
                    continue;
                case Opcodes.GOTO:
                    // Default (only) path for GOTO is to jump
                    insn = ((JumpInsnNode) insn).label;
                    continue;
                case Opcodes.JSR:
                case Opcodes.RET:
                case Opcodes.IRETURN:
                case Opcodes.LRETURN:
                case Opcodes.FRETURN:
                case Opcodes.DRETURN:
                case Opcodes.ARETURN:
                case Opcodes.RETURN:
                    // This shouldn't happen before a super call
                    return null;
                default:
                    throw new IllegalStateException("Instruction not expected, probably a more complex case: " + insn.getOpcode());
            }
        }

        // Verify what we have is actually a valid super call before returning it
        if (superCall.receiver == null) {
            return null;
        }

        final MethodCallArgument receiver = superCall.receiver;
        if (!(receiver instanceof Variable) || ((Variable) receiver).index != 0) {
            throw new IllegalStateException("Receiver for super call is not `this`");
        }

        return superCall;
    }

    private static int @NotNull [] buildParamIndexMapping(final @NotNull MethodData data) throws IOException {
        final List<@NotNull JvmType> targetParams = data.params();
        final int[] outputParamIndices = new int[targetParams.size()];
        int currentIndex = 0;
        int currentTargetIndex = 1; // `this` is 0

        if (data.parentClass().outerClass() != null && !data.parentClass().isStaticInnerClass()) {
            currentTargetIndex++; // index 1 is the outer class
        }

        for (final JvmType paramType : targetParams) {
            outputParamIndices[currentIndex] = currentTargetIndex;
            currentIndex++;
            currentTargetIndex++;
            if (paramType == PrimitiveType.LONG || paramType == PrimitiveType.DOUBLE) {
                currentTargetIndex++;
            }
        }
        return outputParamIndices;
    }
}

//
// Below are the classes which make up the simplified view of the bytecode model of the super call.
//

/**
 * Model for {@link SuperConstructorHydrator}.
 *
 * <p>Represents something which affects the Java stack in some way.
 */
interface MethodCallArgument {}

/**
 * Model for {@link SuperConstructorHydrator}.
 *
 * <p>This is mainly what we're looking to keep track of in {@link MethodCall}. This will tell us the LVT index
 * corresponding to the method call index.
 */
final class Variable implements MethodCallArgument {
    /**
     * Model for {@link SuperConstructorHydrator}.
     */
    final int index;

    /**
     * Model for {@link SuperConstructorHydrator}.
     *
     * @param index LVT index.
     */
    Variable(int index) {
        this.index = index;
    }
}

/**
 * Model for {@link SuperConstructorHydrator}.
 */
final class FieldAccess implements MethodCallArgument {
    /**
     * Model for {@link SuperConstructorHydrator}.
     */
    final MethodCallArgument receiver;

    /**
     * Model for {@link SuperConstructorHydrator}.
     *
     * @param receiver Method receiver ({@code this}).
     */
    FieldAccess(MethodCallArgument receiver) {
        this.receiver = receiver;
    }
}

/**
 * Model for {@link SuperConstructorHydrator}.
 *
 * <p>This applies to any load operation not referring to a local variable. We essentially just throw that data away.
 */
final class Constant implements MethodCallArgument {
    /**
     * Model for {@link SuperConstructorHydrator}.
     */
    static final Constant INSTANCE = new Constant();
}

/**
 * Model for {@link SuperConstructorHydrator}.
 *
 * <p>{@code new} expressions.
 */
final class NewCall implements MethodCallArgument {
    /**
     * Model for {@link SuperConstructorHydrator}.
     */
    final String desc;

    /**
     * Model for {@link SuperConstructorHydrator}.
     *
     * @param desc Method descriptor.
     */
    NewCall(String desc) {
        this.desc = desc;
    }
}

/**
 * Model for {@link SuperConstructorHydrator}.
 *
 * <p>{@code new} expressions for arrays.
 */
final class NewArray implements MethodCallArgument {
    /**
     * Model for {@link SuperConstructorHydrator}.
     */
    static final NewArray INSTANCE = new NewArray();
}

/**
 * Model for {@link SuperConstructorHydrator}.
 *
 * <p>This is the core stack model for the hydrator, it represents method calls and keeps track of which stack items
 * correspond with which method parameters.
 */
final class MethodCall implements MethodCallArgument {

    /**
     * Model for {@link SuperConstructorHydrator}.
     *
     * <p>The arguments for this method, made up of stack items.
     */
    final @NotNull ArrayDeque<MethodCallArgument> args = new ArrayDeque<>();

    /**
     * Model for {@link SuperConstructorHydrator}.
     *
     * <p>If this method is not static, the stack item which is the {@code this} object for this method call.
     */
    @Nullable MethodCallArgument receiver;
    /**
     * Model for {@link SuperConstructorHydrator}.
     *
     * <p>The declaring class of the method.
     */
    @Nullable String owner;
    /**
     * Model for {@link SuperConstructorHydrator}.
     *
     * <p>The method descriptor.
     */
    @Nullable String desc;

    /**
     * Model for {@link SuperConstructorHydrator}.
     *
     * <p>This will collapse the current stack in {@link #args} into a new {@link MethodCall} and replace the args which
     * made up that method call with the method call itself. This effectively turns the arguments for the method call
     * into the method call itself.
     *
     * @param argCount Number of arguments to consume.
     * @param isStatic Whether the method is static.
     * @param owner The owning class of the method.
     * @param desc The method descriptor.
     */
    void collapseInvoke(
        final int argCount,
        final boolean isStatic,
        final @Nullable String owner,
        final @NotNull String desc
    ) {
        final int back = argCount + (isStatic ? 0 : 1);
        final int count = this.args.size();

        if (back == count) {
            // This is our invoke instruction
            this.receiver = this.args.removeFirst();
            this.owner = owner;
            this.desc = desc;
            return;
        }

        if (back > count) {
            throw new IllegalStateException("Cannot collapse args - requested " + back + " args but only have " + count);
        }

        final MethodCall innerCall = new MethodCall();
        for (int i = 0; i < argCount; i++) {
            innerCall.args.addFirst(this.args.removeLast());
        }
        if (!isStatic) {
            innerCall.receiver = this.args.removeLast();
        }
        if (Objects.equals(Type.getReturnType(desc), Type.VOID_TYPE)) {
            // If this method returns void and this is part of the super() call, that means this
            // was executed off of a DUP
            this.args.removeLast();
        }
        this.args.addLast(innerCall);

        innerCall.owner = owner;
        innerCall.desc = desc;
    }
}
