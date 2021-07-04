/*
 * Copyright (C) 2020 Guus Lieben
 *
 * This framework is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library. If not, see {@literal<http://www.gnu.org/licenses/>}.
 */

package org.dockbox.hartshorn.persistence.mapping;

import org.dockbox.hartshorn.api.Hartshorn;
import org.dockbox.hartshorn.api.domain.Exceptional;
import org.dockbox.hartshorn.persistence.properties.PersistenceModifier;
import org.dockbox.hartshorn.persistence.properties.PersistenceProperty;
import org.dockbox.hartshorn.test.HartshornRunner;
import org.dockbox.hartshorn.util.HartshornUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(HartshornRunner.class)
public class PersistenceModifiersTests {

    @Test
    void testSkipEmptyKeepsNonEmpty() {
        ObjectMapper mapper = Hartshorn.context().get(ObjectMapper.class, PersistenceProperty.of(PersistenceModifier.SKIP_EMPTY));
        final ModifierElement element = new ModifierElement(HartshornUtils.asList("sample", "other"));
        final Exceptional<String> out = mapper.write(element);

        Assertions.assertTrue(out.present());
        Assertions.assertEquals("{\"names\":[\"sample\",\"other\"]}", HartshornUtils.strip(out.get()));
    }

    @Test
    void testSkipEmptySkipsEmpty() {
        ObjectMapper mapper = Hartshorn.context().get(ObjectMapper.class, PersistenceProperty.of(PersistenceModifier.SKIP_EMPTY));
        final ModifierElement element = new ModifierElement(HartshornUtils.emptyList());
        final Exceptional<String> out = mapper.write(element);

        Assertions.assertTrue(out.present());
        Assertions.assertEquals("{}", HartshornUtils.strip(out.get()));
    }
}