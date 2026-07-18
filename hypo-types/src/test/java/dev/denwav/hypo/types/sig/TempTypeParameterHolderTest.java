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

import dev.denwav.hypo.types.HierarchyTypeVariableBinder;
import dev.denwav.hypo.types.sig.param.TypeParameter;
import dev.denwav.hypo.types.sig.param.TypeVariable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("[types] TempTypeParameterHolder Tests")
class TempTypeParameterHolderTest {

    @Test
    @DisplayName("Verify TempTypeParameterHolder adds TypeParameters and resolveAll triggers eager getDefinition")
    void testTempHolderAndResolveAll() {
        final TempTypeParameterHolder holder = new TempTypeParameterHolder();
        assertTrue(holder.getTypeParameters().isEmpty());

        final TypeParameter paramT = TypeParameter.of("T");
        holder.add(paramT);

        // Create a lazy TypeVariable pointing to holder
        final HierarchyTypeVariableBinder binder = HierarchyTypeVariableBinder.of(holder);
        final TypeVariable varT = TypeVariable.ofLazy("T", () -> binder.bindingFor("T"));

        final TypeParameter paramU = TypeParameter.of("U", varT);
        holder.add(paramU);

        assertEquals(2, holder.getTypeParameters().size());

        // Call resolveAll and verify getDefinition() was triggered
        holder.resolveAll();
        assertEquals(paramT, varT.getDefinition());
    }

    @Test
    @DisplayName("Verify resolve(TypeRepresentable...) triggers eager getDefinition without re-traversing held parameters")
    void testResolveTypeRepresentables() {
        final TempTypeParameterHolder holder = new TempTypeParameterHolder();
        final TypeParameter paramT = TypeParameter.of("T");
        holder.add(paramT);

        final HierarchyTypeVariableBinder binder = HierarchyTypeVariableBinder.of(holder);
        final TypeVariable varT = TypeVariable.ofLazy("T", () -> binder.bindingFor("T"));

        final TypeParameter paramU = TypeParameter.of("U", varT);

        // Call resolve on paramU directly (which contains varT) without adding paramU to holder
        holder.resolve(paramU);
        assertEquals(paramT, varT.getDefinition());
    }
}
