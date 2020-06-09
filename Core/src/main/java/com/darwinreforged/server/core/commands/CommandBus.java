package com.darwinreforged.server.core.commands;

import com.darwinreforged.server.core.DarwinServer;
import com.darwinreforged.server.core.commands.annotations.Command;
import com.darwinreforged.server.core.commands.annotations.Permission;
import com.darwinreforged.server.core.commands.annotations.Source;
import com.darwinreforged.server.core.commands.context.CommandContext;
import com.darwinreforged.server.core.commands.registrations.ClassRegistration;
import com.darwinreforged.server.core.commands.registrations.CommandRegistration;
import com.darwinreforged.server.core.commands.registrations.SingleMethodRegistration;
import com.darwinreforged.server.core.internal.Utility;
import com.darwinreforged.server.core.modules.Module;
import com.darwinreforged.server.core.resources.Permissions;
import com.darwinreforged.server.core.resources.translations.DefaultTranslations;
import com.darwinreforged.server.core.tuple.Triple;
import com.darwinreforged.server.core.types.internal.Singleton;
import com.darwinreforged.server.core.types.living.CommandSender;
import com.darwinreforged.server.core.types.location.DarwinLocation;
import com.darwinreforged.server.core.types.location.DarwinWorld;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 The type Command bus.
 */
@Utility("Handles command registrations and processing")
public abstract class CommandBus<C, A extends ArgumentTypeValue<?>> {

    public enum Arguments {
        BOOL,
        DOUBLE,
        ENTITY,
        INTEGER,
        LOCATION,
        LONG,
        PLAYER,
        MODULE,
        REMAININGSTRING,
        STRING,
        USER,
        UUID,
        VECTOR,
        WORLD,
        EDITSESSION,
        MASK,
        PATTERN,
        REGION,
        OTHER
    }

    protected static final List<String> REGISTERED_COMMANDS = new ArrayList<>();

    public void register(Object... objs) {
        for (Object obj : objs) {
            Class<?> clazz;
            if (obj instanceof Class) clazz = (Class<?>) obj;
            else clazz = obj.getClass();

            DarwinServer.getLog().info(String.format("\n\nScanning %s for commands", clazz.toGenericString()));
            try {
                if (clazz.isAnnotationPresent(Command.class)) registerClassCommand(clazz, obj);
                else registerSingleMethodCommand(clazz, obj);
            } catch (Throwable e) {
                DarwinServer.getLog().warn(String.format("Failed to register potential command class : %s", clazz.toGenericString()));
                e.printStackTrace();
            }
        }
    }

    private void registerSingleMethodCommand(Class<?> clazz, Object obj) {
        List<Method> methods = new ArrayList<>();
        for (Method method : clazz.getDeclaredMethods()) {
            method.setAccessible(true);
            if (method.isAnnotationPresent(Command.class)) methods.add(method);
        }
        SingleMethodRegistration[] registrations = handleSingleMethod(methods);
        Arrays.stream(registrations)
                .forEach(registration -> Arrays.stream(registration.getAliases())
                        .forEach(alias -> {
                            String context = registration.getCommand().context();
                            String next = context.contains(" ") ? context.replaceFirst(context.substring(0, context.indexOf(' ')), alias) : context;
                            registerCommand(next, registration.getPermission().p(), (s, c) -> {
                                String result = invoke(registration.getMethod(), s, c, registration);
                                if (result == null || !result.equals("success")) s.sendMessage(DefaultTranslations.UNKNOWN_ERROR.f(result), false);
                            });
                            DarwinServer.getLog().info("Registered singular command : /" + alias);
                        }));
    }

