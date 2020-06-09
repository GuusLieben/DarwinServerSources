package com.darwinreforged.server.modules.moduledata;

import com.darwinreforged.server.core.DarwinServer;
import com.darwinreforged.server.core.events.internal.server.ServerStartedEvent;
import com.darwinreforged.server.core.events.util.Listener;
import com.darwinreforged.server.core.files.FileManager;
import com.darwinreforged.server.core.modules.Module;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 The type Darwin module data.
 */
@Module(id = "darwinmoduledata", name = "Darwin Module Data", version = "0.0.2", description = "Collects and stores plugin data", authors = {"GuusLieben"})
public class DarwinModuleData {

    private final Map<String, Map<String, Object>> data = new HashMap<>();

    /**
     Instantiates a new Darwin module data.
     */
    public DarwinModuleData() {
    }

    /**
     On server start.

     @param event
     the event
     */
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
}
