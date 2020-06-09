package com.darwinreforged.server.modules.todo.modularwiki;

import com.darwinreforged.server.core.DarwinServer;
import com.darwinreforged.server.core.chat.ClickEvent;
import com.darwinreforged.server.core.chat.ClickEvent.ClickAction;
import com.darwinreforged.server.core.chat.HoverEvent;
import com.darwinreforged.server.core.chat.HoverEvent.HoverAction;
import com.darwinreforged.server.core.chat.Pagination;
import com.darwinreforged.server.core.chat.Text;
import com.darwinreforged.server.core.commands.annotations.Command;
import com.darwinreforged.server.core.commands.annotations.Permission;
import com.darwinreforged.server.core.commands.context.CommandArgument;
import com.darwinreforged.server.core.commands.context.CommandContext;
import com.darwinreforged.server.core.events.internal.server.ServerReloadEvent;
import com.darwinreforged.server.core.events.internal.server.ServerStartedEvent;
import com.darwinreforged.server.core.events.util.Listener;
import com.darwinreforged.server.core.files.FileManager;
import com.darwinreforged.server.core.modules.Module;
import com.darwinreforged.server.core.player.DarwinPlayer;
import com.darwinreforged.server.core.resources.Permissions;
import com.darwinreforged.server.core.resources.translations.DefaultTranslations;
import com.darwinreforged.server.core.types.living.CommandSender;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Module(
        id = "modularwiki",
        name = "Modular Wiki",
        description = "Configuration based wiki plugin for Darwin Reforged",
        authors = {
                "GuusLieben"
        },
        version = "0.1.7"
)
public class ModularWikiModule {

    public static WikiStorageModel storageModel;

    public ModularWikiModule() {
    }

    @Listener
    public void onServerStart(ServerStartedEvent event) {
        this.init();
    }

    @Listener
    public void onServerReload(ServerReloadEvent event) {
        this.init();
    }

    public void init() {
        FileManager fm = DarwinServer.getUtilMan().get(FileManager.class);
        File entryFile = new File(fm.getDataDirectory(this).toFile(), "entries.yml");
        ModularWikiModule.storageModel = fm.getYamlDataFromFile(entryFile, WikiStorageModel.class, null);

        if (ModularWikiModule.storageModel == null || ModularWikiModule.storageModel.entries.size() == 0) {
            ModularWikiModule.storageModel = new WikiStorageModel(
                    Collections.singletonList(new WikiObject("sample", "Sample wiki", "wiki.entry", new String[]{"Line 1", "Line 2"}, false)));
            fm.writeYamlDataToFile(ModularWikiModule.storageModel, entryFile);
        }
    }

