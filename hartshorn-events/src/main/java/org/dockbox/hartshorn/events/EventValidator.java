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

package org.dockbox.hartshorn.events;

import org.dockbox.hartshorn.application.context.ApplicationContext;
import org.dockbox.hartshorn.application.lifecycle.ApplicationState.Started;
import org.dockbox.hartshorn.application.lifecycle.LifecycleObserver;
import org.dockbox.hartshorn.component.Service;
import org.dockbox.hartshorn.component.condition.RequiresActivator;
import org.dockbox.hartshorn.events.annotations.Posting;
import org.dockbox.hartshorn.events.annotations.UseEvents;
import org.dockbox.hartshorn.events.parents.Event;
import org.dockbox.hartshorn.util.CollectionUtilities;
import org.dockbox.hartshorn.util.introspect.view.TypeView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Service
@RequiresActivator(UseEvents.class)
public class EventValidator implements LifecycleObserver {

    @Override
    public void onStarted(final ApplicationContext applicationContext) {
        if (applicationContext.hasActivator(UseEvents.class)) {
            new EngineChangedState<Started>() {
            }.with(applicationContext).post();
        }

        final List<TypeView<? extends Event>> allEvents = applicationContext.environment().children(Event.class)
                .stream()
                .filter(type -> !type.modifiers().isAbstract())
                // Anonymous classes indicate the event carries type parameters when posted (e.g. EngineChangedState<State>)
                // These are only created when the event is posted, so they can be ignored here, as they are not explicit
                // definitions.
                .filter(type -> !type.isAnonymous())
                .toList();
        final List<TypeView<? extends Event>> postedEvents = new ArrayList<>();

        for (final TypeView<?> bridge : applicationContext.environment().types(Posting.class)) {
            final Posting posting = bridge.annotations().get(Posting.class).get();
            postedEvents.addAll(Arrays.stream(posting.value())
                    .map(applicationContext.environment()::introspect)
                    .toList());
        }

        final Set<TypeView<? extends Event>> difference = CollectionUtilities.difference(allEvents, postedEvents);

        if (!difference.isEmpty()) {
            final StringBuilder message = new StringBuilder(difference.size() + " events are not handled by any event bridge!");

            if (!applicationContext.environment().isCI()) {
                for (final TypeView<? extends Event> event : difference) {
                    message.append("\n\t- ").append(event.name());
                }
            }
            applicationContext.log().warn(message.toString());
        }
    }
}
