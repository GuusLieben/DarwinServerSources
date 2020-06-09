package com.darwinreforged.server.sponge.implementations;

import com.boydti.fawe.object.FawePlayer;
import com.darwinreforged.server.core.DarwinServer;
import com.darwinreforged.server.core.commands.ArgumentTypeValue;
import com.darwinreforged.server.core.commands.CommandBus;
import com.darwinreforged.server.core.commands.context.CommandArgument;
import com.darwinreforged.server.core.commands.context.CommandFlag;
import com.darwinreforged.server.core.math.Vector3d;
import com.darwinreforged.server.core.modules.Module;
import com.darwinreforged.server.core.player.DarwinPlayer;
import com.darwinreforged.server.core.player.DarwinUser;
import com.darwinreforged.server.core.player.PlayerManager;
import com.darwinreforged.server.core.resources.Permissions;
import com.darwinreforged.server.core.tuple.Tuple;
import com.darwinreforged.server.core.types.living.CommandSender;
import com.darwinreforged.server.core.types.living.Console;
import com.darwinreforged.server.core.types.location.DarwinLocation;
import com.darwinreforged.server.core.types.location.DarwinWorld;
import com.google.common.collect.Multimap;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.input.ParserContext;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.CommandFlags;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.Extent;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

@SuppressWarnings("unchecked")
public class SpongeCommandBus extends CommandBus<CommandContext, SpongeArgumentTypeValue> {

    private static final Map<String, List<Tuple<String, CommandSpec>>> childsPerAlias = new HashMap<>();

    @Override
    protected SpongeArgumentTypeValue getArgumentValue(String type, String permission, String key) {
        try {
            return new SpongeArgumentTypeValue(type, permission, key);
        } catch (IllegalArgumentException e) {
            return new SpongeArgumentTypeValue(Arguments.OTHER.toString(), permission, key);
        }
    }

    @Override
    public void registerCommandNoArgs(String command, String permission, CommandRunner runner) {
        Sponge.getCommandManager().register(DarwinServer.getServer(), CommandSpec.builder().permission(permission).executor(buildExecutor(runner, command)).build(), command);
    }

    @Override
    public void registerCommandArgsAndOrChild(String command, String permission, CommandRunner runner) {
        CommandSpec.Builder spec = CommandSpec.builder();
        if (permission != null) spec.permission(permission);
        else spec.permission(Permissions.ADMIN_BYPASS.p());

        String[] parts = command.split(" ");
        String part = parts.length > 1 ? parts[1] : null;
        // Child command
        if (part != null && subcommand.matcher(part).matches()) {
            String arguments = command.substring(command.indexOf(' ') + 1)
                    .replaceFirst(part, "");
            if (arguments.endsWith(" ")) arguments = arguments.substring(0, arguments.length() - 2);

            CommandElement[] elements = parseArguments(arguments);

            elements = Arrays.stream(elements).filter(Objects::nonNull).toArray(CommandElement[]::new);
            if (elements.length > 0) spec.executor(buildExecutor(runner, command)).arguments(elements);
            else spec.executor(buildExecutor(runner, command));

            String alias = command.substring(0, command.indexOf(' '));
            if (part.equals("")) part = "@m";
            List<Tuple<String, CommandSpec>> aliases = childsPerAlias.getOrDefault(alias, new ArrayList<>());
            aliases.add(new Tuple<>(part, spec.build()));
            childsPerAlias.put(alias, aliases);

            // Parent command
        } else if (command.startsWith("*")) {
            String registeredCmd = command.substring(1);
            if (command.contains(" ")) registeredCmd = command.substring(1, command.indexOf(' '));
            if (!REGISTERED_COMMANDS.contains(registeredCmd)) {
                List<Tuple<String, CommandSpec>> childs = childsPerAlias.getOrDefault(registeredCmd, new ArrayList<>());
                childs.forEach(child -> {
                    if (child.getFirst().equals("@m")) {
                        spec.executor(child.getSecond().getExecutor());
                    } else {
                        spec.child(child.getSecond(), child.getFirst());
                    }
                });

                try {
                    Field executorF = spec.getClass().getDeclaredField("executor");
                    executorF.setAccessible(true);
                    if (executorF.get(spec) == null) spec.executor((src, args) -> CommandResult.success());
                } catch (Throwable e) {
                    DarwinServer.error("Could not access executor field", e);
                    spec.executor((src, args) -> CommandResult.success());
                }

                try {
                    Sponge.getCommandManager().register(DarwinServer.getServer(), spec.build(), registeredCmd);
                } catch (IllegalArgumentException e) {
                    DarwinServer.error(e.getMessage(), e);
                }
                REGISTERED_COMMANDS.add(registeredCmd);
            }
            // Single method command
        } else {
            if (!REGISTERED_COMMANDS.contains(command.substring(0, command.indexOf(' ')))) {
                spec.executor(buildExecutor(runner, command)).arguments(parseArguments(command.substring(command.indexOf(' ') + 1)));
                Sponge.getCommandManager().register(DarwinServer.getServer(), spec.build(), command.substring(0, command.indexOf(' ')));
                REGISTERED_COMMANDS.add(command.substring(0, command.indexOf(' ')));
            }

        }
    }

