package com.itz0cat.catcoinflip.data;

import com.itz0cat.catcoinflip.profile.Profile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ChatParserTest {

    private StatsManager statsManager;
    private ChatParser chatParser;
    private Profile profile;

    @BeforeEach
    void setUp() {
        statsManager = new StatsManager();
        chatParser = new ChatParser(statsManager);
        profile = new Profile("BananaSMP");
        profile.setHouseCut(0.05); // 5%
    }

    @Test
    @DisplayName("Parses BananaSMP multi-line game summary correctly")
    void testGameSummaryParsing() {
        // Multi-line summary from real server log
        chatParser.onChatMessage("GAME SUMMARY", profile, "x106_");
        chatParser.onChatMessage("LOSER", profile, "x106_");
        chatParser.onChatMessage("● Amount Bet: $1,900", profile, "x106_");
        chatParser.onChatMessage("● Winner: denonking92763", profile, "x106_");
        chatParser.onChatMessage("● Loser: x106_", profile, "x106_");

        PlayerStats winner = statsManager.getOrCreatePlayerStats("denonking92763");
        assertEquals(1, winner.getWins());
        assertEquals(0, winner.getLosses());
        assertEquals(1000L, winner.getTotalWagered());
        assertEquals(900L, winner.getNetProfit());
        assertEquals("WIN", winner.getLastResult());

        // Check head to head
        assertEquals(1, winner.getVsWins()); // Against local player x106_

        PlayerStats loser = statsManager.getOrCreatePlayerStats("x106_");
        assertEquals(0, loser.getWins());
        assertEquals(1, loser.getLosses());
        assertEquals(-1000L, loser.getNetProfit());

        // Check session stats for local user
        SessionStats session = statsManager.getData().getSession();
        assertEquals(0, session.getWins());
        assertEquals(1, session.getLosses());
        assertEquals(-1000L, session.getNetProfit());
    }

    @Test
    @DisplayName("Parses balance updates and updates session balance")
    void testBalanceParsing() {
        chatParser.onChatMessage(" Your Balance: $157,489,660.", profile, "x106_");
        assertEquals(157489660L, statsManager.getData().getSession().getLastUserBalance());
    }

    @Test
    @DisplayName("Ignores shards creation messages and parses money creation")
    void testCreationParsing() {
        // Shards should be ignored
        chatParser.onChatMessage("COINFLIP ▶  Created Coinflip game for 100 (Shards)!", profile, "x106_");
        // Money should be accepted
        chatParser.onChatMessage("COINFLIP ▶  Created Coinflip game for 1,000 (Money)!", profile, "x106_");
    }
}
