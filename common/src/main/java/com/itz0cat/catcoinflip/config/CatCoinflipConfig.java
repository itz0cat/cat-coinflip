package com.itz0cat.catcoinflip.config;

import com.itz0cat.catcoinflip.profile.Profile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CatCoinflipConfig {

    private int schemaVersion = 1;
    private int configVersion = 1;
    private boolean checkForUpdates = true;
    private boolean autoDownload = false;
    private String activeProfile = "BananaSMP";
    private boolean tooltipEnabled = true;

    private List<Profile> profiles = new ArrayList<>();
    private Map<String, String> customPlaceholders = new HashMap<>();

    public int getSchemaVersion() { return schemaVersion; }
    public void setSchemaVersion(int schemaVersion) { this.schemaVersion = schemaVersion; }

    public int getConfigVersion() { return configVersion; }
    public void setConfigVersion(int configVersion) { this.configVersion = configVersion; }

    public boolean isCheckForUpdates() { return checkForUpdates; }
    public void setCheckForUpdates(boolean checkForUpdates) { this.checkForUpdates = checkForUpdates; }

    public boolean isAutoDownload() { return autoDownload; }
    public void setAutoDownload(boolean autoDownload) { this.autoDownload = autoDownload; }

    public String getActiveProfile() { return activeProfile != null ? activeProfile : "BananaSMP"; }
    public void setActiveProfile(String activeProfile) { this.activeProfile = activeProfile; }

    public boolean isTooltipEnabled() { return tooltipEnabled; }
    public void setTooltipEnabled(boolean tooltipEnabled) { this.tooltipEnabled = tooltipEnabled; }

    public List<Profile> getProfiles() {
        if (profiles == null) profiles = new ArrayList<>();
        return profiles;
    }
    public void setProfiles(List<Profile> profiles) { this.profiles = profiles; }

    public Map<String, String> getCustomPlaceholders() {
        if (customPlaceholders == null) customPlaceholders = new HashMap<>();
        return customPlaceholders;
    }
    public void setCustomPlaceholders(Map<String, String> customPlaceholders) { this.customPlaceholders = customPlaceholders; }
}
