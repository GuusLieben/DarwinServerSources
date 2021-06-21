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

package org.dockbox.hartshorn.commands.beta.impl;

import org.dockbox.hartshorn.api.Hartshorn;
import org.dockbox.hartshorn.api.domain.Exceptional;
import org.dockbox.hartshorn.api.exceptions.Except;
import org.dockbox.hartshorn.commands.annotations.Command;
import org.dockbox.hartshorn.commands.beta.api.CommandContainerContext;
import org.dockbox.hartshorn.commands.beta.api.CommandExecutor;
import org.dockbox.hartshorn.commands.beta.api.CommandExecutorContext;
import org.dockbox.hartshorn.di.context.DefaultContext;
import org.dockbox.hartshorn.util.HartshornUtils;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import lombok.Getter;

@Getter
public class MethodCommandExecutorContext extends DefaultContext implements CommandExecutorContext {

    private final Method method;
    private final Class<?> type;
    private final List<String> parentAliases;
    private final Command command;
    @Nullable
    private final Command parent;
    private final boolean isChild;

    public MethodCommandExecutorContext(Method method, Class<?> type) {
        if (!method.isAnnotationPresent(Command.class)) throw new IllegalArgumentException("Provided method is not a command handler");
        this.method = method;
        this.type = type;
        this.command = method.getAnnotation(Command.class);

        if (type.isAnnotationPresent(Command.class)) {
            this.parent = type.getAnnotation(Command.class);
            this.isChild = true;
        }
        else {
            this.parent = null;
            this.isChild = false;
        }

        this.add(new SimpleCommandContainerContext(this.command));

        this.parentAliases = HartshornUtils.emptyList();
        if (this.parent != null) {
            this.parentAliases.addAll(HartshornUtils.asList(this.parent.value()));
        }
    }

    @Override
    public CommandExecutor executor() {
        // TODO: Support variable parameters
        return (ctx) -> {
            final Object instance = Hartshorn.context().get(this.getType());
            try {
                this.getMethod().invoke(instance, ctx);
            }
            catch (IllegalAccessException | InvocationTargetException e) {
                Except.handle(e);
            }
        };
    }

    @Override
    public boolean accepts(String command) {
        final CommandContainerContext containerContext = this.container();
        return containerContext.matches(this.strip(command, true));
    }

    @Override
    public String strip(String command, boolean parentOnly) {
        for (String alias : this.parentAliases) {
            if (command.startsWith(alias+' ')) command = command.substring(alias.length()+1);
        }

        if (!parentOnly) {
            for (String alias : this.container().aliases()) {
                if (command.startsWith(alias+' ')) command = command.substring(alias.length()+1);
            }
        }
        return command;
    }

    @Override
    public List<String> aliases() {
        List<String> aliases = HartshornUtils.emptyList();
        for (String parentAlias : this.getParentAliases()) {
            for (String alias : this.aliases()) {
                aliases.add(parentAlias + ' ' + alias);
            }
        }
        return aliases;
    }

    @Override
    public Class<?> parent() {
        return this.type;
    }

    private CommandContainerContext container() {
        final Exceptional<CommandContainerContext> container = this.first(CommandContainerContext.class);
        if (container.absent()) throw new IllegalStateException("Container context was lost!");
        return container.get();
    }
}