    private void registerClassCommand(Class<?> clazz, Object obj) {
        ClassRegistration registration = handleClassType(clazz);
        Arrays.stream(registration.getAliases()).forEach(alias -> {
            if (!(obj instanceof Class)) registration.setSourceInstance(obj);
            AtomicReference<CommandRunner> parentRunner = new AtomicReference<>((s, c) -> s.sendMessage("This command requires arguments!", false));

            Arrays.stream(registration.getSubcommands()).forEach(subRegistration -> {
                CommandRunner methodRunner = (s, c) -> {
                    String result = invoke(
                            subRegistration.getMethod(),
                            s, c,
                            subRegistration);
                    if (result == null || !result.equals("success")) s.sendMessage(DefaultTranslations.UNKNOWN_ERROR.f(result), false);
                };

                Arrays.stream(subRegistration.getAliases()).forEach(subAlias -> {
                    if (!subAlias.equals("")) {
                        String context = subRegistration.getCommand().context();
                        String next = context.contains(" ") ? context.replaceFirst(context.substring(0, context.indexOf(' ')), alias) : context;
                        registerCommand(next, subRegistration.getPermission().p(), methodRunner);
                    } else {
                        parentRunner.set(methodRunner);
                    }
                });
            });

            String context = registration.getCommand().context();
            String next = context.contains(" ") ? context.replaceFirst(context.substring(0, context.indexOf(' ')), alias) : context;
            registerCommand('*' + next, registration.getPermission().p(), parentRunner.get());

            // Printing aliases, not used for actual logic
            List<String> subcommands = new ArrayList<>();
            Arrays.stream(registration.getSubcommands()).forEach(sub -> subcommands.addAll(Arrays.asList(sub.getAliases())));
            DarwinServer.getLog().info(String.format("Registered command : /%s %s", alias, String.join("|", subcommands)));
        });
    }

    private ClassRegistration handleClassType(Class<?> clazz) {
        Triple<Command, Permissions, String[]> information = getCommandInformation(clazz);
        Method[] methods = clazz.getDeclaredMethods();
        SingleMethodRegistration[] registrations = handleSingleMethod(Arrays.stream(methods).filter(m -> m.isAnnotationPresent(Command.class)).collect(Collectors.toList()));
        return new ClassRegistration(information.getThird()[0], information.getThird(), information.getSecond(), information.getFirst(), clazz, registrations);
    }

    private Triple<Command, Permissions, String[]> getCommandInformation(AnnotatedElement element) {
        Command command = element.getAnnotation(Command.class);
        Permissions permission = Permissions.ADMIN_BYPASS;
        if (element.isAnnotationPresent(Permission.class)) {
            Permission ann = element.getAnnotation(Permission.class);
            permission = ann.value();
        }
        return new Triple<>(command, permission, command.aliases());
    }

    private SingleMethodRegistration[] handleSingleMethod(Collection<Method> methods) {
        List<Class<?>> commandTypes = Arrays.asList(CommandSender.class, CommandContext.class);
        List<Class<?>> locationTypes = Arrays.asList(DarwinWorld.class, DarwinLocation.class);

        return methods.stream().filter(method -> {
            boolean allowed = true;

            Command command = method.getAnnotation(Command.class);
            Class<?>[] parameterTypes = method.getParameterTypes();
            for (Class<?> type : parameterTypes) {
                AtomicBoolean defaultTypeAssignable = new AtomicBoolean(false);
                for (Class<?> ct : commandTypes) {
                    if (type.isAssignableFrom(ct) || ct.isAssignableFrom(type)) {
                        defaultTypeAssignable.set(true);
                        break;
                    }
                }

                AtomicBoolean locationTypeAssignable = new AtomicBoolean(false);
                for (Class<?> lt : locationTypes) {
                    if (type.isAssignableFrom(lt)  || lt.isAssignableFrom(type)) {
                        locationTypeAssignable.set(true);
                        break;
                    }
                }

                if (!(defaultTypeAssignable.get() || (type.isAnnotationPresent(Source.class) && command.injectLocations() && locationTypeAssignable.get()))) {
                    allowed = false;
                    break;
                }
            }
            return allowed;

        }).map(method -> {
            method.setAccessible(true);
            Triple<Command, Permissions, String[]> information = getCommandInformation(method);
            return new SingleMethodRegistration(information.getThird()[0], information.getThird(), information.getFirst(), method, information.getSecond());
        }).toArray(SingleMethodRegistration[]::new);
    }

