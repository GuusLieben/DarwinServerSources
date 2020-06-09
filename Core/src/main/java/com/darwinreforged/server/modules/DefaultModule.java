package com.darwinreforged.server.modules;


import com.darwinreforged.server.core.chat.ClickEvent;
import com.darwinreforged.server.core.chat.ClickEvent.ClickAction;
import com.darwinreforged.server.core.chat.HoverEvent;
import com.darwinreforged.server.core.chat.HoverEvent.HoverAction;
import com.darwinreforged.server.core.chat.Pagination;
import com.darwinreforged.server.core.chat.Pagination.PaginationBuilder;
import com.darwinreforged.server.core.chat.Text;
import com.darwinreforged.server.core.commands.annotations.Command;
import com.darwinreforged.server.core.commands.context.CommandArgument;
import com.darwinreforged.server.core.commands.context.CommandContext;
import com.darwinreforged.server.core.events.internal.server.ServerReloadEvent;
import com.darwinreforged.server.core.events.internal.server.ServerStartedEvent;
import com.darwinreforged.server.core.events.util.Listener;
import com.darwinreforged.server.core.DarwinServer;
import com.darwinreforged.server.core.files.FileManager;
import com.darwinreforged.server.core.modules.DisabledModule;
import com.darwinreforged.server.core.modules.Module;
import com.darwinreforged.server.core.resources.translations.DefaultTranslations;
import com.darwinreforged.server.core.types.living.CommandSender;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Module(id = "darwinserver", name = "Server Config Module", description = "Native module used for basic actions for DarwinServer", authors = {"GuusLieben"})
public class DefaultModule {


    private final Map<String, Map<String, Object>> data = new HashMap<>();

    @Listener
    public void onServerStart(ServerStartedEvent event) {
        File dataFile = new File(DarwinServer.getUtilMan().get(FileManager.class).getDataDirectory(this).toFile(), "module_data.yml");
        DarwinServer.getModMan().getAllModuleInfo().forEach(this::registerPlugin);
        DarwinServer.getUtilMan().get(FileManager.class).writeYamlDataToFile(data, dataFile);
    }

    private void registerPlugin(Module module) {
        // Data storage before Yaml conversion
        Map<String, Object> data = new HashMap<>();

        // Generic data
        data.put("id", module.id());
        data.put("name", module.name());
        data.put("version", module.version());
        data.put("description", module.description());
        data.put("url", module.url());
        data.put("authors", module.authors());
        data.put("source", DarwinServer.getModMan().getModuleSource(module.id()));

        // Write plugin data to unique file
        this.data.put(module.id(), data);
    }

    @Command(aliases = "dserver", usage = "dserver [module]", desc = "Returns active and failed modules to the player", min = 0, context = "dserver [module{Module}]")
    public void commandList(CommandSender src, CommandContext ctx) {
        Optional<CommandArgument<Module>> moduleCandidate = ctx.getArgument("module", Module.class);

        if (moduleCandidate.isPresent()) {
            Module mod = moduleCandidate.get().getValue();
            String[] dependencies = Arrays.stream(mod.dependencies()).map(dep ->
                    DefaultTranslations.DARWIN_SINGLE_MODULE_DEPENDENCY.f(dep.toString().toLowerCase(), dep.getMainClass(), dep.isLoaded() ? "Present" : "Absent")
            ).toArray(String[]::new);
            String source = DefaultTranslations.MODULE_SOURCE.f(DarwinServer.getModMan().getModuleSources().get(mod.id()));

            Text message = Text.of(
                    DefaultTranslations.DARWIN_SINGLE_MODULE_HEADER.f(mod.name()), '\n',
                    DefaultTranslations.DARWIN_SINGLE_MODULE_DATA.f(
                            mod.id(),
                            mod.name(),
                            mod.description(),
                            mod.version(),
                            mod.url(),
                            dependencies.length > 0 ? String.join(", ", dependencies) : DefaultTranslations.NONE.s().toLowerCase(),
                            mod.authors().length > 0 ? String.join(", ", mod.authors()) : DefaultTranslations.UNKNOWN.s().toLowerCase(),
                            source
                    )
            );
            src.sendMessage(message, false);

        } else {
            List<Text> moduleContext = new ArrayList<>();
            DarwinServer.getModMan().getModules().forEach((clazz, ignored) -> {
                Optional<Module> infoOptional = DarwinServer.getModMan().getModuleInfo(clazz);
                if (infoOptional.isPresent()) {
                    Module info = infoOptional.get();
                    String name = info.name();
                    String id = info.id();
                    boolean disabled = clazz.getAnnotation(DisabledModule.class) != null;
                    String source = DefaultTranslations.MODULE_SOURCE.f(DarwinServer.getModMan().getModuleSource(id));
                    Text activeModule = Text.of(DefaultTranslations.ACTIVE_MODULE_ROW.f(name, id, source));
                    activeModule.setHoverEvent(new HoverEvent(HoverAction.SHOW_TEXT, DefaultTranslations.DARWIN_SERVER_MODULE_HOVER.f(id)));
                    activeModule.setClickEvent(new ClickEvent(ClickAction.RUN_COMMAND, "/dserver " + id));
                    moduleContext.add(disabled ? Text.of(DefaultTranslations.DISABLED_MODULE_ROW.f(name, id, source))
                            : activeModule);
                }
            });
            DarwinServer.getModMan().getFailedModules().forEach(module -> moduleContext.add(Text.of(DefaultTranslations.FAILED_MODULE_ROW.f(module))));

            Text header = Text.of(DefaultTranslations.DARWIN_SERVER_VERSION.f(DarwinServer.getVersion(), DarwinServer.getServer().getServerType()))
                    .append(Text.NEW_LINE)
                    .append(DefaultTranslations.DARWIN_SERVER_UPDATE.f(DarwinServer.getLastUpdate()))
                    .append(Text.NEW_LINE)
                    .append(DefaultTranslations.DARWIN_SERVER_AUTHOR.f(DarwinServer.getAuthor()))
                    .append(Text.NEW_LINE)
                    .append(DefaultTranslations.DARWIN_SERVER_MODULE_HEAD.s());

            PaginationBuilder builder = Pagination.builder();
            builder
                    .title(Text.of(DefaultTranslations.DARWIN_MODULE_TITLE.s()))
                    .padding(Text.of(DefaultTranslations.DARWIN_MODULE_PADDING.s()))
                    .contents(moduleContext)
                    .header(header)
                    .build().sendTo(src);
        }
    }

    @Listener
    public void onServerReload(ServerReloadEvent event) {
        DarwinServer.reload();
    }
    
}