    @Command(aliases = "wiki", usage = "wiki [entry]", desc = "Reads a specific entry to the sender", context = "wiki [entry{String}]")
    @Permission(Permissions.MODWIKI_USE)
    public void execute(CommandSender src, CommandContext ctx) {
        Optional<CommandArgument<String>> optionalEntry = ctx.getStringArgument("entry");
        if (src instanceof DarwinPlayer) {

            if (ModularWikiModule.storageModel != null) {
                if (optionalEntry.isPresent()) {
                    // Specific entry
                    Optional<WikiObject> optionalWikiObject = ModularWikiModule.storageModel.getEntries().stream()
                            .filter(wikiObject -> wikiObject.getId().equals(optionalEntry.get().getValue()))
                            .findFirst();
                    if (optionalWikiObject.isPresent()) {
                        WikiObject wikiObject = optionalWikiObject.get();
                        if (wikiObject.getPermission() == null || src.hasPermission(wikiObject.getPermission())) {
                            List<Text> content = new ArrayList<>();

                            Arrays.asList(wikiObject.getDescription()).forEach(line -> {
                                String[] partialLines = line.split("\\|");
                                if (partialLines.length == 2) {
                                    Text singleLine = Text.of(partialLines[1])
                                            .setHoverEvent(new HoverEvent(HoverAction.SHOW_TEXT, WikiTranslations.WIKI_OPEN_ENTRY_HOVER.f(partialLines[0])))
                                            .setClickEvent(new ClickEvent(ClickAction.RUN_COMMAND, String.format("/wiki %s", partialLines[0])));

                                    content.add(singleLine);
                                } else content.add(Text.of(line));
                            });
                            Text shareButton = Text.of(WikiTranslations.WIKI_SHARE_BUTTON.f(wikiObject.getName()))
                                    .setHoverEvent(new HoverEvent(HoverAction.SHOW_TEXT, WikiTranslations.WIKI_SHARE_BUTTON_HOVER.s()))
                                    .setClickEvent(new ClickEvent(ClickAction.SUGGEST_COMMAND, String.format("/sharewiki %s", wikiObject.getId())));
                            Pagination.builder()
                                    .contents(content)
                                    .footer(shareButton)
                                    .title(Text.of(DefaultTranslations.DEFAULT_TITLE.f(wikiObject.getName())))
                                    .padding(Text.of(DefaultTranslations.DARWIN_MODULE_PADDING.s()))
                                    .build().sendTo(src);

                        } else {
                            src.sendMessage(WikiTranslations.WIKI_NOT_ALLOWED.f(wikiObject.getPermission()), false);
                        }
                    } else {
                        src.sendMessage(WikiTranslations.WIKI_NOT_FOUND.f(optionalEntry.get().getValue()), false);
                    }

                } else {
                    // List all entries
                    List<Text> content = new ArrayList<>();

                    ModularWikiModule.storageModel.getEntries().stream().filter(wikiObject -> !wikiObject.isHidden()).forEach(wikiObject -> {
                        if (wikiObject.getPermission() == null || src.hasPermission(wikiObject.getPermission()) && !wikiObject.isHidden()) {
                            Text singleEntryText = Text.of(WikiTranslations.WIKI_LIST_ROW.f(wikiObject.getName()))
                                    .setHoverEvent(new HoverEvent(HoverAction.SHOW_TEXT, WikiTranslations.WIKI_LIST_ROW_HOVER.f(wikiObject.getName())))
                                    .setClickEvent(new ClickEvent(ClickAction.RUN_COMMAND, String.format("/wiki %s ", wikiObject.getId())));
                            content.add(singleEntryText);
                        }
                    });

                    Pagination.builder()
                            .contents(content)
                            .title(Text.of(DefaultTranslations.DEFAULT_TITLE.f("Wiki")))
                            .padding(Text.of(DefaultTranslations.DARWIN_MODULE_PADDING.s()))
                            .build().sendTo(src);

                }
            } else {
                src.sendMessage(WikiTranslations.WIKI_NO_ENTRIES.s(), false);
            }
        }
    }

    @Command(aliases = "sharewiki", usage = "sharewiki <entry> <player>", desc = "Shares a specific entry", context = "sharewiki <entry{String}> <player{Player}>")
    @Permission(Permissions.MODWIKI_SHARE)
    public void execute(DarwinPlayer src, CommandContext ctx) {
        Optional<CommandArgument<String>> entryArg = ctx.getStringArgument("entry");
        Optional<CommandArgument<DarwinPlayer>> playerArg = ctx.getArgument("player", DarwinPlayer.class);
        if (entryArg.isPresent() && playerArg.isPresent()) {
            DarwinPlayer p = playerArg.get().getValue();
            String entry = entryArg.get().getValue();
            WikiObject wikiObject = ModularWikiModule.storageModel.getEntries().stream().filter(wikiObj -> wikiObj.getId().equals(entry)).findFirst().orElse(null);
            if (wikiObject != null) {
                String shareMessage = WikiTranslations.WIKI_SHARED_USER.f(src.getName(), wikiObject.getName());

                Text viewButton = Text.of(shareMessage, " ", WikiTranslations.WIKI_VIEW_BUTTON.s())
                        .setHoverEvent(new HoverEvent(HoverAction.SHOW_TEXT, WikiTranslations.WIKI_VIEW_BUTTON_HOVER.f(entry)))
                        .setClickEvent(new ClickEvent(ClickAction.RUN_COMMAND, String.format("/wiki %s", entry)));

                p.sendMessage(viewButton, false);
                src.sendMessage(WikiTranslations.WIKI_SHARED_WITH.f(entry, p.getName()), false);
            } else
                src.sendMessage(WikiTranslations.WIKI_NOT_FOUND.f(entry), false);
        }
    }


}
