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

package org.dockbox.hartshorn.util.introspect.view.wildcard;

import org.dockbox.hartshorn.util.introspect.TypeParametersIntrospector;
import org.dockbox.hartshorn.util.introspect.view.TypeView;
import org.dockbox.hartshorn.util.option.Option;

import java.util.Collections;
import java.util.List;

public class WildcardTypeParametersIntrospector implements TypeParametersIntrospector {

    @Override
    public List<TypeView<?>> from(final Class<?> fromInterface) {
        return Collections.emptyList();
    }

    @Override
    public Option<TypeView<?>> at(final int index) {
        return Option.empty();
    }

    @Override
    public List<TypeView<?>> all() {
        return Collections.emptyList();
    }

    @Override
    public int count() {
        return 0;
    }
}