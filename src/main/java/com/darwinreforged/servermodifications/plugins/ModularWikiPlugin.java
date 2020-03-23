package com.darwinreforged.servermodifications.plugins;

import com.darwinreforged.servermodifications.resources.Translations;
import com.darwinreforged.servermodifications.util.PlayerUtils;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;

@Plugin(
        id = "modularwiki",
        name = "Modular Wiki",
        description = "Configuration based wiki plugin for Darwin Reforged",
        authors = {
                "DiggyNevs"
        },
        version = "0.1.7"
)
public class ModularWikiPlugin implements CommandExecutor {

    @Inject
    private Logger logger;

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path root;

    public static WikiObject[] wikiObjects;

    private CommandSpec wikiCommandSpec =
            CommandSpec.builder()
                    .permission("modwiki.use")
                    .executor(new WikiCommand())
                    .arguments(GenericArguments.optional(GenericArguments.string(Text.of("entry"))))
                    .build();

    private CommandSpec wikiReloadCommandSpec =
            CommandSpec.builder()
                    .permission("modwiki.reload")
                    .executor(this)
                    .build();

    private CommandSpec wikiShareCommandSpec =
            CommandSpec.builder()
                    .permission("modwiki.share")
                    .executor(new WikiShareCommand())
                    .arguments(GenericArguments.string(Text.of("entry")), GenericArguments.player(Text.of("pl")))
                    .build();

    public static Text defaultBreakLine = Translations.WIKI_BREAKLINE.ft("Wiki");

    public ModularWikiPlugin() {
    }

