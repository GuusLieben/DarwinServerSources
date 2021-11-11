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

package org.dockbox.hartshorn.events.parents;

import org.dockbox.hartshorn.events.EventBus;
import org.jetbrains.annotations.NotNull;

/**
 * Low level event type which can be cancelled, usually this cancellable state is respected by the
 * underlying implementation.
 */
public interface Cancellable extends Event {

    /**
     * Indicates whether the event is currently cancelled
     *
     * @return The cancelled state
     */
    boolean cancelled();

    /**
     * Sets the cancelled state of the event
     *
     * @param cancelled
     *         Whether the event should be cancelled
     */
    Cancellable cancelled(boolean cancelled);

    @Override
    @NotNull
    default Cancellable post() {
        this.applicationContext().get(EventBus.class).post(this);
        return this;
    }
}