    private String invoke(Method method, CommandSender sender, CommandContext ctx, CommandRegistration registration) {
        try {
            Class<?> c = method.getDeclaringClass();
            List<Object> finalArgs = new ArrayList<>();
            for (Class<?> parameterType : method.getParameterTypes()) {
                if (parameterType.equals(CommandSender.class) || CommandSender.class.isAssignableFrom(parameterType))
                    finalArgs.add(sender);
                else if (parameterType.equals(CommandContext.class) || CommandContext.class.isAssignableFrom(parameterType))
                    finalArgs.add(ctx);
                else
                    throw new IllegalStateException("Method requested parameter type '" + parameterType.toGenericString() + "' which is not provided");
            }

            Object o;
            if (registration.getSourceInstance().isPresent()) {
                o = registration.getSourceInstance().get();
            } else if (c.equals(DarwinServer.class) || c.isAssignableFrom(DarwinServer.class) || DarwinServer.class.isAssignableFrom(c)) {
                o = DarwinServer.getServer();
            } else if (c.isAssignableFrom(Singleton.class)) {
                Field field = c.getDeclaredField("instance");
                o = field.get(null);
            } else {
                Optional<?> modOptional;
                if (c.isAnnotationPresent(Module.class) && (modOptional = DarwinServer.getModMan().getModule(c)).isPresent()) {
                    o = modOptional.get();
                } else {
                    o = c.getConstructor().newInstance();
                }
            }
            method.invoke(o, finalArgs.toArray());
            return "success"; // No error message to return
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | InstantiationException | NoSuchFieldException e) {
            DarwinServer.error("Failed to invoke command", e.getCause());
            return e.getCause().getMessage();
        } catch (Throwable e) {
            return e.getMessage();
        }
    }

    private void registerCommand(String command, String permission, CommandRunner runner) {
        if (command.indexOf(' ') < 0 && !command.startsWith("*")) registerCommandNoArgs(command, permission, runner);
        else registerCommandArgsAndOrChild(command, permission, runner);
    }

    protected static final Pattern argFinder = Pattern.compile("((?:<.+?>)|(?:\\[.+?\\])|(?:-(?:(?:-\\w+)|\\w)(?: [^ -]+)?))"); //each match is a flag or argument
    protected static final Pattern flag = Pattern.compile("-(-?\\w+)(?: ([^ -]+))?"); //g1: name  (g2: value)
    protected static final Pattern argument = Pattern.compile("([\\[<])(.+)[\\]>]"); //g1: <[  g2: run argFinder, if nothing it's a value
    protected static final Pattern value = Pattern.compile("(\\w+)(?:\\{(\\w+)(?::([\\w\\.]+))?\\})?"); //g1: name  g2: if present type, other wise use g1
    protected static final Pattern subcommand = Pattern.compile("[a-z]*");

    protected A argValue(String valueString) {
        String type;
        String key;
        String permission;
        Matcher vm = value.matcher(valueString);
        if (!vm.matches())
            DarwinServer.error("Unknown argument specification `" + valueString + "`, use Type or Name{Type} or Name{Type:Permission}");
        key = vm.group(1);
        type = vm.group(2);
        permission = vm.group(3);
        if (type == null) type = key;

        return getArgumentValue(type, permission, key);
    }

    protected abstract A getArgumentValue(String type, String permission, String key);

    public abstract void registerCommandNoArgs(String command, String permission, CommandRunner runner);

    protected abstract CommandContext convertContext(C ctx, CommandSender sender, String command);

    public abstract void registerCommandArgsAndOrChild(String command, String permission, CommandRunner runner);

    @FunctionalInterface
    public interface CommandRunner {
        void run(CommandSender sender, CommandContext ctx);
    }
}
