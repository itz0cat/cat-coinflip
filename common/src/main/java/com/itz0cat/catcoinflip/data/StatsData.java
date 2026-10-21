package com.itz0cat.catcoinflip.data;

import java.util.HashMap;
import java.util.Map;

public class StatsData {

    private Map<String, PlayerStats> players = new HashMap<>();
    private SessionStats session = new SessionStats();

    public Map<String, PlayerStats> getPlayers() {
        if (players == null) players = new HashMap<>();
        return players;
    }

    public void setPlayers(Map<String, PlayerStats> players) {
        this.players = players;
    }

    public SessionStats getSession() {
        if (session == null) session = new SessionStats();
        return session;
    }

    public void setSession(SessionStats session) {
        this.session = session;
    }
}
