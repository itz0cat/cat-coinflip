package com.itz0cat.catcoinflip.tooltip;

import com.itz0cat.catcoinflip.data.PlayerStats;
import com.itz0cat.catcoinflip.data.SessionStats;
import com.itz0cat.catcoinflip.profile.Profile;
import com.itz0cat.catcoinflip.profile.TooltipLineConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TooltipGeneratorTest {

    @Test
    @DisplayName("Generates lines with accurate math and formatting")
    void testTooltipGeneration() {
        Profile profile = new Profile("BananaSMP");
        profile.setHouseCut(0.05); // 5%
        profile.setCurrencySymbol("$");
        profile.setShortNumbersFrom(100_000L);

        List<TooltipLineConfig> lines = new ArrayList<>();
        lines.add(new TooltipLineConfig("amount_short", "Amount", "§e", "★", true));
        lines.add(new TooltipLineConfig("payout", "Payout", "§a", "✔", true));
        lines.add(new TooltipLineConfig("house_cut", "House Cut", "§c", "⚡", true));
        lines.add(new TooltipLineConfig("if_win", "Profit on Win", "§2", "↗", true));
        lines.add(new TooltipLineConfig("if_lose", "Loss on Fail", "§4", "↘", true));
        lines.add(new TooltipLineConfig("can_afford", "Affordable", "§b", "✔", true));
        lines.add(new TooltipLineConfig("whale_tag", "Tag", "§d", "♛", true));
        profile.setLines(lines);

        ListingData listing = new ListingData("denonking92763", 100_000_000L, 0, 10);
        PlayerStats stats = new PlayerStats("denonking92763");
        SessionStats session = new SessionStats();

        List<String> result = TooltipGenerator.generateLines(listing, stats, session, profile, 250_000_000L);

        assertFalse(result.isEmpty());
        // Verify Amount ($100M)
        assertTrue(result.stream().anyMatch(l -> l.contains("$100M")));
        // Verify Payout (1.90x ($190M))
        assertTrue(result.stream().anyMatch(l -> l.contains("1.90x") && l.contains("$190M")));
        // Verify House Cut (5.0% ($5M))
        assertTrue(result.stream().anyMatch(l -> l.contains("5.0%") && l.contains("$5M")));
        // Verify Profit on Win (+$95M)
        assertTrue(result.stream().anyMatch(l -> l.contains("+$95M")));
        // Verify High Roller tag
        assertTrue(result.stream().anyMatch(l -> l.contains("HIGH ROLLER")));
    }
}
