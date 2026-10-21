package com.itz0cat.catcoinflip.v26_1_2;

import com.itz0cat.catcoinflip.CatCoinflip;
import com.itz0cat.catcoinflip.command.CommandRewriteResult;
import com.itz0cat.catcoinflip.config.ConfigManager;
import com.itz0cat.catcoinflip.data.PlayerStats;
import com.itz0cat.catcoinflip.data.SessionStats;
import com.itz0cat.catcoinflip.profile.Profile;
import com.itz0cat.catcoinflip.tooltip.ListingData;
import com.itz0cat.catcoinflip.tooltip.TooltipGenerator;
import com.itz0cat.catcoinflip.v26_1_2.command.ClientCommands26_1_2;
import com.itz0cat.catcoinflip.v26_1_2.compat.PlatformCompat26_1_2;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CatCoinflip26_1_2 implements ClientModInitializer {

    private static final Pattern AMOUNT_LINE_PATTERN = Pattern.compile("(?i)(?:\\$?\\s*Amount:|Amount:\\s*\\$?)\\s*\\$?([0-9,]+)");

    @Override
    public void onInitializeClient() {
        PlatformCompat26_1_2 compat = new PlatformCompat26_1_2();
        CatCoinflip mod = CatCoinflip.getInstance();
        mod.init(compat);

        // 1. Command rewrite hook for /cf 28m
        ClientSendMessageEvents.MODIFY_COMMAND.register(command -> {
            CommandRewriteResult res = mod.processOutgoingCommand(command);
            if (res.getAction() == CommandRewriteResult.Action.BLOCKED) {
                return null;
            } else if (res.getAction() == CommandRewriteResult.Action.REWRITTEN) {
                String cmd = res.getCommand();
                return cmd.startsWith("/") ? cmd.substring(1) : cmd;
            }
            return command;
        });

        // 2. Chat monitoring for data tracking
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (!overlay && message != null) {
                String localName = getLocalPlayerName();
                mod.onChatMessage(message.getString(), localName);
            }
        });

        ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> {
            if (message != null) {
                String localName = getLocalPlayerName();
                mod.onChatMessage(message.getString(), localName);
            }
        });

        // 3. Item Tooltip Injection
        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
            if (!ConfigManager.getConfig().isTooltipEnabled()) {
                return;
            }

            Minecraft mc = Minecraft.getInstance();
            Screen currentScreen = mc.screen;
            if (!(currentScreen instanceof AbstractContainerScreen<?>)) {
                return;
            }

            Profile activeProfile = mod.getProfileManager().getActiveProfile();
            if (activeProfile == null) return;

            // Check GUI title match
            String title = currentScreen.getTitle().getString().replaceAll("(?i)§[0-9a-fklmnor]", "").trim();
            if (!title.matches(activeProfile.getGuiTitleRegex()) && !title.toLowerCase().contains("coinflip")) {
                return;
            }

            // Inspect lore for Coinflip Game and Amount
            int amountLineIndex = -1;
            long parsedAmount = -1;

            for (int i = 0; i < lines.size(); i++) {
                String lineStr = lines.get(i).getString().replaceAll("(?i)§[0-9a-fklmnor]", "").trim();
                Matcher matcher = AMOUNT_LINE_PATTERN.matcher(lineStr);
                if (matcher.find()) {
                    amountLineIndex = i;
                    try {
                        parsedAmount = Long.parseLong(matcher.group(1).replace(",", ""));
                    } catch (Exception ignored) {}
                    break;
                }
            }

            if (amountLineIndex != -1 && parsedAmount > 0) {
                String itemName = stack.getHoverName().getString().replaceAll("(?i)§[0-9a-fklmnor]", "").trim();
                String opponentName = itemName.replaceAll("(?i)('s)?\\s*Head", "").replaceAll("(?i)('s)?\\s*Skull", "").trim();

                PlayerStats playerStats = mod.getStatsManager().getOrCreatePlayerStats(opponentName);
                SessionStats sessionStats = mod.getStatsManager().getData().getSession();
                long userBalance = sessionStats.getLastUserBalance();

                ListingData listing = new ListingData(opponentName, parsedAmount, 0, 1);
                List<String> extraLines = TooltipGenerator.generateLines(listing, playerStats, sessionStats, activeProfile, userBalance);

                int insertPos = amountLineIndex + 1;
                for (String extra : extraLines) {
                    lines.add(insertPos++, Component.literal(extra));
                }
            }
        });

        // 4. Server join / disconnect
        ClientPlayConnectionEvents.JOIN.register((listener, sender, client) -> {
            ServerData server = client.getCurrentServer();
            String ip = (server != null && server.ip != null) ? server.ip : "";
            mod.onServerJoin(ip);
        });

        ClientPlayConnectionEvents.DISCONNECT.register((listener, client) -> {
            mod.onServerDisconnect();
        });

        // 5. Register client commands
        ClientCommands26_1_2.register();
    }

    private static String getLocalPlayerName() {
        Minecraft mc = Minecraft.getInstance();
        return (mc != null && mc.player != null) ? mc.player.getScoreboardName() : null;
    }
}
