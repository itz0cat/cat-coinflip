package com.itz0cat.catcoinflip.updater;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.itz0cat.catcoinflip.compat.PlatformCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.concurrent.CompletableFuture;

public class AutoUpdater {

    private static final String RELEASES_API = "https://api.github.com/repos/itz0cat/cat-coinflip/releases/latest";
    private static final int TIMEOUT_MS = 8000;

    private final String currentModVersion;
    private final String minecraftVersion;
    private final PlatformCompat compat;

    private String updateStatus = "Idle";
    private String latestReleaseTag = null;
    private String downloadJarUrl = null;
    private String checksumsUrl = null;
    private boolean updateAvailable = false;

    public AutoUpdater(String currentModVersion, String minecraftVersion, PlatformCompat compat) {
        this.currentModVersion = currentModVersion;
        this.minecraftVersion = minecraftVersion;
        this.compat = compat;
    }

    public synchronized boolean isUpdateAvailable() { return updateAvailable; }
    public synchronized String getUpdateStatus() { return updateStatus; }
    public synchronized String getLatestReleaseTag() { return latestReleaseTag; }

    public CompletableFuture<Boolean> checkForUpdatesAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                updateStatus = "Checking for updates...";
                HttpURLConnection conn = (HttpURLConnection) URI.create(RELEASES_API).toURL().openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(TIMEOUT_MS);
                conn.setReadTimeout(TIMEOUT_MS);
                conn.setRequestProperty("User-Agent", "CatCoinflip-Mod");
                conn.setRequestProperty("Accept", "application/vnd.github.v3+json");

                int code = conn.getResponseCode();
                if (code == 200) {
                    try (InputStream is = conn.getInputStream()) {
                        String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                        JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
                        latestReleaseTag = obj.get("tag_name").getAsString();

                        JsonArray assets = obj.getAsJsonArray("assets");
                        String targetJarName = "+mc" + minecraftVersion + ".jar";

                        downloadJarUrl = null;
                        checksumsUrl = null;

                        for (JsonElement el : assets) {
                            JsonObject asset = el.getAsJsonObject();
                            String name = asset.get("name").getAsString();
                            String url = asset.get("browser_download_url").getAsString();

                            if (name.contains(targetJarName) && !name.contains("-sources") && !name.contains("-dev")) {
                                downloadJarUrl = url;
                            }
                            if (name.equalsIgnoreCase("checksums.txt")) {
                                checksumsUrl = url;
                            }
                        }

                        // Compare tags / versions
                        String cleanTag = latestReleaseTag.replaceAll("^v", "");
                        if (!cleanTag.equalsIgnoreCase(currentModVersion) && downloadJarUrl != null) {
                            updateAvailable = true;
                            updateStatus = "Update " + latestReleaseTag + " available for MC " + minecraftVersion + "!";
                            if (compat != null) {
                                compat.sendClientChatMessage("§6[CatCoinflip] §aA new version is available: §f" + latestReleaseTag + "§a. Open §f/catcf§a or Cloth Config to update.");
                            }
                            return true;
                        } else {
                            updateStatus = "Mod is up to date (v" + currentModVersion + ")";
                            return false;
                        }
                    }
                } else {
                    updateStatus = "GitHub API returned " + code;
                }
            } catch (Exception e) {
                updateStatus = "Update check failed: " + e.getMessage();
            }
            return false;
        });
    }

    public CompletableFuture<Boolean> performUpdateAsync() {
        return CompletableFuture.supplyAsync(() -> {
            if (downloadJarUrl == null || !updateAvailable) {
                updateStatus = "No update to download";
                return false;
            }

            try {
                updateStatus = "Downloading update jar...";
                Path tempDir = Files.createTempDirectory("catcoinflip-update");
                File tempJar = tempDir.resolve("catcoinflip-update.jar").toFile();

                // 1. Download jar
                downloadFile(downloadJarUrl, tempJar);

                // 2. Fetch checksums.txt if available and verify
                if (checksumsUrl != null) {
                    updateStatus = "Verifying SHA-256...";
                    File tempChecksums = tempDir.resolve("checksums.txt").toFile();
                    downloadFile(checksumsUrl, tempChecksums);
                    String expectedHash = findExpectedHash(tempChecksums, tempJar.getName());

                    if (expectedHash != null) {
                        String actualHash = calculateSha256(tempJar);
                        if (!actualHash.equalsIgnoreCase(expectedHash)) {
                            updateStatus = "Verification failed! SHA-256 mismatch.";
                            tempJar.delete();
                            return false;
                        }
                    }
                }

                // 3. Stage update in config/catcoinflip/update/
                if (compat != null) {
                    Path configDir = compat.getConfigDirectory();
                    Path updateDir = configDir.resolve("catcoinflip").resolve("update");
                    Files.createDirectories(updateDir);
                    Path stagedTarget = updateDir.resolve("catcoinflip-" + latestReleaseTag + "+mc" + minecraftVersion + ".jar");
                    Files.copy(tempJar.toPath(), stagedTarget, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

                    updateStatus = "Staged for next game launch: " + stagedTarget.getFileName();
                    compat.sendClientChatMessage("§6[CatCoinflip] §aUpdate downloaded successfully! Restart Minecraft to apply.");
                    return true;
                }
            } catch (Exception e) {
                updateStatus = "Download error: " + e.getMessage();
            }
            return false;
        });
    }

    private void downloadFile(String urlStr, File dest) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) URI.create(urlStr).toURL().openConnection();
        conn.setConnectTimeout(TIMEOUT_MS);
        conn.setReadTimeout(15000);
        conn.setRequestProperty("User-Agent", "CatCoinflip-Mod");
        try (InputStream in = conn.getInputStream(); FileOutputStream out = new FileOutputStream(dest)) {
            in.transferTo(out);
        }
    }

    private String calculateSha256(File file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream is = Files.newInputStream(file.toPath())) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
        }
        byte[] hash = digest.digest();
        StringBuilder hex = new StringBuilder();
        for (byte b : hash) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }

    private String findExpectedHash(File checksumsFile, String jarName) {
        try {
            for (String line : Files.readAllLines(checksumsFile.toPath())) {
                String[] parts = line.trim().split("\\s+");
                if (parts.length >= 2) {
                    String hash = parts[0];
                    String file = parts[1];
                    if (file.contains(minecraftVersion)) {
                        return hash;
                    }
                }
            }
        } catch (Exception ignored) {}
        return null;
    }
}