    public static Text getBreakLine(String tag) {
        return Text.of(
                TextColors.DARK_AQUA, TextStyles.STRIKETHROUGH, "============",
                TextStyles.RESET, TextColors.AQUA, String.format(" %s ", tag),
                TextColors.DARK_AQUA, TextStyles.STRIKETHROUGH, "============");
    }

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        if (this.init()) {
            Sponge.getCommandManager().register(this, wikiCommandSpec, "wiki");
            Sponge.getCommandManager().register(this, wikiReloadCommandSpec, "wikireload");
            Sponge.getCommandManager().register(this, wikiShareCommandSpec, "wikishare");
        }
    }

    public boolean init() {
        File configurationFile = new File(this.root.toFile(), "wiki.conf");
        if (!configurationFile.exists()) {
            try {
                configurationFile.getParentFile().mkdirs();
                if (!configurationFile.createNewFile())
                    throw new IOException("Failed to create configuration file, do I have permission?");
            } catch (IOException e) {
                this.logger.error(e.getMessage());
                return false;
            }
        }


        try (JsonReader reader = new JsonReader(new FileReader(configurationFile))) {
            ModularWikiPlugin.wikiObjects = new Gson().fromJson(reader, WikiObject[].class);
            return true;
        } catch (IOException e) {
            this.logger.error(e.getMessage());
            this.logger.warn("Could not read configuration file, ModularWiki command will not be registered");
            return false;
        }
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        PlayerUtils.tell(src, (this.init() ? Translations.WIKI_RELOADED_USER_SUCCESS.t() : Translations.WIKI_RELOADED_USER_FAILURE.t()));
        return CommandResult.success();
    }

    public static class WikiCommand implements CommandExecutor {

        @Override
        public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
            Optional<String> optionalEntry = args.getOne("entry");
            if (src instanceof Player) {
                Player pl = (Player) src;

                if (ModularWikiPlugin.wikiObjects != null) {
                    if (optionalEntry.isPresent()) {
                        // Specific entry
                        Optional<WikiObject> optionalWikiObject = Arrays.stream(ModularWikiPlugin.wikiObjects)
                                .filter(wikiObject -> wikiObject.id.equals(optionalEntry.get()))
                                .findFirst();
                        if (optionalWikiObject.isPresent()) {
                            WikiObject wikiObject = optionalWikiObject.get();
                            if (wikiObject.permission == null || pl.hasPermission(wikiObject.permission)) {
                                Text.Builder multiLineDescriptionBuilder = Text.builder();
                                Arrays.asList(wikiObject.description).forEach(line -> {
                                    String[] partialLines = line.split("\\|");
                                    if (partialLines.length == 2) {
                                        Text.Builder singleLineBuilder = Text.builder();
                                        singleLineBuilder
                                                .append(Text.of("\n", TextColors.WHITE, partialLines[1]))
                                                .onHover(TextActions.showText(Translations.WIKI_OPEN_ENTRY_HOVER.ft(partialLines[0])))
                                                .onClick(TextActions.runCommand(String.format("/modularwiki:wiki %s", partialLines[0])));

                                        multiLineDescriptionBuilder.append(singleLineBuilder.build());
                                    } else multiLineDescriptionBuilder.append(Text.of("\n", TextColors.WHITE, line));
                                });
                                Text shareButton = Text.builder()
                                        .append(Translations.WIKI_SHARE_BUTTON.ft(wikiObject.name))
                                        .onHover(TextActions.showText(Translations.WIKI_SHARE_BUTTON_HOVER.t()))
                                        .onClick(TextActions.suggestCommand(String.format("/modularwiki:wikishare %s", wikiObject.id)))
                                        .build();
                                PlayerUtils.tell(pl, Translations.WIKI_BREAKLINE.ft(wikiObject.name));
                                PlayerUtils.tell(pl, Text.of(multiLineDescriptionBuilder.build(), "\n", shareButton, ModularWikiPlugin.getBreakLine(wikiObject.name)));
                            } else {
                                PlayerUtils.tell(pl, Translations.WIKI_NOT_ALLOWED.ft(wikiObject.permission));
                            }
                        } else {
                            PlayerUtils.tell(pl, Translations.WIKI_NOT_FOUND.ft(optionalEntry.get()));
                        }

                    } else {
                        // List all entries
                        Text.Builder multiLineEntryBuilder = Text.builder();

                        Arrays.asList(ModularWikiPlugin.wikiObjects).forEach(wikiObject -> {
                            if (wikiObject.permission == null || pl.hasPermission(wikiObject.permission) && !wikiObject.hide) {
                                Text singleEntryText = Text.builder()
                                        .append(Translations.WIKI_LIST_ROW.ft(wikiObject.name))
                                        .onClick(TextActions.runCommand("/modularwiki:wiki " + wikiObject.id))
                                        .onHover(TextActions.showText(Translations.WIKI_LIST_ROW_HOVER.ft(wikiObject.name)))
                                        .build();
                                multiLineEntryBuilder.append(singleEntryText);
                            }
                        });
                        PlayerUtils.tell(pl, Text.of(ModularWikiPlugin.defaultBreakLine, multiLineEntryBuilder.build(), "\n", ModularWikiPlugin.defaultBreakLine), false);
                    }
                } else {
                    PlayerUtils.tell(pl, Translations.WIKI_NO_ENTRIES.t());
                }
            }
            return CommandResult.success();
        }
    }

    public static class WikiObject {

        private String id;
        private String name;
        private String permission;
        private String[] description;
        private boolean hide = false;

        public WikiObject(String id, String name, String permission, String[] description, boolean hide) {
            this.id = id;
            this.name = name;
            this.permission = permission;
            this.description = description;
            this.hide = hide;
        }
    }

    public static class WikiShareCommand implements CommandExecutor {

        @Override
        public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
            String entry = (String) args.getOne("entry").orElse(null);
            Player player = (Player) args.getOne("pl").orElse(null);
            if (entry != null && player != null) {
                WikiObject wikiObject = Arrays.stream(ModularWikiPlugin.wikiObjects).filter(wikiObj -> wikiObj.id.equals(entry)).findFirst().orElse(null);
                if (wikiObject != null) {
                    Text shareMessage = Translations.WIKI_SHARED_USER.ft(src.getName(), wikiObject.name);

                    Text viewButton = Text.builder()
                            .append(Translations.WIKI_VIEW_BUTTON.t())
                            .onHover(TextActions.showText(Translations.WIKI_VIEW_BUTTON_HOVER.ft(entry)))
                            .onClick(TextActions.runCommand(String.format("/modularwiki:wiki %s", entry))).build();

                    PlayerUtils.tell(player, Text.of(shareMessage, viewButton));
                } else
                    PlayerUtils.tell(src, Translations.WIKI_NOT_FOUND.ft(entry));
            } else PlayerUtils.tell(src, Translations.WIKI_SHARE_FAILED.t());
            return CommandResult.success();
        }

    }
}
