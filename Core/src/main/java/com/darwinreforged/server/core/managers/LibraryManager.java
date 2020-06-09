package com.darwinreforged.server.core.managers;

import com.darwinreforged.server.core.DarwinServer;
import com.darwinreforged.server.core.internal.ServerDependencies;
import com.darwinreforged.server.core.internal.inject.JarFileClassLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class LibraryManager {

    private final JarFileClassLoader externalJarClassLoader = new JarFileClassLoader("Scheduler CL" + System.currentTimeMillis(), new URL[0]);

    public void checkLibraries() {
        AtomicBoolean performRestart = new AtomicBoolean(false);
        System.out.println("Checking");
        Arrays.stream(ServerDependencies.values()).forEach(serverDependency -> {
            if (checkOrDownloadLibrary(serverDependency.getBasePackage(), serverDependency.getUrl(), serverDependency.getVersion(), serverDependency.getBaseFile()))
                performRestart.set(true);
        });
        if (performRestart.get()) DarwinServer.getServer().stopServer("Downloaded libraries");
    }

    void injectJarToClassPath(File file, Consumer<Class<?>> consumer) throws IOException, ClassNotFoundException {
        if (file != null && file.exists() && file.getName().endsWith(".jar")) {
            JarFile jarFile = new JarFile(file);
            Enumeration<JarEntry> entries = jarFile.entries();

            this.externalJarClassLoader.addURL(file.toURI().toURL());
            boolean updateContext = false;

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".class")) {
                    Class<?> clazz;
                    String className = entry.getName().replace("/", ".").replace(".class", "");

                    try {
                        clazz = this.externalJarClassLoader.loadClass(className);
                    } catch (ClassNotFoundException e) {
                        clazz = Class.forName(className, true, this.externalJarClassLoader);
                    }

                    if (clazz != null) {
                        DarwinServer.getLog().info("Injected '{}' into the classpath", clazz.toGenericString());
                        if (consumer != null) consumer.accept(clazz);
                        updateContext = true;
                    } else {
                        DarwinServer.error("Failed to load class '" + className + "'");
                    }
                }
            }

            if (updateContext) Thread.currentThread().setContextClassLoader(this.externalJarClassLoader);
        }
    }

    protected boolean checkOrDownloadLibrary(String pkg, String url, String version, String fileName) {
        if (Package.getPackage(pkg) == null) {
            try {
                File packageDir = new File(DarwinServer.getServer().getLibraryDirectory(), String.format("%s/%s", pkg.replaceAll("\\.", "/"), version));
                File file = new File(packageDir, fileName);
                if (!file.exists()) {
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                    URL remoteUrl = new URL(url);
                    InputStream inputStream = remoteUrl.openStream();
                    Files.copy(inputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    return true;
                }
                externalJarClassLoader.addURL(file.toURI().toURL());
                Thread.currentThread().setContextClassLoader(externalJarClassLoader);
            } catch (IOException e) {
                DarwinServer.error(String.format("Failed to download library '%s' version %s from '%s'", fileName, version, url), e);
                DarwinServer.getServer().stopServer(String.format("Failed to download library '%s' version %s from '%s'", fileName, version, url));
            }
        }
        return false;
    }

}
