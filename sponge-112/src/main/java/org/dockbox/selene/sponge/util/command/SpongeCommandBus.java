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

package org.dockbox.selene.sponge.util.command;

import com.google.common.collect.Multimap;

import org.dockbox.selene.core.SeleneUtils;
import org.dockbox.selene.core.command.context.CommandValue;
import org.dockbox.selene.core.command.context.CommandValue.Argument;
import org.dockbox.selene.core.command.context.CommandValue.Flag;
import org.dockbox.selene.core.command.source.CommandSource;
import org.dockbox.selene.core.events.chat.CommandEvent;
import org.dockbox.selene.core.events.parents.Cancellable;
import org.dockbox.selene.core.i18n.entry.IntegratedResource;
import org.dockbox.selene.core.impl.command.context.SimpleCommandContext;
import org.dockbox.selene.core.impl.command.DefaultCommandBus;
import org.dockbox.selene.core.impl.command.registration.AbstractRegistrationContext;
import org.dockbox.selene.core.impl.command.registration.CommandInheritanceContext;
import org.dockbox.selene.core.impl.command.registration.MethodCommandContext;
import org.dockbox.selene.core.impl.command.values.AbstractArgumentElement;
import org.dockbox.selene.core.impl.command.values.AbstractArgumentValue;
import org.dockbox.selene.core.impl.command.values.AbstractFlagCollection;
import org.dockbox.selene.core.objects.Exceptional;
import org.dockbox.selene.core.objects.targets.Locatable;
import org.dockbox.selene.core.server.Selene;
import org.dockbox.selene.sponge.util.SpongeConversionUtil;
import org.dockbox.selene.sponge.util.command.values.SpongeArgumentElement;
import org.dockbox.selene.sponge.util.command.values.SpongeArgumentValue;
import org.dockbox.selene.sponge.util.command.values.SpongeFlagCollection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;

