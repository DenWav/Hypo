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

package dev.denwav.hypo.types.sig;

import dev.denwav.hypo.types.TypeRepresentable;
import dev.denwav.hypo.types.intern.Intern;
import dev.denwav.hypo.types.sig.param.TypeParameter;
import dev.denwav.hypo.types.sig.param.TypeVariable;
import dev.denwav.hypo.types.visitor.TraversingTypeVisitor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Internal mutable {@link TypeParameterHolder} used during type signature binding to hold newly created
 * {@link TypeParameter} instances before intermediate signatures and root definitions are finalized.
 */
@ApiStatus.Internal
public final class TempTypeParameterHolder implements TypeParameterHolder {

    private static final TraversingTypeVisitor RESOLVER = new TraversingTypeVisitor() {
        @Override
        public boolean visit(final @NotNull TypeVariable var) {
            var.getDefinition();
            return TraversingTypeVisitor.super.visit(var);
        }
    };

    private final List<TypeParameter> parameters = new ArrayList<>();

    /**
     * Create a new {@link TempTypeParameterHolder}.
     */
    public TempTypeParameterHolder() {}

    /**
     * Add a {@link TypeParameter} to this holder.
     *
     * @param parameter The {@link TypeParameter} to add.
     */
    public void add(final @NotNull TypeParameter parameter) {
        this.parameters.add(parameter);
    }

    /**
     * Eagerly resolve all {@link TypeVariable} instances contained in all {@link TypeParameter} objects inside
     * this holder by traversing them and calling {@link TypeVariable#getDefinition()}.
     */
    public void resolveAll() {
        for (final TypeParameter param : this.parameters) {
            RESOLVER.accept(param);
        }
        this.parameters.replaceAll(Intern::intern);
    }

    /**
     * Eagerly resolve all {@link TypeVariable} instances in the given type structures without re-traversing
     * the parameters already inside this holder.
     *
     * @param types The {@link TypeRepresentable} objects to visit and eagerly resolve.
     */
    public void resolve(final TypeRepresentable @NotNull ... types) {
        for (final TypeRepresentable type : types) {
            if (type != null) {
                RESOLVER.accept(type);
            }
        }
    }

    @Override
    public @NotNull List<? extends TypeParameter> getTypeParameters() {
        return Collections.unmodifiableList(this.parameters);
    }
}
