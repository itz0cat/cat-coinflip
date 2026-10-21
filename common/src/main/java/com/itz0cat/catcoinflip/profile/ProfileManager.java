package com.itz0cat.catcoinflip.profile;

import java.util.ArrayList;
import java.util.List;

public class ProfileManager {

    private final List<Profile> profiles = new ArrayList<>();
    private String activeProfileName = "BananaSMP";
    private String currentServerAddress = "";

    public ProfileManager() {}

    public synchronized void setProfiles(List<Profile> newProfiles) {
        profiles.clear();
        if (newProfiles != null) {
            profiles.addAll(newProfiles);
        }
    }

    public synchronized List<Profile> getProfiles() {
        return new ArrayList<>(profiles);
    }

    public synchronized Profile getProfile(String name) {
        for (Profile p : profiles) {
            if (p.getName().equalsIgnoreCase(name)) {
                return p;
            }
        }
        return null;
    }

    public synchronized Profile getActiveProfile() {
        Profile p = getProfile(activeProfileName);
        if (p == null && !profiles.isEmpty()) {
            p = profiles.get(0);
            activeProfileName = p.getName();
        }
        return p;
    }

    public synchronized void setActiveProfileName(String name) {
        if (getProfile(name) != null) {
            this.activeProfileName = name;
        }
    }

    public synchronized String getActiveProfileName() {
        return activeProfileName;
    }

    public synchronized void addProfile(Profile profile) {
        if (profile == null) return;
        removeProfile(profile.getName());
        profiles.add(profile);
    }

    public synchronized boolean removeProfile(String name) {
        if (profiles.size() <= 1) {
            // Cannot remove the last profile
            return false;
        }
        boolean removed = profiles.removeIf(p -> p.getName().equalsIgnoreCase(name));
        if (removed && activeProfileName.equalsIgnoreCase(name)) {
            activeProfileName = profiles.get(0).getName();
        }
        return removed;
    }

    public synchronized void onServerConnected(String serverAddress) {
        this.currentServerAddress = serverAddress != null ? serverAddress : "";
        if (currentServerAddress.isEmpty()) {
            return;
        }

        // Try to find matching profile
        for (Profile profile : profiles) {
            if (profile.matchesServer(currentServerAddress)) {
                this.activeProfileName = profile.getName();
                return;
            }
        }
    }

    public synchronized void onServerDisconnected() {
        this.currentServerAddress = "";
    }

    public synchronized String getCurrentServerAddress() {
        return currentServerAddress;
    }
}
