package com.itz0cat.catcoinflip.v1_21_11.config;

import com.itz0cat.catcoinflip.CatCoinflip;
import com.itz0cat.catcoinflip.config.CatCoinflipConfig;
import com.itz0cat.catcoinflip.config.ConfigManager;
import com.itz0cat.catcoinflip.config.RemoteConfigManager;
import com.itz0cat.catcoinflip.profile.Profile;
import com.itz0cat.catcoinflip.profile.ProfileManager;
import com.itz0cat.catcoinflip.profile.TooltipLineConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.List;

public class ClothConfigScreen1_21_11 {

    public static Screen buildScreen(Screen parent) {
        CatCoinflipConfig config = ConfigManager.getConfig();
        ProfileManager pm = CatCoinflip.getInstance().getProfileManager();
        Profile profile = pm.getActiveProfile();

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.literal("§6Cat Coinflip"));

        ConfigEntryBuilder eb = builder.entryBuilder();

        // 1. General Tab
        ConfigCategory general = builder.getOrCreateCategory(Text.literal("General"));
        general.addEntry(eb.startBooleanToggle(Text.literal("Enable Tooltip"), config.isTooltipEnabled())
                .setDefaultValue(true)
                .setSaveConsumer(config::setTooltipEnabled)
                .build());

        general.addEntry(eb.startBooleanToggle(Text.literal("Quick Create (/cf 28m)"), profile.isQuickCreate())
                .setDefaultValue(true)
                .setSaveConsumer(profile::setQuickCreate)
                .build());

        general.addEntry(eb.startBooleanToggle(Text.literal("Echo Expansion in Chat"), profile.isEchoExpansion())
                .setDefaultValue(true)
                .setSaveConsumer(profile::setEchoExpansion)
                .build());

        general.addEntry(eb.startLongField(Text.literal("Confirm Large Bets Above ($)"), profile.getConfirmAbove())
                .setDefaultValue(0L)
                .setTooltip(Text.literal("Amounts at or above this require typing again within 10s. 0 = disabled."))
                .setSaveConsumer(profile::setConfirmAbove)
                .build());

        // 2. Profiles Tab
        ConfigCategory profilesCat = builder.getOrCreateCategory(Text.literal("Profiles"));
        profilesCat.addEntry(eb.startStrField(Text.literal("Active Profile"), profile.getName())
                .setDefaultValue("BananaSMP")
                .setTooltip(Text.literal("Current server profile name"))
                .setSaveConsumer(pm::setActiveProfileName)
                .build());

        profilesCat.addEntry(eb.startDoubleField(Text.literal("House Cut %"), profile.getHouseCut() * 100.0)
                .setDefaultValue(5.0)
                .setSaveConsumer(v -> profile.setHouseCut(v / 100.0))
                .build());

        profilesCat.addEntry(eb.startLongField(Text.literal("Minimum Bet"), profile.getMinBet())
                .setDefaultValue(100L)
                .setSaveConsumer(profile::setMinBet)
                .build());

        profilesCat.addEntry(eb.startStrField(Text.literal("Currency Symbol"), profile.getCurrencySymbol())
                .setDefaultValue("$")
                .setSaveConsumer(profile::setCurrencySymbol)
                .build());

        // 3. Tooltip Lines Tab
        ConfigCategory linesCat = builder.getOrCreateCategory(Text.literal("Tooltip Lines"));
        linesCat.addEntry(eb.startTextDescription(Text.literal("§7Toggle and configure lines displayed on Coinflip item tooltips:")).build());

        List<TooltipLineConfig> lines = profile.getLines();
        for (TooltipLineConfig line : lines) {
            linesCat.addEntry(eb.startBooleanToggle(Text.literal(line.getLabel() + " (" + line.getId() + ")"), line.isEnabled())
                    .setDefaultValue(line.isEnabled())
                    .setSaveConsumer(line::setEnabled)
                    .build());
        }

        // 4. Messages and Placeholders Tab
        ConfigCategory messagesCat = builder.getOrCreateCategory(Text.literal("Messages & Placeholders"));
        messagesCat.addEntry(eb.startStrField(Text.literal("Quick Create Template"), profile.getQuickCreateTemplate())
                .setDefaultValue("{alias} create {amount}")
                .setSaveConsumer(profile::setQuickCreateTemplate)
                .build());

        // 5. Formatting Tab
        ConfigCategory formattingCat = builder.getOrCreateCategory(Text.literal("Formatting"));
        formattingCat.addEntry(eb.startLongField(Text.literal("Short Numbers From"), profile.getShortNumbersFrom())
                .setDefaultValue(100_000L)
                .setTooltip(Text.literal("Threshold above which numbers are abbreviated with K, M, B, T, Qa"))
                .setSaveConsumer(profile::setShortNumbersFrom)
                .build());

        formattingCat.addEntry(eb.startBooleanToggle(Text.literal("Plain Text Icons Fallback"), profile.isPlainTextIcons())
                .setDefaultValue(false)
                .setSaveConsumer(profile::setPlainTextIcons)
                .build());

        formattingCat.addEntry(eb.startBooleanToggle(Text.literal("Hide Missing Data"), profile.isHideMissingData())
                .setDefaultValue(false)
                .setSaveConsumer(profile::setHideMissingData)
                .build());

        // 6. Remote and Updates Tab
        ConfigCategory remoteCat = builder.getOrCreateCategory(Text.literal("Remote & Updates"));
        RemoteConfigManager rm = ConfigManager.getRemoteManager();
        remoteCat.addEntry(eb.startTextDescription(Text.literal("§eRemote Status: §f" + (rm != null ? rm.getStatusMessage() : "Idle"))).build());

        remoteCat.addEntry(eb.startBooleanToggle(Text.literal("Check For Updates on Launch"), config.isCheckForUpdates())
                .setDefaultValue(true)
                .setSaveConsumer(config::setCheckForUpdates)
                .build());

        remoteCat.addEntry(eb.startBooleanToggle(Text.literal("Auto Download Updates"), config.isAutoDownload())
                .setDefaultValue(false)
                .setSaveConsumer(config::setAutoDownload)
                .build());

        builder.setSavingRunnable(() -> {
            ConfigManager.save();
        });

        return builder.build();
    }
}
