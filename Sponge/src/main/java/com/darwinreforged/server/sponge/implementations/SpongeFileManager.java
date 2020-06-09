package com.darwinreforged.server.sponge.implementations;

import com.darwinreforged.server.core.DarwinServer;
import com.darwinreforged.server.core.files.FileManager;
import com.darwinreforged.server.core.modules.Module;

import org.spongepowered.api.Sponge;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;

public class SpongeFileManager extends FileManager {
    @Override
    public Path getDataDirectory(Object plugin) {
        Optional<Module> infoOptional;
        if (plugin instanceof Class) infoOptional = DarwinServer.getModMan().getModuleInfo((Class<?>) plugin);
        else infoOptional = DarwinServer.getModMan().getModuleInfo(plugin.getClass());

        if (infoOptional.isPresent()) {
            Path darwinDataPath = infoOptional.map(moduleInfo -> Sponge.getGame().getSavesDirectory().resolve("data/" + moduleInfo.id())).get().toAbsolutePath();
            return createPathIfNotExist(darwinDataPath);
        }
        return getConfigDirectory(plugin);
    }

    @Override
    public Path getModuleDirectory() {
        Path modDir = Sponge.getGame().getGameDirectory().resolve("modules/").toAbsolutePath();
        return createPathIfNotExist(modDir);
    }

    @Override
    public Path getConfigDirectory(Object plugin) {
        Optional<Module> infoOptional;
        if (plugin instanceof Class) infoOptional = DarwinServer.getModMan().getModuleInfo((Class<?>) plugin);
        else infoOptional = DarwinServer.getModMan().getModuleInfo(plugin.getClass());

        Path darwinConfigPath = Sponge.getConfigManager().getPluginConfig(DarwinServer.getServer()).getDirectory();

        return createPathIfNotExist(infoOptional.map(moduleInfo -> {
            if (moduleInfo.id().equals("darwinserver")) return darwinConfigPath;
            return new File(
                    darwinConfigPath.toFile().getParentFile().getParent(),
                    String.format("modules/%s", moduleInfo.id().replaceAll("\\.", "_"))).toPath();
        }).orElse(darwinConfigPath));
    }

    @Override
    public Path getLogDirectory() {
        return Sponge.getGame().getGameDirectory().resolve("logs").toAbsolutePath();
    }
}
