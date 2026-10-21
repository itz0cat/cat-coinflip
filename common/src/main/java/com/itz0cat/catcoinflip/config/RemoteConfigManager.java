package com.itz0cat.catcoinflip.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.itz0cat.catcoinflip.profile.Profile;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class RemoteConfigManager {

    private static final String REMOTE_URL = "https://raw.githubusercontent.com/itz0cat/cat-coinflip/main/config/defaults.json";
    private static final int TIMEOUT_MS = 6000;
    private static final int MAX_SIZE_BYTES = 1024 * 1024; // 1MB cap
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path configDir;
    private CatCoinflipConfig cachedRemoteConfig;
    private String statusMessage = "Up to date";
    private final List<String> detectedChanges = new ArrayList<>();

    public RemoteConfigManager(Path configDir) {
        this.configDir = configDir;
        loadCached();
    }

    public synchronized String getStatusMessage() {
        return statusMessage;
    }

    public synchronized List<String> getDetectedChanges() {
        return new ArrayList<>(detectedChanges);
    }

    public CompletableFuture<Boolean> checkRemoteConfig(CatCoinflipConfig currentConfig) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpURLConnection conn = (HttpURLConnection) URI.create(REMOTE_URL).toURL().openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(TIMEOUT_MS);
                conn.setReadTimeout(TIMEOUT_MS);
                conn.setRequestProperty("User-Agent", "CatCoinflip-Mod");

                int code = conn.getResponseCode();
                if (code == 200) {
                    int length = conn.getContentLength();
                    if (length > MAX_SIZE_BYTES) {
                        statusMessage = "Remote config exceeds max allowed size";
                        return false;
                    }

                    try (InputStream is = conn.getInputStream();
                         InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                        CatCoinflipConfig remote = GSON.fromJson(reader, CatCoinflipConfig.class);
                        if (remote != null && remote.getSchemaVersion() >= 1) {
                            processRemoteUpdate(remote, currentConfig);
                            saveCache(remote);
                            statusMessage = "Remote config verified successfully";
                            return true;
                        }
                    }
                } else {
                    statusMessage = "Server returned HTTP " + code;
                }
            } catch (Exception e) {
                statusMessage = "Offline / fetch error: " + e.getMessage();
            }
            return false;
        });
    }

    private synchronized void processRemoteUpdate(CatCoinflipConfig remote, CatCoinflipConfig local) {
        this.cachedRemoteConfig = remote;
        detectedChanges.clear();

        if (remote.getConfigVersion() > local.getConfigVersion()) {
            detectedChanges.add("New config version available: v" + remote.getConfigVersion() + " (current: v" + local.getConfigVersion() + ")");
        }

        // Check for new profiles in remote
        for (Profile rp : remote.getProfiles()) {
            boolean found = false;
            for (Profile lp : local.getProfiles()) {
                if (lp.getName().equalsIgnoreCase(rp.getName())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                detectedChanges.add("New server profile available: " + rp.getName());
            }
        }
    }

    public synchronized void adoptRemoteProfile(String profileName, CatCoinflipConfig local) {
        if (cachedRemoteConfig == null) return;
        for (Profile rp : cachedRemoteConfig.getProfiles()) {
            if (rp.getName().equalsIgnoreCase(profileName)) {
                local.getProfiles().removeIf(p -> p.getName().equalsIgnoreCase(profileName));
                local.getProfiles().add(rp.copy(profileName));
                ConfigManager.save();
                break;
            }
        }
    }

    public synchronized void resetToRemoteDefaults(CatCoinflipConfig local) {
        if (cachedRemoteConfig != null) {
            local.setProfiles(new ArrayList<>(cachedRemoteConfig.getProfiles()));
            local.setConfigVersion(cachedRemoteConfig.getConfigVersion());
            ConfigManager.save();
        } else {
            ConfigManager.loadBundledDefaults();
        }
    }

    private void loadCached() {
        if (configDir == null) return;
        File cacheFile = configDir.resolve("catcoinflip").resolve("cache").resolve("defaults.json").toFile();
        if (cacheFile.exists()) {
            try (FileReader reader = new FileReader(cacheFile)) {
                this.cachedRemoteConfig = GSON.fromJson(reader, CatCoinflipConfig.class);
            } catch (Exception ignored) {}
        }
    }

    private void saveCache(CatCoinflipConfig remote) {
        if (configDir == null || remote == null) return;
        try {
            File cacheDir = configDir.resolve("catcoinflip").resolve("cache").toFile();
            if (!cacheDir.exists()) cacheDir.mkdirs();
            File cacheFile = new File(cacheDir, "defaults.json");
            try (FileWriter writer = new FileWriter(cacheFile)) {
                GSON.toJson(remote, writer);
            }
        } catch (Exception ignored) {}
    }
}
