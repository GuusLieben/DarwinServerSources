package com.darwinreforged.server.sponge;

import com.darwinreforged.server.core.DarwinServer;
import com.darwinreforged.server.core.events.internal.server.ServerInitEvent;
import com.darwinreforged.server.core.events.internal.server.ServerStartedEvent;
import com.darwinreforged.server.core.internal.ServerType;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;

import java.io.File;
import java.io.IOException;

/**
 Central plugin which handles module registrations and passes early server events
 */
@Plugin(
        id = "darwinserver",
        name = "Darwin Server",
        description = "Custom plugins and modifications combined into a single source",
        url = "https://darwinreforged.com",
        authors = {DarwinServer.AUTHOR}
)
public class DarwinServerSponge extends DarwinServer {

    public DarwinServerSponge() throws InstantiationException {
    }

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        getEventBus().post(new ServerStartedEvent(null));
    }

    @Listener
    public void onServerInit(GameInitializationEvent event) throws IOException {
        setupPlatform();

        Sponge.getEventManager().registerListeners(this, new SpongeListener());
        getEventBus().post(new ServerInitEvent(null));
    }

    @Override
    public void runAsync(Runnable runnable) {
        Sponge.getScheduler().createAsyncExecutor(this).execute(runnable);
    }

    @Override
    public void runOnMainThread(Runnable runnable) {
        Sponge.getScheduler().createSyncExecutor(this).execute(runnable);
    }

    @Override
    public ServerType getServerType() {
        return ServerType.SPONGE;
    }

    @Override
    protected File getLibraryDirectory() {
        return Sponge.getGame().getSavesDirectory().resolve("data/darwinserver/libraries").toFile();
    }

    @Override
    protected void stopServer(String message) {
        getLog().warn("Warning! Server shutdown requested!");
        Sponge.getServer().shutdown(Text.of(message));
    }
}
