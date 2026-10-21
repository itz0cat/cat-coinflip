package com.itz0cat.catcoinflip.tooltip;

import com.itz0cat.catcoinflip.data.PlayerStats;
import com.itz0cat.catcoinflip.data.SessionStats;
import com.itz0cat.catcoinflip.format.NumberFormatter;
import com.itz0cat.catcoinflip.profile.Profile;
import com.itz0cat.catcoinflip.profile.TooltipLineConfig;

import java.util.ArrayList;
import java.util.List;

public class TooltipGenerator {

    public static List<String> generateLines(
            ListingData listing,
            PlayerStats stats,
            SessionStats session,
            Profile profile,
            long userBalance
    ) {
        List<String> output = new ArrayList<>();
        if (listing == null || profile == null) {
            return output;
        }

        long amount = listing.getAmount();
        long threshold = profile.getShortNumbersFrom();
        String cur = profile.getCurrencySymbol();
        double houseCut = profile.getHouseCut();
        boolean hideMissing = profile.isHideMissingData();
        boolean plainIcons = profile.isPlainTextIcons();

        // Calculations
        long netWin = Math.round(amount * (1.0 - houseCut));
        long totalPayout = amount + netWin;
        double payoutMultiplier = 1.0 + (1.0 - houseCut);
        long houseCutAmt = Math.round(amount * houseCut);

        double riskPercent = (userBalance > 0) ? (((double) amount / userBalance) * 100.0) : -1.0;
        boolean canAfford = (userBalance >= 0) && (userBalance >= amount);
        long shortfall = (!canAfford && userBalance >= 0) ? (amount - userBalance) : 0L;

        long balAfterWin = (userBalance >= 0) ? (userBalance + netWin) : -1L;
        long balAfterLoss = (userBalance >= 0) ? (userBalance - amount) : -1L;
        long lossesToBroke = (userBalance > 0 && amount > 0) ? (userBalance / amount) : -1L;

        // EV = 0.5 * netWin - 0.5 * amount
        long ev = Math.round(0.5 * netWin - 0.5 * amount);
        double breakevenRate = (1.0 / payoutMultiplier) * 100.0;

        for (TooltipLineConfig cfg : profile.getLines()) {
            if (!cfg.isEnabled()) {
                continue;
            }

            String id = cfg.getId();
            String icon = plainIcons ? "" : (cfg.getIcon() + " ");
            String color = cfg.getColor() != null ? cfg.getColor() : "§7";
            String label = cfg.getLabel();

            String val = null;
            boolean hasData = true;

            switch (id) {
                case "amount_short":
                    val = cur + NumberFormatter.formatCompact(amount, threshold);
                    break;
                case "if_win":
                    val = "+" + cur + NumberFormatter.formatCompact(netWin, threshold);
                    break;
                case "if_lose":
                    val = "-" + cur + NumberFormatter.formatCompact(amount, threshold);
                    break;
                case "payout":
                    val = String.format("%.2fx (%s%s)", payoutMultiplier, cur, NumberFormatter.formatCompact(totalPayout, threshold));
                    break;
                case "house_cut":
                    val = String.format("%.1f%% (%s%s)", houseCut * 100.0, cur, NumberFormatter.formatCompact(houseCutAmt, threshold));
                    break;
                case "risk":
                    if (riskPercent >= 0) {
                        val = String.format("%.1f%% of balance", riskPercent);
                    } else {
                        hasData = false;
                        val = "N/A";
                    }
                    break;
                case "can_afford":
                    if (userBalance >= 0) {
                        val = canAfford ? "§aYes" : "§cNo";
                    } else {
                        hasData = false;
                        val = "N/A";
                    }
                    break;
                case "shortfall":
                    if (userBalance >= 0) {
                        val = shortfall > 0 ? (cur + NumberFormatter.formatCompact(shortfall, threshold)) : "None";
                    } else {
                        hasData = false;
                        val = "N/A";
                    }
                    break;
                case "balance_after_win":
                    if (balAfterWin >= 0) {
                        val = cur + NumberFormatter.formatCompact(balAfterWin, threshold);
                    } else {
                        hasData = false;
                        val = "N/A";
                    }
                    break;
                case "balance_after_loss":
                    if (balAfterLoss >= 0) {
                        val = cur + NumberFormatter.formatCompact(balAfterLoss, threshold);
                    } else {
                        hasData = false;
                        val = "N/A";
                    }
                    break;
                case "losses_to_broke":
                    if (lossesToBroke >= 0) {
                        val = String.valueOf(lossesToBroke);
                    } else {
                        hasData = false;
                        val = "N/A";
                    }
                    break;
                case "ev":
                    val = (ev >= 0 ? "+" : "") + cur + NumberFormatter.formatCompact(ev, threshold);
                    break;
                case "breakeven_rate":
                    val = String.format("%.1f%%", breakevenRate);
                    break;
                case "win_rate":
                    if (stats != null && stats.getTotalGames() > 0) {
                        val = String.format("%.1f%%", stats.getWinRate());
                    } else {
                        hasData = false;
                        val = "N/A";
                    }
                    break;
                case "record":
                    if (stats != null && stats.getTotalGames() > 0) {
                        val = stats.getWins() + "W - " + stats.getLosses() + "L";
                    } else {
                        hasData = false;
                        val = "N/A";
                    }
                    break;
                case "games_seen":
                    if (stats != null) {
                        val = String.valueOf(stats.getTotalGames());
                    } else {
                        hasData = false;
                        val = "0";
                    }
                    break;
                case "streak":
                    if (stats != null && stats.getStreak() != 0) {
                        val = (stats.getStreak() > 0 ? "+" : "") + stats.getStreak() + (stats.getStreak() > 0 ? "W" : "L");
                    } else {
                        hasData = false;
                        val = "0";
                    }
                    break;
                case "last_result":
                    if (stats != null && !"N/A".equals(stats.getLastResult())) {
                        val = stats.getLastResult();
                    } else {
                        hasData = false;
                        val = "N/A";
                    }
                    break;
                case "total_wagered":
                    if (stats != null && stats.getTotalWagered() > 0) {
                        val = cur + NumberFormatter.formatCompact(stats.getTotalWagered(), threshold);
                    } else {
                        hasData = false;
                        val = "N/A";
                    }
                    break;
                case "biggest_bet":
                    if (stats != null && stats.getBiggestBet() > 0) {
                        val = cur + NumberFormatter.formatCompact(stats.getBiggestBet(), threshold);
                    } else {
                        hasData = false;
                        val = "N/A";
                    }
                    break;
                case "avg_bet":
                    if (stats != null && stats.getAverageBet() > 0) {
                        val = cur + NumberFormatter.formatCompact(stats.getAverageBet(), threshold);
                    } else {
                        hasData = false;
                        val = "N/A";
                    }
                    break;
                case "net_profit":
                    if (stats != null && stats.getTotalGames() > 0) {
                        val = (stats.getNetProfit() >= 0 ? "+" : "") + cur + NumberFormatter.formatCompact(stats.getNetProfit(), threshold);
                    } else {
                        hasData = false;
                        val = "N/A";
                    }
                    break;
                case "last_seen":
                    if (stats != null && stats.getLastSeen() > 0) {
                        long diff = (System.currentTimeMillis() - stats.getLastSeen()) / 1000;
                        if (diff < 60) val = diff + "s ago";
                        else if (diff < 3600) val = (diff / 60) + "m ago";
                        else val = (diff / 3600) + "h ago";
                    } else {
                        hasData = false;
                        val = "N/A";
                    }
                    break;
                case "vs_record":
                    if (stats != null && stats.getVsTotalGames() > 0) {
                        val = stats.getVsWins() + "W - " + stats.getVsLosses() + "L";
                    } else {
                        hasData = false;
                        val = "0W - 0L";
                    }
                    break;
                case "vs_profit":
                    if (stats != null && stats.getVsTotalGames() > 0) {
                        val = (stats.getVsProfit() >= 0 ? "+" : "") + cur + NumberFormatter.formatCompact(stats.getVsProfit(), threshold);
                    } else {
                        hasData = false;
                        val = cur + "0";
                    }
                    break;
                case "vs_last_result":
                    if (stats != null && !"N/A".equals(stats.getVsLastResult())) {
                        val = stats.getVsLastResult();
                    } else {
                        hasData = false;
                        val = "N/A";
                    }
                    break;
                case "rank":
                    if (amount >= 1_000_000_000L) val = "High Tier";
                    else if (amount >= 50_000_000L) val = "Mid Tier";
                    else val = "Low Tier";
                    break;
                case "page_pos":
                    val = "#" + (listing.getSlotIndex() + 1);
                    break;
                case "size_vs_avg":
                    if (stats != null && stats.getAverageBet() > 0) {
                        double ratio = ((double) amount / stats.getAverageBet()) * 100.0;
                        val = String.format("%.0f%%", ratio);
                    } else {
                        hasData = false;
                        val = "100%";
                    }
                    break;
                case "listing_age":
                    long ageSec = Math.max(0, (System.currentTimeMillis() - listing.getDetectedTimestamp()) / 1000);
                    val = ageSec + "s";
                    break;
                case "total_listings":
                    val = String.valueOf(listing.getTotalListings());
                    break;
                case "whale_tag":
                    if (amount >= 1_000_000_000L) val = "♛ WHALE";
                    else if (amount >= 100_000_000L) val = "★ HIGH ROLLER";
                    else val = "STANDARD";
                    break;
                case "session_profit":
                    if (session != null) {
                        val = (session.getNetProfit() >= 0 ? "+" : "") + cur + NumberFormatter.formatCompact(session.getNetProfit(), threshold);
                    } else {
                        val = cur + "0";
                    }
                    break;
                case "session_wl":
                    if (session != null) {
                        val = session.getWins() + "W - " + session.getLosses() + "L";
                    } else {
                        val = "0W - 0L";
                    }
                    break;
                case "session_winrate":
                    if (session != null && session.getTotalGames() > 0) {
                        val = String.format("%.1f%%", session.getWinRate());
                    } else {
                        val = "0.0%";
                    }
                    break;
                case "my_streak":
                    if (session != null && session.getCurrentStreak() != 0) {
                        val = (session.getCurrentStreak() > 0 ? "+" : "") + session.getCurrentStreak() + (session.getCurrentStreak() > 0 ? "W" : "L");
                    } else {
                        val = "0";
                    }
                    break;
                case "best_streak":
                    val = session != null ? ("+" + session.getBestStreak() + "W") : "0W";
                    break;
                case "worst_streak":
                    val = session != null ? (session.getWorstStreak() + "L") : "0L";
                    break;
                case "biggest_win":
                    val = session != null ? (cur + NumberFormatter.formatCompact(session.getBiggestWin(), threshold)) : (cur + "0");
                    break;
                case "biggest_loss":
                    val = session != null ? (cur + NumberFormatter.formatCompact(session.getBiggestLoss(), threshold)) : (cur + "0");
                    break;
                case "games_per_hour":
                    val = session != null ? String.format("%.1f", session.getGamesPerHour()) : "0.0";
                    break;
                case "profit_per_hour":
                    val = session != null ? ((session.getProfitPerHour() >= 0 ? "+" : "") + cur + NumberFormatter.formatCompact((long) session.getProfitPerHour(), threshold) + "/hr") : (cur + "0/hr");
                    break;
                case "risk_bar":
                    val = buildRiskMeter(riskPercent);
                    break;
                default:
                    val = "N/A";
                    break;
            }

            if (!hasData && hideMissing) {
                continue;
            }

            String displayVal = hasData ? val : "§7N/A";
            output.add(color + icon + label + ": §f" + displayVal);
        }

        return output;
    }

    private static String buildRiskMeter(double riskPercent) {
        if (riskPercent < 0) {
            return "░░░░░░░░░░ N/A";
        }
        int filled = (int) Math.min(10, Math.max(0, Math.round(riskPercent / 10.0)));
        StringBuilder sb = new StringBuilder();
        if (filled > 7) sb.append("§c");
        else if (filled > 4) sb.append("§6");
        else sb.append("§a");

        for (int i = 0; i < filled; i++) sb.append("█");
        sb.append("§8");
        for (int i = filled; i < 10; i++) sb.append("░");
        sb.append(String.format(" §f%.0f%%", Math.min(100.0, riskPercent)));
        return sb.toString();
    }
}
