/*
 * Copyright 2019-2023 the original author or authors.
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

package org.dockbox.hartshorn.events.handle;

import org.dockbox.hartshorn.events.parents.Event;
import org.dockbox.hartshorn.util.introspect.view.ParameterView;
import org.dockbox.hartshorn.util.introspect.view.TypeView;
import org.dockbox.hartshorn.util.option.Option;
import org.dockbox.hartshorn.util.introspect.util.ParameterLoaderRule;

public class EventParameterRule implements ParameterLoaderRule<EventParameterLoaderContext> {
    @Override
    public boolean accepts(final ParameterView<?> parameter, final int index, final EventParameterLoaderContext context, final Object... args) {
        final TypeView<Event> typeView = context.applicationContext().environment().introspect(context.event());
        return typeView.isChildOf(parameter.type().type());
    }

    @Override
    public <T> Option<T> load(final ParameterView<T> parameter, final int index, final EventParameterLoaderContext context, final Object... args) {
        return Option.of(parameter.type().cast(context.event()));
    }
}
