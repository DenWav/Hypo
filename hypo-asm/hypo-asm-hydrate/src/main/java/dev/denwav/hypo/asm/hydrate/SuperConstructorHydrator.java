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

import dev.denwav.hypo.asm.AsmConstructorData;
import dev.denwav.hypo.core.HypoContext;
import dev.denwav.hypo.hydrate.HydrationProvider;
import dev.denwav.hypo.hydrate.generic.HypoHydration;
import dev.denwav.hypo.hydrate.generic.SuperCall;
import dev.denwav.hypo.model.HypoModelUtil;
import dev.denwav.hypo.model.data.ClassData;
import dev.denwav.hypo.model.data.ConstructorData;
import dev.denwav.hypo.model.data.HypoKey;
import dev.denwav.hypo.model.data.MethodData;
import dev.denwav.hypo.types.PrimitiveType;
import dev.denwav.hypo.types.desc.MethodDescriptor;
import dev.denwav.hypo.types.desc.TypeDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicInterpreter;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Interpreter;
import org.objectweb.asm.tree.analysis.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link HydrationProvider} for determining the target of {@code this()} and {@code super()} constructor calls, as well
 * as which parameters are directly passed to it. This is implemented for the {@code hypo-asm} module and requires
 * {@link AsmConstructorData} to work.
 *
 * <p>This class fills in {@link HypoHydration#SUPER_CALL_TARGET} and {@link HypoHydration#SUPER_CALLER_SOURCES} keys on
 * {@link AsmConstructorData} objects.
 */
public class SuperConstructorHydrator implements HydrationProvider<AsmConstructorData> {

    private static final Logger logger = LoggerFactory.getLogger(SuperConstructorHydrator.class);

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
    public List<HypoKey<?>> provides() {
        return List.of(HypoHydration.SUPER_CALL_TARGET, HypoHydration.SUPER_CALLER_SOURCES);
    }

    @Override
    public @NotNull Class<? extends AsmConstructorData> target() {
        return AsmConstructorData.class;
    }

    @Override
    public void hydrate(final @NotNull AsmConstructorData data, final @NotNull HypoContext context) throws IOException {
        try {
            this.hydrate0(data);
        } catch (final IllegalStateException e) {
            logger.debug(
                "Failed to determine super constructor linking for {}#{}{}: {}",
                data.parentClass().name(), data.name(), data.descriptorText(), e.getMessage()
            );
        }
    }

    private void hydrate0(final @NotNull AsmConstructorData data) throws IOException {
        final SuperCallSite superCallSite = findSuperCall(data);
        if (superCallSite == null) {
            return;
        }

        final ClassData thisClass = data.parentClass();
        final String owner = superCallSite.owner();
        final String desc = superCallSite.desc();
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
            throw new IllegalStateException("Owner of super method for " + data + " is invalid: "
                + normalizedOwner + ". ");
        }

        final MethodData targetMethod = targetClass.method("<init>", MethodDescriptor.parse(desc));
        if (!(targetMethod instanceof final ConstructorData targetConstructor)) {
            throw new IllegalStateException("Target constructor is not an instance of "
                + ConstructorData.class.getName());
        }

        final ArrayList<SuperCall.SuperCallParameter> superCallParams = new ArrayList<>();
        final SuperCall superCallData = new SuperCall(data, targetConstructor, superCallParams);

        // Store our value
        data.store(HypoHydration.SUPER_CALL_TARGET, superCallData);
        final var superCallers = targetConstructor.compute(HypoHydration.SUPER_CALLER_SOURCES, ArrayList::new);
        synchronized (superCallers) {
            superCallers.add(superCallData);
        }

        // Determine output param indexes
        final int[] thisParamIndices = buildParamIndexMapping(data);
        final int[] targetParamIndices = buildParamIndexMapping(targetConstructor);

        // Match each traced argument back to one of this constructor's own parameters
        int index = -1;
        for (final TraceValue arg : superCallSite.args()) {
            index++;

            final int lvtIndex;
            final boolean directMatch;
            switch (arg.kind) {
                case PARAM:
                    lvtIndex = arg.lvtIndex;
                    directMatch = true;
                    break;
                case WRAPPED:
                    lvtIndex = arg.lvtIndex;
                    directMatch = false;
                    break;
                case NONE:
                case UNINITIALIZED_THIS:
                default:
                    continue;
            }

            if (lvtIndex > thisParamIndices[thisParamIndices.length - 1]) {
                // Defensive: PARAM/WRAPPED are only ever seeded from this constructor's own declared
                // parameter locals (SuperCallInterpreter#newParameterValue), so this is unreachable.
                continue;
            }

            if (directMatch) {
                // arguments which are directly passed through take priority
                superCallParams.removeIf(s -> s.thisIndex() == lvtIndex);
            } else {
                // we only take method calls if they aren't overwriting anything else
                boolean anyExist = false;
                for (final SuperCall.SuperCallParameter superCallParam : superCallParams) {
                    if (superCallParam.thisIndex() == lvtIndex) {
                        anyExist = true;
                        break;
                    }
                }
                if (anyExist) {
                    continue;
                }
            }

            superCallParams.add(new SuperCall.SuperCallParameter(lvtIndex, targetParamIndices[index]));
        }
    }

    /**
     * Locate this constructor's real {@code super()}/{@code this()} call by running ASM's {@link Analyzer} with a
     * {@link SuperCallInterpreter} over the whole method. The JVM spec guarantees exactly one {@code INVOKESPECIAL
     * <init>} instruction consumes the incoming uninitialized {@code this} (local {@code 0}), and that instruction
     * is this constructor's real {@code super()}/{@code this()} call.
     *
     * @param data The constructor to search.
     * @return The located call's owner, descriptor, and traced arguments, or {@code null} if this constructor's
     *         instructions never make such a call.
     */
    private static @Nullable SuperCallSite findSuperCall(final @NotNull AsmConstructorData data) throws IOException {
        final MethodNode methodNode = data.getNode();
        final SuperCallInterpreter interpreter = new SuperCallInterpreter();
        try {
            new Analyzer<>(interpreter).analyze(data.parentClass().name(), methodNode);
        } catch (final AnalyzerException e) {
            throw new IllegalStateException("Failed to analyze constructor for super()/this() call", e);
        }

        final MethodInsnNode callInsn = interpreter.superCallInsn;
        final List<TraceValue> callArgs = interpreter.superCallArgs;
        if (callInsn == null || callArgs == null) {
            return null;
        }
        assertValidSuperCallOwner(data, callInsn.owner);

        return new SuperCallSite(callInsn.owner, callInsn.desc, callArgs);
    }

    /**
     * Assert the given call owner is either {@code data}'s own declaring class (a {@code this()} call) or its
     * direct superclass (a {@code super()} call). This is the same validation {@link #hydrate0} performs later in
     * the pipeline once the target constructor is resolved.
     *
     * @param data The constructor the call was found in.
     * @param owner The owner of the located call, as found on the {@code INVOKESPECIAL} instruction.
     */
    private static void assertValidSuperCallOwner(
        final @NotNull AsmConstructorData data,
        final @NotNull String owner
    ) throws IOException {
        final ClassData thisClass = data.parentClass();
        final String normalizedOwner = HypoModelUtil.normalizedClassName(owner);
        if (thisClass.name().equals(normalizedOwner)) {
            return;
        }

        final ClassData superClass = thisClass.superClass();
        if (superClass != null && superClass.name().equals(normalizedOwner)) {
            return;
        }

        throw new IllegalStateException("super()/this() call owner is not this class or its superclass: " + owner);
    }

    private static int @NotNull [] buildParamIndexMapping(final @NotNull MethodData data) throws IOException {
        final List<? extends @NotNull TypeDescriptor> targetParams = data.params();
        final int[] outputParamIndices = new int[targetParams.size()];
        int currentIndex = 0;
        int currentTargetIndex = 1; // `this` is 0

        if (data.parentClass().outerClass() != null && !data.parentClass().isStaticInnerClass()) {
            currentTargetIndex++; // index 1 is the outer class
        }

        for (final TypeDescriptor paramType : targetParams) {
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
// Below are the classes which make up the Analyzer-based model for the super call.
//

/**
 * Model for {@link SuperConstructorHydrator}.
 *
 * <p>A 4-state trace of what a value observed during analysis is, relative to the calling constructor's own
 * parameters.
 * <ul>
 *     <li>{@link Kind#NONE}: not traceable to a parameter at all</li>
 *     <li>{@link Kind#UNINITIALIZED_THIS}: the constructor's own not-yet-initialized {@code this}</li>
 *     <li>{@link Kind#PARAM}: a direct untransformed reference to one of the constructor's own parameters</li>
 *     <li>{@link Kind#WRAPPED}: the result of exactly one method call applied directly to a {@link Kind#PARAM} value</li>
 * </ul>
 */
final class TraceValue implements Value {

    /**
     * Model for {@link SuperConstructorHydrator}.
     *
     * <p>The kind of a {@link TraceValue}, indicating what a value observed during analysis traces back to,
     * relative to the calling constructor's own parameters or uninitialized {@code this}.
     */
    enum Kind {
        /**
         * Not traceable to a parameter at all.
         */
        NONE,
        /**
         * The constructor's own not-yet-initialized {@code this}.
         */
        UNINITIALIZED_THIS,
        /**
         * A direct untransformed reference to one of the constructor's own parameters.
         */
        PARAM,
        /**
         * The result of exactly one method call applied directly to a {@link #PARAM} value.
         */
        WRAPPED,
    }

    /**
     * Model for {@link SuperConstructorHydrator}.
     */
    final @NotNull Kind kind;
    /**
     * Model for {@link SuperConstructorHydrator}.
     *
     * <p>The calling constructor's LVT slot this value traces back to. Only meaningful for {@link Kind#PARAM} and
     * {@link Kind#WRAPPED}; {@code -1} otherwise.
     */
    final int lvtIndex;
    private final int size;

    private TraceValue(final @NotNull Kind kind, final int lvtIndex, final int size) {
        this.kind = kind;
        this.lvtIndex = lvtIndex;
        this.size = size;
    }

    /**
     * Create a {@link Kind#NONE} trace value.
     * @param size The stack size.
     * @return The new {@link TraceValue}
     */
    static @NotNull TraceValue none(final int size) {
        return new TraceValue(Kind.NONE, -1, size);
    }

    /**
     * Create a {@link Kind#UNINITIALIZED_THIS} trace value.
     * @param size The stack size.
     * @return The new {@link TraceValue}
     */
    static @NotNull TraceValue uninitializedThis(final int size) {
        return new TraceValue(Kind.UNINITIALIZED_THIS, -1, size);
    }

    /**
     * Create a {@link Kind#PARAM} trace value.
     * @param lvtIndex The LVT index.
     * @param size The stack size.
     * @return The new {@link TraceValue}
     */
    static @NotNull TraceValue param(final int lvtIndex, final int size) {
        return new TraceValue(Kind.PARAM, lvtIndex, size);
    }

    /**
     * Create a {@link Kind#WRAPPED} trace value.
     * @param lvtIndex The LVT index.
     * @param size The stack size.
     * @return The new {@link TraceValue}
     */
    static @NotNull TraceValue wrapped(final int lvtIndex, final int size) {
        return new TraceValue(Kind.WRAPPED, lvtIndex, size);
    }

    @Override
    public int getSize() {
        return this.size;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof final TraceValue that)) {
            return false;
        }
        return this.kind == that.kind && this.lvtIndex == that.lvtIndex;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.kind, this.lvtIndex);
    }
}