    private Object getValue(Object obj) {
        if (obj instanceof Player) {
            PlayerManager pm = DarwinServer.getUtilMan().get(PlayerManager.class);
            DarwinPlayer dplayer = pm.getPlayer(((Player) obj).getUniqueId(), ((Player) obj).getName());
            return dplayer;

        } else if (obj instanceof User) {
            DarwinUser duser = new DarwinUser(((User) obj).getUniqueId(), ((User) obj).getName());
            return duser;

        } else if (obj instanceof Entity) {
            // TODO : Regular Entity wrapper
            throw new UnsupportedOperationException();

        } else if (obj instanceof Location) {
            Location<Extent> sloc = (Location<Extent>) obj;
            World sworld = (World) sloc.getExtent();

            Vector3d locv = new Vector3d(sloc.getX(), sloc.getY(), sloc.getZ());
            DarwinWorld dworld = new DarwinWorld(sworld.getUniqueId(), sworld.getName());
            DarwinLocation dloc = new DarwinLocation(dworld, locv);
            return dloc;

        } else if (obj instanceof World) {
            DarwinWorld dworld = new DarwinWorld(((World) obj).getUniqueId(), ((World) obj).getName());
            return dworld;

        } else {
            return obj;
        }
    }

    @Override
    protected com.darwinreforged.server.core.commands.context.CommandContext convertContext(CommandContext ctx, CommandSender sender, String command) {
        Multimap<String, Object> parsedArgs;
        try {
            Field parsedArgsF = ctx.getClass().getDeclaredField("parsedArgs");
            if (!parsedArgsF.isAccessible()) parsedArgsF.setAccessible(true);
            parsedArgs = (Multimap<String, Object>) parsedArgsF.get(ctx);
        } catch (IllegalAccessException | ClassCastException | NoSuchFieldException e) {
            DarwinServer.error("Could not load parsed arguments from Sponge command context", e);
            return null;
        }

        List<CommandArgument<?>> arguments = new ArrayList<>();
        List<CommandFlag<?>> flags = new ArrayList<>();

        parsedArgs.asMap().forEach((s, o) -> o.forEach(obj -> {
            if (Pattern.compile("-(-?" + s + ")").matcher(command).find())
                flags.add(new CommandFlag<>(getValue(obj), s));
            else arguments.add(new CommandArgument<>(getValue(obj), s));
        }));

        com.darwinreforged.server.core.commands.context.CommandContext darwinCtx;
        if (sender instanceof DarwinPlayer) {
            DarwinLocation loc = ((DarwinPlayer) sender).getLocation().orElse(null);
            DarwinWorld world = ((DarwinPlayer) sender).getWorld().orElse(null);
            darwinCtx = new com.darwinreforged.server.core.commands.context.CommandContext(
                    arguments.toArray(new CommandArgument<?>[0]),
                    sender, world, loc,
                    new Permissions[0],
                    flags.toArray(new CommandFlag<?>[0])
            );
        } else {
            darwinCtx = new com.darwinreforged.server.core.commands.context.CommandContext(
                    arguments.toArray(new CommandArgument<?>[0]),
                    sender, null, null,
                    new Permissions[0],
                    flags.toArray(new CommandFlag<?>[0])
            );
        }
        return darwinCtx;
    }

