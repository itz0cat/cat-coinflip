package com.itz0cat.catcoinflip;

import com.itz0cat.catcoinflip.command.CommandRewriteResult;
import com.itz0cat.catcoinflip.command.QuickCreateHandler;
import com.itz0cat.catcoinflip.compat.PlatformCompat;
import com.itz0cat.catcoinflip.config.CatCoinflipConfig;
import com.itz0cat.catcoinflip.config.ConfigManager;
import com.itz0cat.catcoinflip.data.ChatParser;
import com.itz0cat.catcoinflip.data.StatsManager;
import com.itz0cat.catcoinflip.profile.Profile;
import com.itz0cat.catcoinflip.profile.ProfileManager;
import com.itz0cat.catcoinflip.updater.AutoUpdater;

import java.nio.file.Path;

public class CatCoinflip {

    public static final String MOD_ID = "catcoinflip";
    public static final String MOD_NAME = "Cat Coinflip";
    public static final String VERSION = "1.0.0";

    private static final CatCoinflip INSTANCE = new CatCoinflip();

    private PlatformCompat compat;
    private final ProfileManager profileManager = new ProfileManager();
    private final StatsManager statsManager = new StatsManager();
    private final ChatParser chatParser = new ChatParser(statsManager);
    private final QuickCreateHandler quickCreateHandler = new QuickCreateHandler();
    private AutoUpdater autoUpdater;

    private CatCoinflip() {}

    public static CatCoinflip getInstance() {
        return INSTANCE;
    }

    public synchronized void init(PlatformCompat compat) {
        this.compat = compat;
        Path configDir = compat.getConfigDirectory();

        // 1. Initialize configuration
        ConfigManager.init(configDir);
        CatCoinflipConfig config = ConfigManager.getConfig();
        profileManager.setProfiles(config.getProfiles());
        profileManager.setActiveProfileName(config.getActiveProfile());

        // 2. Initialize local stats storage
        statsManager.init(configDir);

        // 3. Initialize updater
        this.autoUpdater = new AutoUpdater(VERSION, compat.getMinecraftVersion(), compat);
        if (config.isCheckForUpdates()) {
            autoUpdater.checkForUpdatesAsync();
        }
    }

    public PlatformCompat getCompat() { return compat; }
    public ProfileManager getProfileManager() { return profileManager; }
    public StatsManager getStatsManager() { return statsManager; }
    public QuickCreateHandler getQuickCreateHandler() { return quickCreateHandler; }
    public AutoUpdater getAutoUpdater() { return autoUpdater; }

    public void onChatMessage(String rawMessage, String localPlayerName) {
        Profile active = profileManager.getActiveProfile();
        chatParser.onChatMessage(rawMessage, active, localPlayerName);
    }

    /**
     * Intercepts outgoing client command.
     * @return CommandRewriteResult containing ACTION (UNMODIFIED, REWRITTEN, BLOCKED) and command string.
     */
    public CommandRewriteResult processOutgoingCommand(String command) {
        Profile active = profileManager.getActiveProfile();
        return quickCreateHandler.processCommand(command, active, compat);
    }

    public void onServerJoin(String address) {
        profileManager.onServerConnected(address);
    }

    public void onServerDisconnect() {
        profileManager.onServerDisconnected();
    }
}