/**
 * Model for {@link SuperConstructorHydrator}.
 *
 * <p>An {@link Interpreter} which computes a {@link TraceValue} for each value in a constructor, and records the
 * real {@code super()}/{@code this()} call (the sole {@code INVOKESPECIAL <init>} instruction whose receiver is the
 * constructor's own not-yet-initialized {@code this}) as a side effect once {@link Analyzer#analyze} finds it.
 *
 * <p>{@link #newOperation}, {@link #binaryOperation}, {@link #ternaryOperation}, and {@link #naryOperation} only
 * need the <i>size</i> of the value a real {@link BasicInterpreter} would compute for the same instruction, since
 * (aside from the {@code Kind.WRAPPED} case {@link #naryOperation} computes itself) none of those operations can
 * ever produce a value directly traceable back to a parameter. {@link #unaryOperation} can map to a value for numeric
 * conversion or {@code checkcast} applied directly to a traced value.
 */
final class SuperCallInterpreter extends Interpreter<TraceValue> {

    private static final BasicValue PLACEHOLDER = BasicValue.INT_VALUE;

    private final @NotNull BasicInterpreter basic = new BasicInterpreter();

    /**
     * The super call instruction.
     */
    /* package */ @Nullable MethodInsnNode superCallInsn;
    /**
     * Traces for the super call instruction arguments.
     */
    /* package */ @Nullable List<TraceValue> superCallArgs;

