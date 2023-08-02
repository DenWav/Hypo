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

package dev.denwav.hypo.asm;

import dev.denwav.hypo.model.data.ClassData;
import dev.denwav.hypo.model.data.ConstructorData;
import dev.denwav.hypo.model.data.MethodData;
import java.util.Collections;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.tree.MethodNode;

/**
 * Implementation of {@link ConstructorData} based on {@code asm}'s {@link MethodNode}.
 *
 * @see AsmMethodData
 */
public class AsmConstructorData extends AsmMethodData implements ConstructorData {

    /**
     * Construct a new instance of {@link AsmConstructorData} using the given {@link MethodNode}. The given {@code node}
     * is understood to be a constructor, passing a {@link MethodNode} which isn't a constructor is undefined. Regular
     * methods should use {@link AsmMethodData}.
     *
     * @param parentClass The {@link ClassData} which declares this constructor.
     * @param node The {@link MethodNode} to use for this {@link AsmConstructorData}.
     */
    public AsmConstructorData(final @NotNull AsmClassData parentClass, final @NotNull MethodNode node) {
        super(parentClass, node);
    }

    @Override
    public @NotNull String name() {
        return "<init>";
    }

    @Override
    public boolean isConstructor() {
        return true;
    }

    @Override
    public boolean isStatic() {
        return false;
    }

    @Override
    public boolean isAbstract() {
        return false;
    }

    @Override
    public boolean isFinal() {
        return false;
    }

    @Override
    public boolean isBridge() {
        return false;
    }

    @Override
    public boolean isNative() {
        return false;
    }

    @Override
    public @Nullable MethodData superMethod() {
        return null;
    }

    @Override
    public void setSuperMethod(final @Nullable MethodData superMethod) {}

    @Override
    public @NotNull Set<MethodData> childMethods() {
        return Collections.emptySet();
    }
}
