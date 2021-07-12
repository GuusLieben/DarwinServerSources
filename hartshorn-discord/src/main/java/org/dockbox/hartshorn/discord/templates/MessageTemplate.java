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

package org.dockbox.hartshorn.discord.templates;

import net.dv8tion.jda.api.entities.Message;

import org.dockbox.hartshorn.api.Hartshorn;
import org.dockbox.hartshorn.api.i18n.text.Text;

public interface MessageTemplate extends Template<Message> {

    static MessageTemplate create(Text content) {
        MessageTemplate template = create();
        template.content(content);
        return template;
    }

    static MessageTemplate create() {
        return Hartshorn.context().get(MessageTemplate.class);
    }

    Text content();

    MessageTemplate content(Text content);

    MessageTemplate copy();

    MessageTemplate resetPlaceholders();
}