    /**
     * Create a new instance of this interpreter.
     */
    SuperCallInterpreter() {
        super(Opcodes.ASM9);
    }

    private static @Nullable TraceValue wrap(final @Nullable BasicValue basicValue) {
        return basicValue == null ? null : TraceValue.none(basicValue.getSize());
    }

    @Override
    public @Nullable TraceValue newValue(final @Nullable Type type) {
        return wrap(this.basic.newValue(type));
    }

    @Override
    public @NotNull TraceValue newParameterValue(
        final boolean isInstanceMethod,
        final int local,
        final @NotNull Type type
    ) {
        final BasicValue basicValue = this.basic.newParameterValue(isInstanceMethod, local, type);
        final int size = basicValue != null ? basicValue.getSize() : 1;
        if (isInstanceMethod && local == 0) {
            return TraceValue.uninitializedThis(size);
        }
        return TraceValue.param(local, size);
    }

    @Override
    public @Nullable TraceValue newOperation(final @NotNull AbstractInsnNode insn) throws AnalyzerException {
        return wrap(this.basic.newOperation(insn));
    }

    @Override
    public @NotNull TraceValue copyOperation(final @NotNull AbstractInsnNode insn, final @NotNull TraceValue value) {
        return value;
    }

    @Override
    public @Nullable TraceValue unaryOperation(
        final @NotNull AbstractInsnNode insn,
        final @NotNull TraceValue value
    ) throws AnalyzerException {
        final BasicValue basicResult = this.basic.unaryOperation(insn, PLACEHOLDER);
        if (basicResult == null) {
            return null;
        }
        final int size = basicResult.getSize();

        // Numeric conversions, checkcast, and instanceof are identity pass-throughs for tracing purposes, so
        // `super((int) someLongParam)` still traces back to `someLongParam`.
        //noinspection EnhancedSwitchMigration
        switch (insn.getOpcode()) {
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
                return switch (value.kind) {
                    case PARAM -> TraceValue.param(value.lvtIndex, size);
                    case WRAPPED -> TraceValue.wrapped(value.lvtIndex, size);
                    case UNINITIALIZED_THIS -> TraceValue.uninitializedThis(size);
                    case NONE -> TraceValue.none(size);
                };
            default:
                return TraceValue.none(size);
        }
    }