import java.lang.reflect.Field;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SpongeCommandBus extends DefaultCommandBus {

    @Override
    protected AbstractArgumentElement<?> wrapElements(List<AbstractArgumentElement<?>> elements) {
        return new SpongeArgumentElement(elements.stream()
                .filter(element -> element instanceof SpongeArgumentElement)
                .map(element -> (SpongeArgumentElement) element)
                .toArray(SpongeArgumentElement[]::new));
    }

    @Override
    protected AbstractArgumentValue<?> generateGenericArgument(String type, String permission, String key) {
        try {
            return new SpongeArgumentValue(type, permission, key);
        } catch (IllegalArgumentException e) {
            return new SpongeArgumentValue(DefaultCommandBus.DEFAULT_TYPE, permission, key);
        }
    }

    @Override
    protected AbstractFlagCollection<?> createEmptyFlagCollection() {
        return new SpongeFlagCollection();
    }

    @Override
    public void apply() {
        this.getRegistrations().forEach((alias, abstractCommand) -> {
            CommandSpec spec = null;
            if (abstractCommand instanceof MethodCommandContext) {
                MethodCommandContext methodContext = (MethodCommandContext) abstractCommand;
                if (!methodContext.getCommand().inherit()) {
                    spec = this.buildContextExecutor(methodContext, alias).build();
                } else {
                    Selene.log().error("Found direct method registration of inherited command! " + methodContext.getLocation());
                }

            } else if (abstractCommand instanceof CommandInheritanceContext) {
                CommandInheritanceContext inheritanceContext = (CommandInheritanceContext) abstractCommand;
                spec = this.buildInheritedContextExecutor(inheritanceContext, alias);

            } else {
                Selene.log().error("Found unknown context type [" + abstractCommand.getClass().getCanonicalName() + "]");
            }

            if (null != spec)
                Sponge.getCommandManager().register(Selene.getServer(), spec, alias);
            else
                Selene.log().warn("Could not generate executor for '" + alias + "'. Any errors logged above.");
        });
    }

    private CommandSpec buildInheritedContextExecutor(CommandInheritanceContext context, String alias) {
        CommandSpec.Builder builder = this.buildContextExecutor(context, alias);
        context.getInheritedCommands().forEach(inheritedContext -> {
            inheritedContext.getAliases().forEach(inheritedAlias -> {
                builder.child(
                        this.buildContextExecutor(inheritedContext, inheritedAlias).build(),
                        inheritedAlias
                );
            });
        });
        return builder.build();
    }

    private CommandSpec.Builder buildContextExecutor(AbstractRegistrationContext context, String alias) {
        CommandSpec.Builder builder = CommandSpec.builder();

        List<AbstractArgumentElement<?>> elements = super.parseArgumentElements(context.getCommand().usage());
        List<CommandElement> commandElements = elements.stream()
                .filter(element -> element instanceof SpongeArgumentElement)
                .map(element -> (SpongeArgumentElement) element)
                .map(AbstractArgumentElement::getReference)
                .collect(Collectors.toList());
        if (!elements.isEmpty())
            builder.arguments(commandElements.toArray(new CommandElement[0]));

        builder.permission(context.getCommand().permission());
        builder.executor(this.buildExecutor(context, alias));

        return builder;
    }

    private CommandExecutor buildExecutor(AbstractRegistrationContext registrationContext, String command) {
        return (src, args) -> {
            CommandSource sender = SpongeConversionUtil
                    .fromSponge(src)
                    .orElseThrow(() ->
                            new IllegalArgumentException("Command sender is not a console or a player, did a plugin call me?"));

            SimpleCommandContext ctx = this.createCommandContext(args, sender, command);
            Exceptional<IntegratedResource> response = this.callCommandWithEvents(
                    sender, ctx, command, registrationContext);

            if (response.errorPresent())
                sender.send(IntegratedResource.UNKNOWN_ERROR.format(response.getError().getMessage()));

            return CommandResult.success();
        };
    }

    private Exceptional<IntegratedResource> callCommandWithEvents(
            CommandSource sender,
            SimpleCommandContext context,
            String command,
            AbstractRegistrationContext registrationContext
    ) {
        Cancellable ceb = new CommandEvent.Before(sender, context).post();

        if (!ceb.isCancelled()) {
            Exceptional<IntegratedResource> response = registrationContext.call(sender, context);
            new CommandEvent.After(sender, context).post();
            return response;
        }
        return Exceptional.empty();
    }

    @SuppressWarnings("unchecked")
    private SimpleCommandContext createCommandContext(CommandContext ctx,
                                                      @NotNull CommandSource sender,
                                                      @Nullable String command) {
        Multimap<String, Object> parsedArgs;
        try {
            Field parsedArgsF = ctx.getClass().getDeclaredField("parsedArgs");
            if (!parsedArgsF.isAccessible()) parsedArgsF.setAccessible(true);
            parsedArgs = (Multimap<String, Object>) parsedArgsF.get(ctx);
        } catch (IllegalAccessException | ClassCastException | NoSuchFieldException e) {
            Selene.getServer().except("Could not load parsed arguments from Sponge command context", e);
            return SimpleCommandContext.EMPTY;
        }

        List<CommandValue.Argument<?>> arguments = SeleneUtils.emptyList();
        List<CommandValue.Flag<?>> flags = SeleneUtils.emptyList();

        assert null != command : "Context carrier command was null";
        parsedArgs.asMap().forEach((s, o) -> o.forEach(obj -> {
            if (Pattern.compile("-(-?" + s + ")").matcher(command).find())
                flags.add(new Flag<>(this.tryConvertObject(obj), s));
            else arguments.add(new Argument<>(this.tryConvertObject(obj), s));
        }));

        return this.constructCommandContext(sender, arguments, flags, command);
    }

    private Object tryConvertObject(Object obj) {
        Exceptional<?> oo = SpongeConversionUtil.autoDetectFromSponge(obj);
        return oo.isPresent() ? oo.get() : obj; // oo.orElse() cannot be cast due to generic ? type
    }

    @NotNull
    private SimpleCommandContext constructCommandContext(@NotNull CommandSource sender, List<CommandValue.Argument<?>> arguments, List<CommandValue.Flag<?>> flags, String command) {
        SimpleCommandContext seleneContext;
        if (sender instanceof org.dockbox.selene.core.objects.player.Player) {
            org.dockbox.selene.core.objects.location.Location loc = ((Locatable) sender).getLocation();
            org.dockbox.selene.core.objects.location.World world = ((Locatable) sender).getLocation().getWorld();
            seleneContext = new SimpleCommandContext(
                    command,
                    arguments.toArray(new CommandValue.Argument<?>[0]),
                    flags.toArray(new CommandValue.Flag<?>[0]),
                    sender, Exceptional.of(loc), Exceptional.of(world),
                    new String[0]
            );
        } else {
            seleneContext = new SimpleCommandContext(
                    command,
                    arguments.toArray(new CommandValue.Argument<?>[0]),
                    flags.toArray(new CommandValue.Flag<?>[0]),
                    sender, Exceptional.empty(), Exceptional.empty(),
                    new String[0]
            );
        }
        return seleneContext;
    }
}
