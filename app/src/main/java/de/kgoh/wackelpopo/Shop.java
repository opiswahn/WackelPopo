package de.kgoh.wackelpopo;

/** Upgrades you buy mid-game with Popo-Klatsch-Counter points. Prices grow exponentially per level. */
public final class Shop {
    public static final int HAND = 0, AUTO = 1, CREAM = 2, CRIT = 3, TURBO = 4, GOLD = 5;

    public static final String[] ICONS = {"👏", "🤖", "🧴", "⚡", "🔥", "👑"};
    public static final String[] NAMES = {"Doppelhand", "Auto-Klatscher", "Popo-Creme", "Kritischer Klatscher",
            "Combo-Turbo", "Goldener Popo"};
    private static final double[] BASE = {15, 30, 50, 80, 120, 500};
    private static final double[] GROWTH = {1.6, 1.6, 1.9, 1.7, 1.8, 4.0};
    private static final int[] MAX = {99, 99, 10, 10, 20, 10};

    private final int[] levels = new int[NAMES.length];

    public int level(int i) {
        return levels[i];
    }

    public boolean maxed(int i) {
        return levels[i] >= MAX[i];
    }

    public long price(int i) {
        return Math.round(BASE[i] * Math.pow(GROWTH[i], levels[i]));
    }

    public void buy(int i) {
        levels[i]++;
    }

    public void reset() {
        for (int i = 0; i < levels.length; i++) levels[i] = 0;
    }

    /** What the next level does, in a few words. */
    public String describe(int i) {
        switch (i) {
            case HAND:
                return "+1 pro Klatsch (jetzt " + (1 + levels[HAND]) + ")";
            case AUTO:
                return "+1 pro Sekunde von allein (jetzt " + levels[AUTO] + "/s)";
            case CREAM:
                return "Counter sinkt 15 % langsamer";
            case CRIT:
                return "+5 % Chance auf x5 (jetzt " + 5 * levels[CRIT] + " %)";
            case TURBO:
                return "+20 % bei Schnellklatschen (jetzt +" + 20 * levels[TURBO] + " %)";
            default:
                return "Alles x1,3 (jetzt x" + String.format(java.util.Locale.GERMANY, "%.1f", goldMultiplier()) + ")";
        }
    }

    public double tapGain(boolean fast) {
        return (1 + levels[HAND]) * (fast ? 1 + 0.2 * levels[TURBO] : 1) * goldMultiplier();
    }

    public double autoRate() {
        return levels[AUTO] * goldMultiplier();
    }

    public double critChance() {
        return 0.05 * levels[CRIT];
    }

    public double drainFactor() {
        return Math.pow(0.85, levels[CREAM]);
    }

    public double goldMultiplier() {
        return Math.pow(1.3, levels[GOLD]);
    }
}
