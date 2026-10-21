package com.itz0cat.catcoinflip.data;

public class SessionStats {

    private long startTime;
    private int wins;
    private int losses;
    private long netProfit;
    private long totalWagered;
    private int currentStreak;
    private int bestStreak;
    private int worstStreak;
    private long biggestWin;
    private long biggestLoss;
    private long lastUserBalance = -1;

    public SessionStats() {
        this.startTime = System.currentTimeMillis();
    }

    public synchronized void reset() {
        this.startTime = System.currentTimeMillis();
        this.wins = 0;
        this.losses = 0;
        this.netProfit = 0;
        this.totalWagered = 0;
        this.currentStreak = 0;
        this.bestStreak = 0;
        this.worstStreak = 0;
        this.biggestWin = 0;
        this.biggestLoss = 0;
    }

    public synchronized void recordResult(boolean won, long betAmount, long netGainOrLoss) {
        this.totalWagered += betAmount;
        if (won) {
            this.wins++;
            this.netProfit += netGainOrLoss;
            if (netGainOrLoss > this.biggestWin) {
                this.biggestWin = netGainOrLoss;
            }
            this.currentStreak = (this.currentStreak > 0) ? (this.currentStreak + 1) : 1;
            if (this.currentStreak > this.bestStreak) {
                this.bestStreak = this.currentStreak;
            }
        } else {
            this.losses++;
            this.netProfit -= betAmount;
            if (betAmount > this.biggestLoss) {
                this.biggestLoss = betAmount;
            }
            this.currentStreak = (this.currentStreak < 0) ? (this.currentStreak - 1) : -1;
            if (this.currentStreak < this.worstStreak) {
                this.worstStreak = this.currentStreak;
            }
        }
    }

    public long getStartTime() { return startTime; }
    public int getWins() { return wins; }
    public int getLosses() { return losses; }
    public int getTotalGames() { return wins + losses; }

    public double getWinRate() {
        int total = getTotalGames();
        return total == 0 ? 0.0 : ((double) wins / total) * 100.0;
    }

    public long getNetProfit() { return netProfit; }
    public long getTotalWagered() { return totalWagered; }
    public int getCurrentStreak() { return currentStreak; }
    public int getBestStreak() { return bestStreak; }
    public int getWorstStreak() { return worstStreak; }
    public long getBiggestWin() { return biggestWin; }
    public long getBiggestLoss() { return biggestLoss; }

    public long getLastUserBalance() { return lastUserBalance; }
    public void setLastUserBalance(long balance) { this.lastUserBalance = balance; }

    public double getGamesPerHour() {
        long elapsedMs = Math.max(1000L, System.currentTimeMillis() - startTime);
        double hours = elapsedMs / 3600000.0;
        return getTotalGames() / Math.max(0.001, hours);
    }

    public double getProfitPerHour() {
        long elapsedMs = Math.max(1000L, System.currentTimeMillis() - startTime);
        double hours = elapsedMs / 3600000.0;
        return netProfit / Math.max(0.001, hours);
    }
}