    @Override
    public @Nullable TraceValue binaryOperation(
        final @NotNull AbstractInsnNode insn,
        final @NotNull TraceValue value1,
        final @NotNull TraceValue value2
    ) throws AnalyzerException {
        return wrap(this.basic.binaryOperation(insn, PLACEHOLDER, PLACEHOLDER));
    }

    @Override
    public @Nullable TraceValue ternaryOperation(
        final @NotNull AbstractInsnNode insn,
        final @NotNull TraceValue value1,
        final @NotNull TraceValue value2,
        final @NotNull TraceValue value3
    ) throws AnalyzerException {
        return wrap(this.basic.ternaryOperation(insn, PLACEHOLDER, PLACEHOLDER, PLACEHOLDER));
    }

    @Override
    public @Nullable TraceValue naryOperation(
        final @NotNull AbstractInsnNode insn,
        final @NotNull List<? extends TraceValue> values
    ) throws AnalyzerException {
        final int opcode = insn.getOpcode();

        if (opcode == Opcodes.INVOKESPECIAL) {
            final MethodInsnNode methodInsn = (MethodInsnNode) insn;
            // Re-recording args on every visit to the *same* instruction (rather than only the first)
            // matters: Analyzer's worklist can execute this instruction once with a not-yet-merged
            // input frame (e.g. before both sides of a preceding branch have reached it) and again
            // later with the converged one. Only a genuinely different qualifying instruction is
            // rejected once superCallInsn is locked onto something else.
            if (methodInsn.name.equals("<init>")
                && !values.isEmpty()
                && values.getFirst().kind == TraceValue.Kind.UNINITIALIZED_THIS
                && (this.superCallInsn == null || this.superCallInsn == methodInsn)) {
                this.superCallInsn = methodInsn;
                this.superCallArgs = List.copyOf(values.subList(1, values.size()));
            }
        }

        final BasicValue basicResult = this.basic.naryOperation(insn, Collections.nCopies(values.size(), PLACEHOLDER));
        if (basicResult == null) {
            // void return - Frame never pushes this result either way
            return null;
        }
        final int size = basicResult.getSize();

        final boolean hasReceiver = opcode != Opcodes.INVOKESTATIC
            && opcode != Opcodes.INVOKEDYNAMIC
            && opcode != Opcodes.MULTIANEWARRAY;
        final TraceValue receiver = hasReceiver && !values.isEmpty() ? values.getFirst() : null;
        final List<? extends TraceValue> args = hasReceiver && !values.isEmpty()
            ? values.subList(1, values.size())
            : values;

        if (receiver != null && receiver.kind == TraceValue.Kind.PARAM) {
            return TraceValue.wrapped(receiver.lvtIndex, size);
        }
        if (args.size() == 1 && args.getFirst().kind == TraceValue.Kind.PARAM) {
            return TraceValue.wrapped(args.getFirst().lvtIndex, size);
        }
        return TraceValue.none(size);
    }

    @Override
    public void returnOperation(
        final @NotNull AbstractInsnNode insn,
        final @NotNull TraceValue value,
        final @NotNull TraceValue expected
    ) {}

    @Override
    public @NotNull TraceValue merge(final @NotNull TraceValue value1, final @NotNull TraceValue value2) {
        if (value1.equals(value2)) {
            return value1;
        }
        return TraceValue.none(value1.getSize());
    }
}

/**
 * Model for {@link SuperConstructorHydrator}.
 *
 * <p>The located {@code super()}/{@code this()} call: its owner and descriptor (as found on the
 * {@code INVOKESPECIAL} instruction), and a {@link TraceValue} per call argument, in call-argument order.
 *
 * @param owner The declaring class of the called constructor.
 * @param desc The descriptor of the called constructor.
 * @param args A {@link TraceValue} per call argument, in call-argument order.
 */
record SuperCallSite(@NotNull String owner, @NotNull String desc, @NotNull List<TraceValue> args) {}
