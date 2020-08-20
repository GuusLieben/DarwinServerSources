package org.dockbox.darwin.sponge;

import net.byteflux.libby.LibraryManager;

import org.dockbox.darwin.core.command.CommandBus;
import org.dockbox.darwin.core.events.server.ServerEvent.Init;
import org.dockbox.darwin.core.server.Server;
import org.dockbox.darwin.core.util.events.EventBus;
import org.dockbox.darwin.core.util.library.LibraryArtifact;
import org.dockbox.darwin.core.util.module.ModuleLoader;
import org.dockbox.darwin.core.util.module.ModuleScanner;
import org.dockbox.darwin.sponge.listeners.SpongeEventListener;
import org.dockbox.darwin.sponge.util.inject.SpongeCommonInjector;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;

@Plugin(
        id = "darwinserver",
        name = "Darwin Server",
        description = "Custom plugins and modifications combined into a single source",
        url = "https://darwinreforged.com",
        authors = "GuusLieben",
        dependencies = {
                @Dependency(id = "plotsquared"),
                @Dependency(id = "nucleus"),
                @Dependency(id = "luckperms")
        }
)
public class SpongeServer extends Server<LibraryManager> {

    public SpongeServer() {
        super(new SpongeCommonInjector());
    }

    @Listener
    public void onServerInit(GameInitializationEvent event) {
        Sponge.getEventManager().registerListeners(this, new SpongeEventListener());

        Iterable<Class<?>> annotatedCandidates = Server.getInstance(ModuleScanner.class)
                .collectClassCandidates("org.dockbox.darwin.integrated")
                .getAnnotatedCandidates();
        EventBus eb = getInstance(EventBus.class);
        CommandBus cb = getInstance(CommandBus.class);
        ModuleLoader loader = getInstance(ModuleLoader.class);

        annotatedCandidates.forEach(module -> {
            loader.loadCandidate(module);
            loader.getModuleInstance(module).ifPresent(instance -> {
                eb.subscribe(instance);
                cb.register(instance);
            });
        });

        getInstance(EventBus.class).post(new Init());
    }

    @NotNull
    @Override
    public ServerType getServerType() {
        return ServerType.SPONGE;
    }

    @Override
    protected LibraryManager getLoader() {
        // TODO : Confirm inject works for Sponge Library Manager (injecting Logger, Path, Plugin)
        return getInstance(LibraryManager.class);
    }

    @Override
    protected LibraryArtifact[] getArtifacts() {
        // Define libraries to download, specifically targeting Sponge
        return new LibraryArtifact[0];
    }

    public static void main(String[] args) {
        System.out.println("DarwinServer is a framework plugin, it should not be started as a separate application.");
        System.exit(8);
    }
}
