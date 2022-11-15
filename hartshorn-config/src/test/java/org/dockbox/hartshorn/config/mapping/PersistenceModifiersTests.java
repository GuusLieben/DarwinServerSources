/*
 * Copyright 2019-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dockbox.hartshorn.config.mapping;

import org.dockbox.hartshorn.application.context.ApplicationContext;
import org.dockbox.hartshorn.config.JsonInclusionRule;
import org.dockbox.hartshorn.config.ObjectMapper;
import org.dockbox.hartshorn.config.annotations.UseSerialization;
import org.dockbox.hartshorn.testsuite.HartshornTest;
import org.dockbox.hartshorn.util.StringUtilities;
import org.dockbox.hartshorn.util.option.Option;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import jakarta.inject.Inject;

@HartshornTest(includeBasePackages = false)
@UseSerialization
public class PersistenceModifiersTests {

    @Inject
    private ApplicationContext applicationContext;

    @Test
    void testSkipEmptyKeepsNonEmpty() {
        final ObjectMapper mapper = this.applicationContext.get(ObjectMapper.class).skipBehavior(JsonInclusionRule.SKIP_EMPTY);
        final ModifierElement element = new ModifierElement(List.of("sample", "other"));
        final Option<String> out = mapper.write(element);

        Assertions.assertTrue(out.present());
        Assertions.assertEquals("{\"names\":[\"sample\",\"other\"]}", StringUtilities.strip(out.get()));
    }

    @Test
    void testSkipEmptySkipsEmpty() {
        final ObjectMapper mapper = this.applicationContext.get(ObjectMapper.class).skipBehavior(JsonInclusionRule.SKIP_EMPTY);
        final ModifierElement element = new ModifierElement(List.of());
        final Option<String> out = mapper.write(element);

        Assertions.assertTrue(out.present());
        Assertions.assertEquals("{}", StringUtilities.strip(out.get()));
    }
}