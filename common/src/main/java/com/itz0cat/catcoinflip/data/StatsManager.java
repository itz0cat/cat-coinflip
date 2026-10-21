package com.itz0cat.catcoinflip.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Path;
import java.util.Map;

public class StatsManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private StatsData data = new StatsData();
    private Path configDir;

    public StatsManager() {}

    public synchronized void init(Path configDir) {
        this.configDir = configDir;
        load();
    }

    public synchronized StatsData getData() {
        if (data == null) data = new StatsData();
        return data;
    }

    public synchronized PlayerStats getOrCreatePlayerStats(String playerName) {
        if (playerName == null || playerName.isEmpty()) {
            return new PlayerStats("Unknown");
        }
        Map<String, PlayerStats> players = getData().getPlayers();
        PlayerStats stats = players.get(playerName.toLowerCase());
        if (stats == null) {
            stats = new PlayerStats(playerName);
            players.put(playerName.toLowerCase(), stats);
        }
        return stats;
    }

    public synchronized void recordGameSummary(String winner, String loser, long potAmount, double houseCut, String localPlayerName) {
        // Full pot was shown in message (e.g. $190 or $1,900)
        // Base listing bet = pot / (1.0 + (1.0 - houseCut))
        // Winner net profit = pot - baseBet
        double multiplier = 1.0 + (1.0 - houseCut);
        long baseBet = (multiplier > 0) ? Math.round(potAmount / multiplier) : (potAmount / 2);
        long winnerNet = potAmount - baseBet;

        boolean localWon = (localPlayerName != null && localPlayerName.equalsIgnoreCase(winner));
        boolean localLost = (localPlayerName != null && localPlayerName.equalsIgnoreCase(loser));

        // 1. Update winner's stats
        PlayerStats winnerStats = getOrCreatePlayerStats(winner);
        winnerStats.recordGame(true, baseBet, winnerNet, localLost);

        // 2. Update loser's stats
        PlayerStats loserStats = getOrCreatePlayerStats(loser);
        loserStats.recordGame(false, baseBet, winnerNet, localWon);

        // 3. Update local session stats if local player was in the game
        if (localWon) {
            getData().getSession().recordResult(true, baseBet, winnerNet);
        } else if (localLost) {
            getData().getSession().recordResult(false, baseBet, baseBet);
        }

        save();
    }

    public synchronized void updateUserBalance(long balance) {
        getData().getSession().setLastUserBalance(balance);
        save();
    }

    public synchronized void onCoinflipCreated(long amount) {
        // Can be used to track pending user listings
    }

    public synchronized void resetSession() {
        getData().getSession().reset();
        save();
    }

    public synchronized void resetAll() {
        this.data = new StatsData();
        save();
    }

    public synchronized void load() {
        if (configDir == null) return;
        File file = configDir.resolve("catcoinflip").resolve("stats.json").toFile();
        if (!file.exists()) {
            this.data = new StatsData();
            return;
        }

        try (FileReader reader = new FileReader(file)) {
            this.data = GSON.fromJson(reader, StatsData.class);
            if (this.data == null) {
                this.data = new StatsData();
            }
        } catch (Exception e) {
            this.data = new StatsData();
        }
    }

    public synchronized void save() {
        if (configDir == null) return;
        try {
            File dir = configDir.resolve("catcoinflip").toFile();
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(dir, "stats.json");
            try (FileWriter writer = new FileWriter(file)) {
                GSON.toJson(this.data, writer);
            }
        } catch (Exception ignored) {}
    }
}
