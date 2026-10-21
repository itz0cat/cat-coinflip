package com.itz0cat.catcoinflip.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.itz0cat.catcoinflip.profile.Profile;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class ConfigManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static CatCoinflipConfig config = new CatCoinflipConfig();
    private static Path configDirectory;
    private static RemoteConfigManager remoteManager;

    public static synchronized void init(Path configDir) {
        configDirectory = configDir;
        remoteManager = new RemoteConfigManager(configDir);
        load();

        // Fetch remote config asynchronously on launch
        CompletableFuture.runAsync(() -> {
            try {
                remoteManager.checkRemoteConfig(config);
            } catch (Exception ignored) {}
        });
    }

    public static synchronized CatCoinflipConfig getConfig() {
        if (config == null) config = new CatCoinflipConfig();
        return config;
    }

    public static synchronized RemoteConfigManager getRemoteManager() {
        return remoteManager;
    }

    public static synchronized void load() {
        if (configDirectory == null) return;
        File file = configDirectory.resolve("catcoinflip").resolve("config.json").toFile();
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                config = GSON.fromJson(reader, CatCoinflipConfig.class);
            } catch (Exception ignored) {}
        }

        // If no config or no profiles, load bundled defaults
        if (config == null || config.getProfiles().isEmpty()) {
            loadBundledDefaults();
        }
    }

    public static synchronized void loadBundledDefaults() {
        try (InputStream is = ConfigManager.class.getResourceAsStream("/assets/catcoinflip/defaults.json")) {
            if (is != null) {
                try (InputStreamReader isr = new InputStreamReader(is)) {
                    config = GSON.fromJson(isr, CatCoinflipConfig.class);
                }
            }
        } catch (Exception ignored) {}

        if (config == null) {
            config = new CatCoinflipConfig();
            config.getProfiles().add(new Profile("BananaSMP"));
        }
        save();
    }

    public static synchronized void save() {
        if (configDirectory == null || config == null) return;
        try {
            File dir = configDirectory.resolve("catcoinflip").toFile();
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(dir, "config.json");
            try (FileWriter writer = new FileWriter(file)) {
                GSON.toJson(config, writer);
            }
        } catch (Exception ignored) {}
    }
}