    private CommandElement[] parseArguments(String argString) {
        List<CommandElement> elements = new LinkedList<>();
        CommandFlags.Builder cflags = null;
        Matcher m = argFinder.matcher(argString);
        while (m.find()) {
            String part = m.group();
            Matcher ma = argument.matcher(part);
            if (ma.matches()) {
                boolean optional = ma.group(1).charAt(0) == '[';
                String argUnp = ma.group(2);
                CommandElement[] result = parseArguments(argUnp);
                if (result.length == 0) {
                    SpongeArgumentTypeValue satv = argValue(ma.group(2));
                    CommandElement permEl = satv.getArgument();
                    result = new CommandElement[]{permEl};
                }
                CommandElement el = wrap(result);
                if (optional) {
                    elements.add(GenericArguments.optional(el));
                } else {
                    elements.add(el);
                }
            } else {
                Matcher mf = flag.matcher(part);
                if (mf.matches()) {
                    if (cflags == null) cflags = GenericArguments.flags();
                    parseFlag(cflags, mf.group(1), mf.group(2));
                } else {
                    DarwinServer.error("Argument type was not recognized for `" + part + "`");
                }
            }
        }
        if (cflags==null) return elements.toArray(new CommandElement[0]);
        else return new CommandElement[]{ cflags.buildWith(wrap(elements.toArray(new CommandElement[0]))) };
    }

    private void parseFlag(CommandFlags.Builder flags, String name, String value) {
        if (value == null) {
            int at;
            if ((at=name.indexOf(':'))>=0) {
                String a=name.substring(0, at), b=name.substring(at+1);
                flags.permissionFlag(b, a);
            } else {
                flags.flag(name);
            }
        } else {
            ArgumentTypeValue<CommandElement> av = argValue(value);
            if (name.indexOf(':') >= 0) {
                DarwinServer.error("Flag values do not support permissions at flag `" + name + "`. Permit the value instead");
            }
            flags.valueFlag(av.getArgument(), name);
        }
    }


    private static CommandElement wrap(CommandElement... elements) {
        if (elements.length == 0) {
            return GenericArguments.none();
        } else if (elements.length == 1) {
            return elements[0];
        } else {
            return GenericArguments.seq(elements);
        }
    }

    private CommandExecutor buildExecutor(CommandRunner runner, String command) {
        return (src, args) -> {
            CommandSender sender;
            if (src instanceof Player) sender = DarwinServer.getUtilMan().get(PlayerManager.class).getPlayer(((Player) src).getUniqueId(), src.getName());
            else if (src instanceof ConsoleSource) sender = Console.instance;
            else sender = null;

            com.darwinreforged.server.core.commands.context.CommandContext ctx = convertContext(args, sender, command);

            if (src instanceof Player) {
                runner.run(sender, ctx);
            } else if (src instanceof ConsoleSource) {
                runner.run(Console.instance, ctx);
            } else {
                src.sendMessage(Text.of(TextColors.RED, "Could not determine if you are a player or console, what are you?"));
            }
            return CommandResult.success();
        };
    }

    public static class FaweArgument extends CommandElement {

        enum FaweTypes {
            REGION, EDIT_SESSION, PATTERN, MASK
        }

        private final FaweTypes type;

        protected FaweArgument(@Nullable Text key, FaweTypes type) {
            super(key);
            this.type = type;
        }

        @Nullable
        @Override
        protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
            try {
                FawePlayer<?> fawePlayer = FawePlayer.wrap(source);
                ParserContext pctx = new ParserContext();
                pctx.setActor(fawePlayer.getPlayer());
                pctx.setWorld(fawePlayer.getWorld());
                pctx.setSession(fawePlayer.getSession());

                switch (this.type) {
                    case REGION:
                        return fawePlayer.getSelection();
                    case EDIT_SESSION:
                        return fawePlayer.getNewEditSession();
                    case PATTERN:
                        String patternRaw = args.getRaw();
                        return WorldEdit.getInstance().getPatternFactory().parseFromInput(patternRaw, pctx);
                    case MASK:
                        String maskRaw = args.getRaw();
                        return WorldEdit.getInstance().getMaskFactory().parseFromInput(maskRaw, pctx);
                }
            } catch (Throwable e) {
                DarwinServer.error("Failed to parse WorldEdit argument", e);
            }
            return null;
        }

        @Override
        public List<String> complete(
                CommandSource src,
                CommandArgs args,
                CommandContext context) {
            return new ArrayList<>();
        }
    }

    public static class ModuleArgument extends CommandElement {

        protected ModuleArgument(@Nullable Text key) {
            super(key);
        }

        @Nullable
        @Override
        protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
            Object module = DarwinServer.getModMan().getModuleInfo(args.next());
            if (module != null && ((Optional<?>) module).isPresent()) return ((Optional<?>) module).get();
            return null;
        }

        @Override
        public List<String> complete(
                CommandSource src,
                CommandArgs args,
                CommandContext context) {
            return DarwinServer.getModMan().getAllModuleInfo().stream().map(Module::id).collect(Collectors.toList());
        }
    }

}
