package com.itz0cat.catcoinflip.data;

import com.itz0cat.catcoinflip.profile.Profile;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatParser {

    private final StatsManager statsManager;

    // Multi-line Game Summary state
    private boolean inGameSummary = false;
    private long summaryPot = 0;
    private String summaryWinner = null;
    private String summaryLoser = null;
    private long lastSummaryLineTime = 0;

    public ChatParser(StatsManager statsManager) {
        this.statsManager = statsManager;
    }

    public synchronized void onChatMessage(String rawMessage, Profile profile, String localPlayerName) {
        if (rawMessage == null || rawMessage.trim().isEmpty() || profile == null) {
            return;
        }

        // Clean formatting codes (§x, §a, etc.)
        String clean = rawMessage.replaceAll("(?i)§[0-9a-fklmnor]", "").trim();

        // 1. Balance update check
        Pattern balancePattern = Pattern.compile("(?i)Your Balance:\\s*\\$([0-9,]+(?:\\.[0-9]{1,2})?)");
        Matcher balMatcher = balancePattern.matcher(clean);
        if (balMatcher.find()) {
            String balDigits = balMatcher.group(1).replace(",", "");
            try {
                double parsed = Double.parseDouble(balDigits);
                statsManager.updateUserBalance((long) parsed);
            } catch (Exception ignored) {}
            return;
        }

        // 2. Coinflip created (Money only, ignore Shards)
        if (clean.contains("COINFLIP") && clean.contains("Created Coinflip game")) {
            if (clean.toLowerCase().contains("(shards)")) {
                // Ignore shards
                return;
            }
            Pattern createPattern = Pattern.compile("(?i)Created Coinflip game for ([0-9,]+)");
            Matcher cm = createPattern.matcher(clean);
            if (cm.find()) {
                String amtStr = cm.group(1).replace(",", "");
                try {
                    long amt = Long.parseLong(amtStr);
                    statsManager.onCoinflipCreated(amt);
                } catch (Exception ignored) {}
            }
            return;
        }

        // 3. Multi-line Game Summary
        long now = System.currentTimeMillis();
        if (inGameSummary && (now - lastSummaryLineTime > 2000)) {
            // Summary timed out without completing
            resetSummaryState();
        }

        if (clean.contains("GAME SUMMARY")) {
            inGameSummary = true;
            summaryPot = 0;
            summaryWinner = null;
            summaryLoser = null;
            lastSummaryLineTime = now;
            return;
        }

        if (inGameSummary) {
            lastSummaryLineTime = now;

            // Check Amount Bet
            Pattern amountPattern = Pattern.compile("(?i)●?\\s*Amount(?: Bet)?:\\s*\\$?([0-9,]+)");
            Matcher am = amountPattern.matcher(clean);
            if (am.find()) {
                try {
                    summaryPot = Long.parseLong(am.group(1).replace(",", ""));
                } catch (Exception ignored) {}
            }

            // Check Winner
            Pattern winnerPattern = Pattern.compile("(?i)●?\\s*Winner:\\s*([a-zA-Z0-9_]{3,16})");
            Matcher wm = winnerPattern.matcher(clean);
            if (wm.find()) {
                summaryWinner = wm.group(1);
            }

            // Check Loser
            Pattern loserPattern = Pattern.compile("(?i)●?\\s*Loser:\\s*([a-zA-Z0-9_]{3,16})");
            Matcher lm = loserPattern.matcher(clean);
            if (lm.find()) {
                summaryLoser = lm.group(1);
            }

            // If we have winner, loser, and pot, finalize!
            if (summaryWinner != null && summaryLoser != null && summaryPot > 0) {
                statsManager.recordGameSummary(summaryWinner, summaryLoser, summaryPot, profile.getHouseCut(), localPlayerName);
                resetSummaryState();
            }
        }
    }

    private void resetSummaryState() {
        inGameSummary = false;
        summaryPot = 0;
        summaryWinner = null;
        summaryLoser = null;
    }
}
