package dev.denwav.hypo.asm.hydrate;

import dev.denwav.hypo.asm.AsmClassData;
import dev.denwav.hypo.asm.AsmMethodData;
import dev.denwav.hypo.core.HypoContext;
import dev.denwav.hypo.hydrate.HydrationProvider;
import dev.denwav.hypo.hydrate.generic.HypoHydration;
import dev.denwav.hypo.hydrate.generic.MethodClosure;
import dev.denwav.hypo.model.data.ClassData;
import dev.denwav.hypo.model.data.types.JvmType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
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

public final class LocalClassHydrator implements HydrationProvider<AsmMethodData> {

    private LocalClassHydrator() {}

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

            for (final AsmClassData nestedClass : nestedClasses) {
                if (handledNestedClasses.contains(nestedClass)) {
                    continue;
                }
                if (!nestedClass.name().equals(methodInsn.owner)) {
                    continue;
                }

                final int @Nullable [] closureIndices = this.handleNestedConst(node, methodInsn, nestedClass);
                handledNestedClasses.add(nestedClass);

                final MethodClosure<ClassData> call = new MethodClosure<>(data, nestedClass, closureIndices != null ? closureIndices : MethodClosure.EMPTY);

                final List<MethodClosure<ClassData>> closures = data.compute(HypoHydration.LOCAL_CLASSES, ArrayList::new);
                synchronized (closures) {
                    closures.add(call);
                }

                final List<MethodClosure<ClassData>> targetClosures = nestedClass.compute(HypoHydration.LOCAL_CLASSES, ArrayList::new);
                synchronized (targetClosures) {
                    targetClosures.add(call);
                }
            }
        }
    }

    private int @Nullable [] handleNestedConst(final MethodNode node, final MethodInsnNode insn, final AsmClassData nestedClass) {
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
