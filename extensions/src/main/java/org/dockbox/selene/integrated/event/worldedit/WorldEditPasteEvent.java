/*
 *  Copyright (C) 2020 Guus Lieben
 *
 *  This framework is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation, either version 2.1 of the
 *  License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 *  the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this library. If not, see {@literal<http://www.gnu.org/licenses/>}.
 */

package org.dockbox.selene.integrated.event.worldedit;

import com.boydti.fawe.object.FawePlayer;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;

import org.dockbox.selene.core.events.AbstractCancellableEvent;
import org.dockbox.selene.core.objects.optional.Exceptional;
import org.dockbox.selene.core.objects.user.Player;

public class WorldEditPasteEvent extends AbstractCancellableEvent {

    private final FawePlayer<?> fawePlayer;
    private final Player player;

    public WorldEditPasteEvent(FawePlayer<?> fawePlayer, Player player) {
        this.fawePlayer = fawePlayer;
        this.player = player;
    }

    public Region getSelection() {
        return this.fawePlayer.getSelection();
    }

    public FawePlayer<?> getFawePlayer() {
        return this.fawePlayer;
    }

    public Exceptional<ClipboardHolder> getClipboard() {
        return Exceptional.of(() -> this.fawePlayer.getSession().getClipboard());
    }

    public Player getPlayer() {
        return this.player;
    }
}
