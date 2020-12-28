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

package org.dockbox.selene.core.impl.command.registration;

import org.dockbox.selene.core.util.SeleneUtils;
import org.dockbox.selene.core.annotations.command.Command;
import org.dockbox.selene.core.command.context.CommandContext;
import org.dockbox.selene.core.command.source.CommandSource;
import org.dockbox.selene.core.exceptions.IllegalSourceException;
import org.dockbox.selene.core.i18n.entry.IntegratedResource;
import org.dockbox.selene.core.objects.Console;
import org.dockbox.selene.core.objects.Exceptional;
import org.dockbox.selene.core.objects.player.Player;
import org.dockbox.selene.core.objects.targets.Identifiable;
import org.dockbox.selene.core.server.Selene;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class MethodCommandContext extends AbstractRegistrationContext {

    private final Method method;

    public MethodCommandContext(Command command, Method method) {
        super(command);
        this.method = method;
    }

    public Method getMethod() {
        return this.method;
    }

    public Class<?> getDeclaringClass() {
        return this.getMethod().getDeclaringClass();
    }

    @Override
    public Exceptional<IntegratedResource> call(CommandSource source, CommandContext context) {
        try {
            List<Object> args = this.prepareArguments(source, context);
            Object instance = this.prepareInstance();
            Command command = this.method.getAnnotation(Command.class);
            if (0 < command.cooldownDuration() && source instanceof Identifiable) {
                String registrationId = this.getRegistrationId((Identifiable<?>) source, context);
                SeleneUtils.OTHER.cooldown(registrationId, command.cooldownDuration(), command.cooldownUnit());
            }
            this.method.invoke(instance, SeleneUtils.OTHER.toArray(Object.class, args));
            return Exceptional.empty();
        } catch (IllegalSourceException e) {
            return Exceptional.of(e);
        } catch (IllegalAccessException | InvocationTargetException e) {
            Selene.except("Failed to invoke command", e.getCause());
            return Exceptional.of(e);
        } catch (Throwable e) {
            Selene.except("Failed to invoke command", e);
            return Exceptional.of(e);
        }
    }

    private List<Object> prepareArguments(CommandSource source, CommandContext context) {
        List<Object> finalArgs = SeleneUtils.COLLECTION.emptyList();

        for (Class<?> parameterType : this.getMethod().getParameterTypes()) {
            if (SeleneUtils.REFLECTION.isEitherAssignableFrom(CommandSource.class, parameterType)) {
                if (parameterType.equals(Player.class)) {
                    if (source instanceof Player) finalArgs.add(source);
                    else throw new IllegalSourceException("Command can only be ran by players");
                } else if (parameterType.equals(Console.class)) {
                    if (source instanceof Console) finalArgs.add(source);
                    else throw new IllegalSourceException("Command can only be ran by the console");
                } else finalArgs.add(source);
            } else if (SeleneUtils.REFLECTION.isEitherAssignableFrom(CommandContext.class, parameterType)) {
                finalArgs.add(context);
            } else {
                throw new IllegalStateException("Method requested parameter type '" + parameterType.getSimpleName() + "' which is not provided");
            }
        }
        return finalArgs;
    }

    private Object prepareInstance() {
        Object instance;
        if (this.getDeclaringClass().equals(Selene.class) || SeleneUtils.REFLECTION.isAssignableFrom(Selene.class, this.getDeclaringClass())) {
            instance = Selene.getServer();
        } else {
            instance = Selene.getInstance(this.getDeclaringClass());
        }
        return instance;
    }

    private boolean isSenderInCooldown(CommandSource sender, CommandContext ctx) {
        Command command = this.getMethod().getAnnotation(Command.class);
        if (0 >= command.cooldownDuration()) return false;
        if (sender instanceof Identifiable) {
            String registrationId = this.getRegistrationId((Identifiable<?>) sender, ctx);
            return SeleneUtils.OTHER.isInCooldown(registrationId);
        }
        return false;
    }

    public String getLocation() {
        return this.getDeclaringClass().getCanonicalName() + "." + this.getMethod().getName();
    }
}
