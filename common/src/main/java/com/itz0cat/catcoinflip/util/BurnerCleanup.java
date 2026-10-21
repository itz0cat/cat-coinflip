package com.itz0cat.catcoinflip.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * Handles locked-file replacement, rollback, and cleanup of old mod JARs across game restarts.
 * Handles Windows JVM file locks via a detached background process waiting on JVM PID.
 */
public final class BurnerCleanup {

    private BurnerCleanup() {}

    public static boolean isWindows() {
        String os = System.getProperty("os.name", "");
        return os.toLowerCase(Locale.ROOT).contains("win");
    }

    @SuppressWarnings("unchecked")
    public static List<Path> getModJarPaths() {
        Set<Path> paths = new LinkedHashSet<>();

        // 1. FabricLoader API via reflection (safe for common module & unit tests)
        try {
            Class<?> flClass = Class.forName("net.fabricmc.loader.api.FabricLoader");
            Object flInstance = flClass.getMethod("getInstance").invoke(null);
            Optional<?> opt = (Optional<?>) flClass.getMethod("getModContainer", String.class).invoke(flInstance, "catcoinflip");
            if (opt != null && opt.isPresent()) {
                Object modContainer = opt.get();
                Object origin = modContainer.getClass().getMethod("getOrigin").invoke(modContainer);
                List<Path> pList = (List<Path>) origin.getClass().getMethod("getPaths").invoke(origin);
                if (pList != null) {
                    for (Path p : pList) {
                        if (p != null && Files.exists(p)) {
                            paths.add(p.toAbsolutePath());
                        }
                    }
                }
            }
        } catch (Throwable ignored) {}

        // 2. Scan mods folder
        Path modsDir = getModsFolderPath();
        if (modsDir != null && Files.isDirectory(modsDir)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(modsDir)) {
                for (Path entry : stream) {
                    String name = entry.getFileName().toString().toLowerCase(Locale.ROOT);
                    if (name.contains("cat-coinflip") || name.contains("catcoinflip")) {
                        if (name.endsWith(".jar")) {
                            paths.add(entry.toAbsolutePath());
                        }
                    }
                }
            } catch (Throwable ignored) {}
        }

        return new ArrayList<>(paths);
    }

    public static Path getModsFolderPath() {
        Path relMods = Paths.get("mods");
        if (Files.isDirectory(relMods)) {
            return relMods.toAbsolutePath();
        }
        return null;
    }

    public static long getCurrentPid() {
        try {
            return ProcessHandle.current().pid();
        } catch (Throwable ignored) {
            return -1;
        }
    }

    /**
     * Schedules replacing or deleting old mod JARs.
     * If newJarToInstall is provided, the background task replaces the old JAR with the new one.
     */
    public static void scheduleJarReplacementOrDeletion(Path oldJar, Path newJarToInstall, boolean backupOld) {
        if (oldJar == null && newJarToInstall == null) return;

        if (backupOld && oldJar != null && Files.exists(oldJar)) {
            try {
                Path backup = oldJar.resolveSibling(oldJar.getFileName().toString() + ".backup");
                Files.copy(oldJar, backup, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            } catch (Throwable ignored) {}
        }

        if (isWindows()) {
            scheduleWindowsSwap(oldJar, newJarToInstall);
        } else {
            directSwap(oldJar, newJarToInstall);
        }
    }

    private static void directSwap(Path oldJar, Path newJarToInstall) {
        try {
            if (oldJar != null) {
                Files.deleteIfExists(oldJar);
            }
            if (newJarToInstall != null && Files.exists(newJarToInstall) && oldJar != null) {
                Files.move(newJarToInstall, oldJar, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Throwable ignored) {}
    }

    private static void scheduleWindowsSwap(Path oldJar, Path newJarToInstall) {
        try {
            StringBuilder bat = new StringBuilder();
            long pid = getCurrentPid();

            bat.append("@echo off\r\n");
            bat.append("setlocal enabledelayedexpansion\r\n");

            if (pid > 0) {
                bat.append("set ATTEMPTS=0\r\n");
                bat.append(":wait_pid\r\n");
                bat.append("tasklist /FI \"PID eq ").append(pid).append("\" 2>nul | find \"").append(pid).append("\" >nul\r\n");
                bat.append("if not errorlevel 1 (\r\n");
                bat.append("    set /a ATTEMPTS+=1\r\n");
                bat.append("    if !ATTEMPTS! leq 30 (\r\n");
                bat.append("        ping -n 2 127.0.0.1 >nul\r\n");
                bat.append("        goto wait_pid\r\n");
                bat.append("    )\r\n");
                bat.append(")\r\n");
            }

            bat.append("ping -n 3 127.0.0.1 >nul\r\n");

            if (oldJar != null) {
                String oldAbs = oldJar.toAbsolutePath().toString();
                bat.append("for /L %%i in (1,1,5) do (\r\n");
                bat.append("    if exist \"").append(oldAbs).append("\" (\r\n");
                bat.append("        del /f /q /a \"").append(oldAbs).append("\" 2>nul\r\n");
                bat.append("        ping -n 2 127.0.0.1 >nul\r\n");
                bat.append("    )\r\n");
                bat.append(")\r\n");
            }

            if (newJarToInstall != null && oldJar != null) {
                String newAbs = newJarToInstall.toAbsolutePath().toString();
                String targetAbs = oldJar.toAbsolutePath().toString();
                bat.append("if exist \"").append(newAbs).append("\" (\r\n");
                bat.append("    move /y \"").append(newAbs).append("\" \"").append(targetAbs).append("\" >nul 2>&1\r\n");
                bat.append(")\r\n");
            }

            bat.append("del /f /q /a \"%~f0\" 2>nul\r\n");

            Path batFile = Files.createTempFile("catcf-update-", ".bat");
            Files.writeString(batFile, bat.toString(), StandardCharsets.UTF_8);

            new ProcessBuilder(
                    "cmd.exe", "/c", "start", "\"Cat Coinflip Update\"",
                    "/min", batFile.toAbsolutePath().toString()
            ).redirectErrorStream(true).start();

        } catch (Throwable ignored) {}
    }
}
