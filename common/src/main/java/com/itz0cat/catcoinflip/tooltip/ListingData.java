package com.itz0cat.catcoinflip.tooltip;

public class ListingData {

    private final String playerName;
    private final long amount;
    private final int slotIndex;
    private final int totalListings;
    private final long detectedTimestamp;

    public ListingData(String playerName, long amount, int slotIndex, int totalListings) {
        this.playerName = playerName != null ? playerName : "Unknown";
        this.amount = amount;
        this.slotIndex = slotIndex;
        this.totalListings = totalListings;
        this.detectedTimestamp = System.currentTimeMillis();
    }

    public String getPlayerName() { return playerName; }
    public long getAmount() { return amount; }
    public int getSlotIndex() { return slotIndex; }
    public int getTotalListings() { return totalListings; }
    public long getDetectedTimestamp() { return detectedTimestamp; }
}
