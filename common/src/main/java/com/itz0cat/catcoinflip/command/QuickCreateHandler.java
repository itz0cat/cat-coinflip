package com.itz0cat.catcoinflip.command;

import com.itz0cat.catcoinflip.compat.PlatformCompat;
import com.itz0cat.catcoinflip.format.AmountParseResult;
import com.itz0cat.catcoinflip.format.AmountParser;
import com.itz0cat.catcoinflip.format.NumberFormatter;
import com.itz0cat.catcoinflip.profile.Profile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class QuickCreateHandler {

    private static final Set<String> KNOWN_SUBCOMMANDS = new HashSet<>(Arrays.asList(
            "delete", "del", "toggle", "help", "stats", "list", "cancel"
    ));

    private final AmountParser amountParser = new AmountParser();
    private final List<String> recentAmounts = new ArrayList<>();

    // Confirmation safety tracking
    private String pendingCommand = null;
    private long pendingExpiry = 0;

    public QuickCreateHandler() {}

    public synchronized CommandRewriteResult processCommand(String rawCommand, Profile profile, PlatformCompat compat) {
        if (rawCommand == null || rawCommand.trim().isEmpty() || profile == null || !profile.isQuickCreate()) {
            return CommandRewriteResult.unmodified(rawCommand);
        }

        // Clean leading slash if present
        String cmd = rawCommand.startsWith("/") ? rawCommand.substring(1).trim() : rawCommand.trim();
        String[] parts = cmd.split("\\s+");
        if (parts.length == 0) {
            return CommandRewriteResult.unmodified(rawCommand);
        }

        String alias = parts[0].toLowerCase(Locale.ROOT);
        boolean aliasMatched = false;
        for (String a : profile.getCommandAliases()) {
            if (a.equalsIgnoreCase(alias)) {
                aliasMatched = true;
                break;
            }
        }

        if (!aliasMatched) {
            return CommandRewriteResult.unmodified(rawCommand);
        }

        // Check if just "/cf" or "/coinflip" without args
        if (parts.length == 1) {
            return CommandRewriteResult.unmodified(rawCommand);
        }

        String firstArg = parts[1].toLowerCase(Locale.ROOT);

        // Check known non-create subcommands
        if (KNOWN_SUBCOMMANDS.contains(firstArg)) {
            return CommandRewriteResult.unmodified(rawCommand);
        }

        String targetAmountStr;
        String remainingArgs = "";
        boolean isExplicitCreate = firstArg.equals("create");

        if (isExplicitCreate) {
            if (parts.length < 3) {
                return CommandRewriteResult.unmodified(rawCommand);
            }
            targetAmountStr = parts[2];
            if (parts.length > 3) {
                remainingArgs = " " + String.join(" ", Arrays.copyOfRange(parts, 3, parts.length));
            }
        } else {
            // First arg is the amount! e.g. "/cf 28m" or "/cf 28m shards"
            targetAmountStr = parts[1];
            if (parts.length > 2) {
                remainingArgs = " " + String.join(" ", Arrays.copyOfRange(parts, 2, parts.length));
            }
        }

        AmountParseResult parseResult = amountParser.parseInput(targetAmountStr);
        if (!parseResult.isSuccess()) {
            // If it was an explicit create command, but invalid, report error locally
            if (isExplicitCreate) {
                if (compat != null) {
                    compat.sendClientChatMessage("§c[CatCoinflip] Invalid amount: " + parseResult.getErrorMessage());
                }
                return CommandRewriteResult.blocked();
            }
            // If it was not explicit create, maybe it's some other subcommand we didn't know about -> pass through
            return CommandRewriteResult.unmodified(rawCommand);
        }

        long amount = parseResult.getValue();
        String plainDigits = parseResult.getPlainDigits();

        // Check minimum bet warning
        if (amount < profile.getMinBet()) {
            if (compat != null) {
                compat.sendClientChatMessage("§e[CatCoinflip] Warning: Bet " + profile.getCurrencySymbol() + plainDigits +
                        " is below minimum (" + profile.getCurrencySymbol() + profile.getMinBet() + ")");
            }
        }

        // Confirmation safety
        long confirmThreshold = profile.getConfirmAbove();
        if (confirmThreshold > 0 && amount >= confirmThreshold) {
            long now = System.currentTimeMillis();
            if (pendingCommand != null && pendingCommand.equalsIgnoreCase(rawCommand) && now < pendingExpiry) {
                // Confirmed! Clear pending
                pendingCommand = null;
                pendingExpiry = 0;
            } else {
                // Require confirmation
                pendingCommand = rawCommand;
                pendingExpiry = now + 10_000L; // 10 seconds
                if (compat != null) {
                    compat.sendClientChatMessage("§6[CatCoinflip] §eLarge bet warning (§f" + profile.getCurrencySymbol() +
                            NumberFormatter.formatCompact(amount, profile.getShortNumbersFrom()) +
                            "§e)! Type §f" + (rawCommand.startsWith("/") ? rawCommand : "/" + rawCommand) + " §eagain within 10s to confirm.");
                }
                return CommandRewriteResult.blocked();
            }
        }

        // Record in recent amounts
        recordRecentAmount(targetAmountStr);

        // Build rewritten command
        String template = profile.getQuickCreateTemplate(); // "{alias} create {amount}"
        String rewritten = template
                .replace("{alias}", parts[0])
                .replace("{amount}", plainDigits) + remainingArgs;

        // Ensure leading slash if original had it
        String finalCommand = rawCommand.startsWith("/") ? ("/" + rewritten) : rewritten;

        // Echo expansion
        if (profile.isEchoExpansion() && compat != null) {
            String shortDisplay = profile.getCurrencySymbol() + NumberFormatter.formatCompact(amount, profile.getShortNumbersFrom());
            compat.sendClientChatMessage("§8[CatCoinflip] §7-> §f" + finalCommand + " §e(" + shortDisplay + ")");
        }

        return CommandRewriteResult.rewritten(finalCommand);
    }

    private synchronized void recordRecentAmount(String amt) {
        recentAmounts.remove(amt);
        recentAmounts.add(0, amt);
        while (recentAmounts.size() > 5) {
            recentAmounts.remove(recentAmounts.size() - 1);
        }
    }

    public synchronized List<String> getSuggestions(String currentInput, Profile profile) {
        List<String> list = new ArrayList<>(Arrays.asList("1k", "10k", "100k", "1m", "10m", "100m", "1b"));
        for (String amt : recentAmounts) {
            if (!list.contains(amt)) {
                list.add(amt);
            }
        }
        return list;
    }
}
