package de.kgoh.wackelpopo;

/** Upgrades you buy mid-game with Popo-Klatsch-Counter points. Prices grow exponentially per level. */
public final class Shop {
    public static final int HAND = 0, AUTO = 1, CREAM = 2, CRIT = 3, TURBO = 4, GOLD = 5;

    public static final String[] ICONS = {"👏", "🤖", "🧴", "⚡", "🔥", "👑"};
    public static final String[] NAMES = {"Doppelhand", "Auto-Klatscher", "Popo-Creme", "Kritischer Klatscher",
            "Combo-Turbo", "Goldener Popo"};
    private static final double[] BASE = {25, 40, 60, 100, 150, 400};
    private static final double[] GROWTH = {2.0, 2.0, 2.2, 2.2, 2.2, 2.5};
    private static final int[] MAX = {99, 99, 10, 10, 10, 5};

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
                return "+0,5 pro Sekunde von allein (jetzt " + fmt(0.5 * levels[AUTO]) + "/s)";
            case CREAM:
                return "Counter sinkt 12 % langsamer";
            case CRIT:
                return "+4 % Chance auf x3 (jetzt " + 4 * levels[CRIT] + " %)";
            case TURBO:
                return "+10 % ab Combo 5 (jetzt +" + 10 * levels[TURBO] + " %)";
            default:
                return "Alles +15 % (jetzt x" + fmt(goldMultiplier()) + ")";
        }
    }

    public double tapGain(boolean fast) {
        return (1 + levels[HAND]) * (fast ? 1 + 0.1 * levels[TURBO] : 1) * goldMultiplier();
    }

    public double autoRate() {
        return 0.5 * levels[AUTO] * goldMultiplier();
    }

    public double critChance() {
        return 0.04 * levels[CRIT];
    }

    public double drainFactor() {
        return Math.pow(0.88, levels[CREAM]);
    }

    public double goldMultiplier() {
        return 1 + 0.15 * levels[GOLD];
    }

    private static String fmt(double v) {
        return String.format(java.util.Locale.GERMANY, "%.2f", v).replaceAll("0+$", "").replaceAll(",$", "");
    }
}
