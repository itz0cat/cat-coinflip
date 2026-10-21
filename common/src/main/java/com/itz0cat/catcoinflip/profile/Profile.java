package com.itz0cat.catcoinflip.profile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class Profile {

    private String name = "Default";
    private List<String> serverAddressPatterns = new ArrayList<>();
    private String guiTitleRegex = "Coinflip \\| Menu";
    private List<String> watermarkScreens = new ArrayList<>();
    private boolean watermarkUseSignature = true;
    private String watermarkAnchor = "bottom_right"; // bottom_right, top_center_above_gui, bottom_center
    private int watermarkScale = 1;
    private float watermarkOpacity = 0.85f;
    private boolean showWatermark = false;

    private double houseCut = 0.05; // 5%
    private long minBet = 100;
    private String currencySymbol = "$";
    private List<String> commandAliases = new ArrayList<>(List.of("cf", "coinflip"));
    private boolean quickCreate = true;
    private String quickCreateTemplate = "{alias} create {amount}";
    private boolean echoExpansion = true;
    private long confirmAbove = 0; // 0 = disabled

    private boolean hideMissingData = false;
    private boolean plainTextIcons = false;
    private long shortNumbersFrom = 100_000L;

    private Map<String, String> chatRegexes = new HashMap<>();
    private List<TooltipLineConfig> lines = new ArrayList<>();

    public Profile() {}

    public Profile(String name) {
        this.name = name;
    }

    public boolean matchesServer(String serverAddress) {
        if (serverAddress == null || serverAddress.isEmpty()) {
            return false;
        }
        if (serverAddressPatterns == null || serverAddressPatterns.isEmpty()) {
            return false;
        }
        for (String patternStr : serverAddressPatterns) {
            try {
                if (Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE).matcher(serverAddress).find()) {
                    return true;
                }
            } catch (Exception ignored) {}
        }
        return false;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<String> getServerAddressPatterns() {
        if (serverAddressPatterns == null) serverAddressPatterns = new ArrayList<>();
        return serverAddressPatterns;
    }
    public void setServerAddressPatterns(List<String> serverAddressPatterns) { this.serverAddressPatterns = serverAddressPatterns; }

    public String getGuiTitleRegex() { return guiTitleRegex != null ? guiTitleRegex : "Coinflip \\| Menu"; }
    public void setGuiTitleRegex(String guiTitleRegex) { this.guiTitleRegex = guiTitleRegex; }

    public List<String> getWatermarkScreens() {
        if (watermarkScreens == null) watermarkScreens = new ArrayList<>();
        return watermarkScreens;
    }
    public void setWatermarkScreens(List<String> watermarkScreens) { this.watermarkScreens = watermarkScreens; }

    public boolean isWatermarkUseSignature() { return watermarkUseSignature; }
    public void setWatermarkUseSignature(boolean watermarkUseSignature) { this.watermarkUseSignature = watermarkUseSignature; }

    public String getWatermarkAnchor() { return watermarkAnchor != null ? watermarkAnchor : "bottom_right"; }
    public void setWatermarkAnchor(String watermarkAnchor) { this.watermarkAnchor = watermarkAnchor; }

    public int getWatermarkScale() { return Math.max(1, watermarkScale); }
    public void setWatermarkScale(int watermarkScale) { this.watermarkScale = watermarkScale; }

    public float getWatermarkOpacity() { return watermarkOpacity; }
    public void setWatermarkOpacity(float watermarkOpacity) { this.watermarkOpacity = watermarkOpacity; }

    public boolean isShowWatermark() { return showWatermark; }
    public void setShowWatermark(boolean showWatermark) { this.showWatermark = showWatermark; }

    public double getHouseCut() { return houseCut; }
    public void setHouseCut(double houseCut) { this.houseCut = houseCut; }

    public long getMinBet() { return minBet; }
    public void setMinBet(long minBet) { this.minBet = minBet; }

    public String getCurrencySymbol() { return currencySymbol != null ? currencySymbol : "$"; }
    public void setCurrencySymbol(String currencySymbol) { this.currencySymbol = currencySymbol; }

    public List<String> getCommandAliases() {
        if (commandAliases == null) commandAliases = new ArrayList<>();
        return commandAliases;
    }
    public void setCommandAliases(List<String> commandAliases) { this.commandAliases = commandAliases; }

    public boolean isQuickCreate() { return quickCreate; }
    public void setQuickCreate(boolean quickCreate) { this.quickCreate = quickCreate; }

    public String getQuickCreateTemplate() { return quickCreateTemplate != null ? quickCreateTemplate : "{alias} create {amount}"; }
    public void setQuickCreateTemplate(String quickCreateTemplate) { this.quickCreateTemplate = quickCreateTemplate; }

    public boolean isEchoExpansion() { return echoExpansion; }
    public void setEchoExpansion(boolean echoExpansion) { this.echoExpansion = echoExpansion; }

    public long getConfirmAbove() { return confirmAbove; }
    public void setConfirmAbove(long confirmAbove) { this.confirmAbove = confirmAbove; }

    public boolean isHideMissingData() { return hideMissingData; }
    public void setHideMissingData(boolean hideMissingData) { this.hideMissingData = hideMissingData; }

    public boolean isPlainTextIcons() { return plainTextIcons; }
    public void setPlainTextIcons(boolean plainTextIcons) { this.plainTextIcons = plainTextIcons; }

    public long getShortNumbersFrom() { return shortNumbersFrom > 0 ? shortNumbersFrom : 100_000L; }
    public void setShortNumbersFrom(long shortNumbersFrom) { this.shortNumbersFrom = shortNumbersFrom; }

    public Map<String, String> getChatRegexes() {
        if (chatRegexes == null) chatRegexes = new HashMap<>();
        return chatRegexes;
    }
    public void setChatRegexes(Map<String, String> chatRegexes) { this.chatRegexes = chatRegexes; }

    public List<TooltipLineConfig> getLines() {
        if (lines == null) lines = new ArrayList<>();
        return lines;
    }
    public void setLines(List<TooltipLineConfig> lines) { this.lines = lines; }

    public Profile copy(String newName) {
        Profile p = new Profile(newName);
        p.serverAddressPatterns = new ArrayList<>(this.getServerAddressPatterns());
        p.guiTitleRegex = this.guiTitleRegex;
        p.watermarkScreens = new ArrayList<>(this.getWatermarkScreens());
        p.watermarkUseSignature = this.watermarkUseSignature;
        p.watermarkAnchor = this.watermarkAnchor;
        p.watermarkScale = this.watermarkScale;
        p.watermarkOpacity = this.watermarkOpacity;
        p.showWatermark = this.showWatermark;
        p.houseCut = this.houseCut;
        p.minBet = this.minBet;
        p.currencySymbol = this.currencySymbol;
        p.commandAliases = new ArrayList<>(this.getCommandAliases());
        p.quickCreate = this.quickCreate;
        p.quickCreateTemplate = this.quickCreateTemplate;
        p.echoExpansion = this.echoExpansion;
        p.confirmAbove = this.confirmAbove;
        p.hideMissingData = this.hideMissingData;
        p.plainTextIcons = this.plainTextIcons;
        p.shortNumbersFrom = this.shortNumbersFrom;
        p.chatRegexes = new HashMap<>(this.getChatRegexes());
        p.lines = new ArrayList<>();
        for (TooltipLineConfig line : this.getLines()) {
            p.lines.add(new TooltipLineConfig(line.getId(), line.getLabel(), line.getColor(), line.getIcon(), line.isEnabled()));
        }
        return p;
    }
}
