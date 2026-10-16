package de.kgoh.wackelpopo;

import java.util.Random;

/** Goodies that drop from the sky now and then: tap one before it lands to get its bonus. */
public final class Goodies {
    public static final int PEACH = 0, ICE = 1, CHOCO = 2, CLOVER = 3, GIFT = 4, ROBOT = 5, STINK = 6, MAGNET = 7;

    public static final String[] ICONS = {"🍑", "🧊", "🍫", "🍀", "🎁", "🤖", "💨", "🧲"};
    public static final String[] NAMES = {"Pfirsich", "Frostpause", "Schoko-Power", "Glücksklee", "Geschenk",
            "Klatsch-Roboter", "Stinkbombe", "Sternen-Magnet"};
    public static final String[] EFFECTS = {"Bonus-Punkte!", "Counter sinkt nicht", "Klatscher x2", "Krit-Chance +25 %",
            "Gratis-Upgrade!", "+3 Auto-Klatscher/s", "Pfui! −15 %", "Nächster Stern sofort!"};
    /** Seconds a timed goodie lasts; 0 means it acts at once. */
    public static final float[] SECONDS = {0, 8, 10, 10, 0, 10, 0, 0};
    private static final int[] WEIGHTS = {30, 14, 14, 12, 7, 12, 8, 6};

    private Goodies() {
    }

    public static int random(Random rnd) {
        int sum = 0;
        for (int w : WEIGHTS) sum += w;
        int r = rnd.nextInt(sum);
        for (int i = 0; i < WEIGHTS.length; i++) {
            r -= WEIGHTS[i];
            if (r < 0) return i;
        }
        return PEACH;
    }
}
