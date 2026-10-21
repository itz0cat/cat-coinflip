package com.itz0cat.catcoinflip.data;

public class PlayerStats {

    private String name;
    private int wins;
    private int losses;
    private long totalWagered;
    private long biggestBet;
    private int streak;
    private long lastSeen;
    private String lastResult = "N/A";
    private long netProfit;

    // Head to head stats vs local player
    private int vsWins;
    private int vsLosses;
    private long vsProfit;
    private String vsLastResult = "N/A";

    public PlayerStats() {}

    public PlayerStats(String name) {
        this.name = name;
        this.lastSeen = System.currentTimeMillis();
    }

    public synchronized void recordGame(boolean won, long betAmount, long payoutNet, boolean isVsLocalUser) {
        this.lastSeen = System.currentTimeMillis();
        this.totalWagered += betAmount;
        if (betAmount > this.biggestBet) {
            this.biggestBet = betAmount;
        }

        if (won) {
            this.wins++;
            this.netProfit += payoutNet;
            this.streak = (this.streak > 0) ? (this.streak + 1) : 1;
            this.lastResult = "WIN";
        } else {
            this.losses++;
            this.netProfit -= betAmount;
            this.streak = (this.streak < 0) ? (this.streak - 1) : -1;
            this.lastResult = "LOSS";
        }

        if (isVsLocalUser) {
            if (won) {
                // The opponent won, meaning local user lost
                this.vsLosses++;
                this.vsProfit -= betAmount;
                this.vsLastResult = "LOSS";
            } else {
                // The opponent lost, meaning local user won
                this.vsWins++;
                this.vsProfit += payoutNet;
                this.vsLastResult = "WIN";
            }
        }
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getWins() { return wins; }
    public int getLosses() { return losses; }
    public int getTotalGames() { return wins + losses; }

    public double getWinRate() {
        int total = getTotalGames();
        return total == 0 ? 0.0 : ((double) wins / total) * 100.0;
    }

    public long getTotalWagered() { return totalWagered; }
    public long getBiggestBet() { return biggestBet; }
    public long getAverageBet() {
        int total = getTotalGames();
        return total == 0 ? 0 : totalWagered / total;
    }

    public int getStreak() { return streak; }
    public long getLastSeen() { return lastSeen; }
    public String getLastResult() { return lastResult; }
    public long getNetProfit() { return netProfit; }

    public int getVsWins() { return vsWins; }
    public int getVsLosses() { return vsLosses; }
    public int getVsTotalGames() { return vsWins + vsLosses; }
    public long getVsProfit() { return vsProfit; }
    public String getVsLastResult() { return vsLastResult; }
}
