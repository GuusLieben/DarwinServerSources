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

package org.dockbox.selene.core.impl.command.convert.impl;

import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.pattern.Pattern;

import org.dockbox.selene.core.command.context.CommandValue.Argument;
import org.dockbox.selene.core.exceptions.ConstraintException;
import org.dockbox.selene.core.exceptions.global.UncheckedSeleneException;
import org.dockbox.selene.core.impl.command.convert.ArgumentConverter;
import org.dockbox.selene.core.impl.command.convert.TypeArgumentParsers.DurationParser;
import org.dockbox.selene.core.impl.command.convert.TypeArgumentParsers.LocationParser;
import org.dockbox.selene.core.impl.command.convert.TypeArgumentParsers.PlayerParser;
import org.dockbox.selene.core.impl.command.convert.TypeArgumentParsers.UuidParser;
import org.dockbox.selene.core.impl.command.convert.TypeArgumentParsers.WorldEditMaskParser;
import org.dockbox.selene.core.impl.command.convert.TypeArgumentParsers.WorldEditPatternParser;
import org.dockbox.selene.core.impl.command.convert.TypeArgumentParsers.WorldParser;
import org.dockbox.selene.core.objects.location.Location;
import org.dockbox.selene.core.objects.location.World;
import org.dockbox.selene.core.objects.Exceptional;
import org.dockbox.selene.core.objects.player.Player;
import org.dockbox.selene.core.server.Selene;
import org.dockbox.selene.core.text.Text;
import org.dockbox.selene.core.util.SeleneUtils;
import org.dockbox.selene.core.annotations.extension.Extension;
import org.dockbox.selene.core.extension.ExtensionManager;
import org.dockbox.selene.core.PlayerStorageService;
import org.dockbox.selene.core.WorldStorageService;

import java.time.Duration;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@SuppressWarnings({"ClassWithTooManyFields", "unused"})
public final class ArgumentConverterRegistry {

    private static transient final Collection<ArgumentConverter<?>> CONVERTERS = SeleneUtils.COLLECTION.emptyConcurrentList();

    private ArgumentConverterRegistry() {
    }

    public static boolean hasConverter(String key) {
        return getOptionalConverter(key).isPresent();
    }

    public static ArgumentConverter<?> getConverter(String key) {
        return getOptionalConverter(key).rethrowUnchecked().orNull();
    }

    public static Exceptional<ArgumentConverter<?>> getOptionalConverter(String key) {
        Optional<ArgumentConverter<?>> optional =CONVERTERS.stream()
                .filter(converter -> converter.getKeys().contains(key))
                .findFirst();
        if (optional.isPresent()) return Exceptional.of(optional);
        else return Exceptional.of(new UncheckedSeleneException("No converter present"));
    }


    public static boolean hasConverter(Class<?> type) {
        return getOptionalConverter(type).isPresent();
    }


    public static <T> ArgumentConverter<T> getConverter(Class<T> type) {
        return getOptionalConverter(type).orNull();
    }

    private static <T> Exceptional<ArgumentConverter<T>> getOptionalConverter(Class<T> type) {
        //noinspection unchecked
        return Exceptional.of(CONVERTERS.stream()
                .filter(converter -> SeleneUtils.REFLECTION.isAssignableFrom(converter.getType(), type))
                .map(converter -> (ArgumentConverter<T>) converter)
                .findFirst());
    }

    public static void registerConverter(ArgumentConverter<?> converter) {
        for (String key : converter.getKeys()) {
            for (ArgumentConverter<?> existingConverter : CONVERTERS) {
                if (existingConverter.getKeys().contains(key)) {
                    throw new ConstraintException("Duplicate argument key '" + key + "' found while registering converter");
                }
            }
        }
        CONVERTERS.add(converter);
    }

    public static final ArgumentConverter<Boolean> BOOLEAN = new ConstantArgumentConverter<>(
            new String[]{"bool", "boolean"},
            Boolean.class,
            Boolean::valueOf,
            false,
            "true", "false", "yes", "no");

    public static final ArgumentConverter<Double> DOUBLE = new ConstantArgumentConverter<>(
            new String[]{"double"},
            Double.class,
            Double::valueOf,
            -1D);

