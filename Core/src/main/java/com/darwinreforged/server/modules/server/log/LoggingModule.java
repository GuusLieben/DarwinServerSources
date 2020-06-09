package com.darwinreforged.server.modules.server.log;

import com.darwinreforged.server.core.DarwinServer;
import com.darwinreforged.server.core.events.internal.server.ServerReloadEvent;
import com.darwinreforged.server.core.events.internal.server.ServerStartedEvent;
import com.darwinreforged.server.core.events.util.Listener;
import com.darwinreforged.server.core.files.FileManager;
import com.darwinreforged.server.core.modules.Module;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.time.Month;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Module(id = "logarchival", name = "Log Archival", description = "Automatically move logs to a sorted archive", authors = {"dags-", "GuusLieben"}, url = "https://github.com/dags-/Archivist")
public class LoggingModule {

    private final PathMatcher filter = FileSystems.getDefault().getPathMatcher("glob:*.log.gz");
    private final Pattern datePattern = Pattern.compile("((\\d{4})-(\\d{2})-\\d{2})");
    private final Pattern namePattern = Pattern.compile("(.*?)(-(\\d+))?.log.gz");
    private final Logger logger = LoggerFactory.getLogger("logarchival");
    private Path logs;

    private FileManager fileUtil;
    private final List<String> unloadBlacklist = new ArrayList<>();

    public LoggingModule() {
    }

    @Listener
    public void onReload(ServerReloadEvent event) {
        logs = DarwinServer.getUtilMan().get(FileManager.class).getLogDirectory();
        this.run();
    }

    @Listener
    public void onServerStart(ServerStartedEvent event) {
        onReload(null);
    }

    private void run() {
        try {
            DarwinServer.getLog().info("Checking for logs to archive in {}", logs);
            Files.list(logs).filter(p -> filter.matches(p.getFileName())).forEach(this::archive);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void archive(Path source) {
        Path directory = getArchiveDirectory(source);
        if (directory == null) {
            DarwinServer.getLog().warn("Unable to determine date of file {}", source);
            return;
        }

        Path destination = getArchiveName(directory, source);
        if (destination == null) {
            DarwinServer.getLog().warn("Unable to resolve archive name for file {}", source);
            return;
        }

        if (!mkdirs(directory)) {
            DarwinServer.getLog().warn("Unable to create directory {}", directory);
            return;
        }

        if (!move(source, destination)) {
            DarwinServer.getLog().warn("Unable to move file {} to {}", source, destination);
            return;
        }

        DarwinServer.getLog().info("Archived file {} to {}", source, destination);
    }

    private Path getArchiveDirectory(Path file) {
        Matcher dateMatcher = datePattern.matcher(file.getFileName().toString());

        String year;
        String month;
        if (dateMatcher.find()) {
            year = dateMatcher.group(2);
            month = dateMatcher.group(3);
        } else {
            try {
                FileTime time = Files.getLastModifiedTime(file);
                Date date = new Date(time.toMillis());
                year = new SimpleDateFormat("yyyy").format(date);
                month = new SimpleDateFormat("MM").format(date);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        String folder = String.format("%s-%s", month, Month.of(Integer.parseInt(month)));

        return logs.resolve(year).resolve(folder);
    }

    private Path getArchiveName(Path dir, Path file) {
        Matcher nameMatcher = namePattern.matcher(file.getFileName().toString());
        if (nameMatcher.find()) {
            String name = nameMatcher.group(1);
            String nameFormat = "%s__%03d.log.gz";
            String filename = String.format(nameFormat, name, 0);

            Path destination = dir.resolve(filename);
            for (int i = 1; Files.exists(destination); i++) {
                filename = String.format(nameFormat, name, i);
                destination = dir.resolve(filename);
            }

            return destination;
        }
        return null;
    }

    private static boolean mkdirs(Path path) {
        if (Files.exists(path)) {
            return true;
        }

        try {
            Files.createDirectories(path);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static boolean move(Path src, Path dst) {
        try {
            Files.move(src, dst, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

}
