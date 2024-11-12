/*
 * Copyright 2019-2024 the original author or authors.
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

package org.dockbox.hartshorn.hsl.customizer;

import java.util.function.Consumer;

import org.dockbox.hartshorn.hsl.runtime.Phase;

/**
 * A {@link CodeCustomizer} that delegates to a given {@link Consumer} when called. This is primarily
 * used to simplify the creation of customizers that have a known {@link Phase}, so configurers only
 * need to provide the {@link Consumer}.
 *
 * @param phase the phase in which this customizer should be called
 * @param consumer the consumer that should be called when this customizer is called
 *
 * @since 0.7.0
 *
 * @author Guus Lieben
 */
public record ConsumerCodeCustomizer(
        Phase phase,
        Consumer<ScriptContext> consumer
) implements CodeCustomizer {

    @Override
    public void call(ScriptContext context) {
        this.consumer().accept(context);
    }
}