    public static final ArgumentConverter<Integer> INTEGER = new ConstantArgumentConverter<>(
            new String[]{"int", "integer"},
            Integer.class,
            Integer::valueOf,
            -1
    );

    public static final ArgumentConverter<Long> LONG = new ConstantArgumentConverter<>(
            new String[]{"long"},
            Long.class,
            Long::valueOf,
            -1L
    );

    public static final ArgumentConverter<Player> PLAYER = new ParserArgumentConverter<>(
            Player.class,
            PlayerParser::new,
            s -> Selene.getInstance(PlayerStorageService.class).getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.startsWith(s))
                    .collect(Collectors.toList()),
            "player", "user"
    );

    public static final ArgumentConverter<UUID> UUID = new ParserArgumentConverter<>(
            UUID.class,
            UuidParser::new,
            s -> SeleneUtils.COLLECTION.emptyList(),
            "uuid", "uniqueid"
    );

    public static final ArgumentConverter<Location> LOCATION = new ParserArgumentConverter<>(
            Location.class,
            LocationParser::new,
            s -> SeleneUtils.COLLECTION.emptyList(),
            "location", "loc", "position", "pos"
    );

    public static final ArgumentConverter<World> WORLD = new ParserArgumentConverter<>(
            World.class,
            WorldParser::new,
            s -> Selene.getInstance(WorldStorageService.class).getLoadedWorlds().stream()
                    .map(World::getName)
                    .filter(n -> n.startsWith(s))
                    .collect(Collectors.toList()),
            "world", "dim", "dimension"
    );

    public static final ArgumentConverter<String> STRING = new SimpleArgumentConverter<>(
            String.class,
            Exceptional::of,
            s -> SeleneUtils.COLLECTION.emptyList(),
            "string", "remaining", "remainingstring"
    );

    public static final ArgumentConverter<Text> TEXT = new ConstantArgumentConverter<>(
            new String[]{"text"},
            Text.class,
            Text::of,
            Text.of()
    );

    public static final ArgumentConverter<Mask> MASK = new SourceAwareArgumentConverter<>(
            Mask.class,
            (source, s) -> {
                if (source instanceof Player) {
                    return new WorldEditMaskParser(((Player) source).getFawePlayer()
                            .orNull())
                            .parse(new Argument<>(s, "mask"));
                }
                return Exceptional.empty();
            },
            (source, s) -> SeleneUtils.COLLECTION.emptyList(),
            "mask"
    );

    public static final ArgumentConverter<Pattern> PATTERN = new SourceAwareArgumentConverter<>(
            Pattern.class,
            (source, s) -> {
                if (source instanceof Player) {
                    return new WorldEditPatternParser(((Player) source).getFawePlayer()
                            .orNull())
                            .parse(new Argument<>(s, "pattern"));
                }
                return Exceptional.empty();
            },
            (source, s) -> SeleneUtils.COLLECTION.emptyList(),
            "pattern"
    );

    public static final ArgumentConverter<BaseBlock> BLOCK = new ConstantArgumentConverter<>(
            new String[]{"block", "baseblock"},
            BaseBlock.class,
            string -> {
                String[] parts = string.split(":");
                int id = Integer.parseInt(parts[0]);
                if (2 == parts.length) {
                    int data = Integer.parseInt(parts[1]);
                    return new BaseBlock(id, data);
                }
                return new BaseBlock(id);
            },
            new BaseBlock(0)
    );

    public static final ArgumentConverter<Extension> EXTENSION = new ConstantArgumentConverter<>(
            new String[]{"extension", "ext"},
            Extension.class,
            s -> Selene.getInstance(ExtensionManager.class).getHeader(s),
            Selene.getInstance(ExtensionManager.class).getRegisteredExtensionIds()
                    .toArray(new String[0])
    );

    public static final ArgumentConverter<Duration> DURATION = new ConstantArgumentConverter<>(
            new String[]{"duration", "time"},
            Duration.class,
            s -> new DurationParser().parse(new Argument<>(s, "duration")).orElse(Duration.ZERO),
            Duration.ZERO
    );

}
