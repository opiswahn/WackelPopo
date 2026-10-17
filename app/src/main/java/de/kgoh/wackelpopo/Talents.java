package de.kgoh.wackelpopo;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Permanent upgrades bought with Popo-Taler. Unlike the shop, which starts from scratch every game, a talent
 * stays forever, so every round over the weeks starts a little stronger.
 */
public final class Talents {
    public static final int CUSHION = 0, THICK_SKIN = 1, RHYTHM = 2, FEVER = 3, LUCK = 4, SECOND_CHANCE = 5;
    public static final String[] ICONS = {"🛋️", "🛡️", "🎯", "🔥", "🍀", "💖"};
    public static final String[] NAMES = {"Polster", "Dicke Haut", "Rhythmusgefühl", "Fieber-Glut", "Goodie-Glück",
            "Zweite Chance"};
    public static final String[] NAMES_EN = {"Cushion", "Thick Skin", "Sense of Rhythm", "Fever Glow", "Goodie Luck",
            "Second Chance"};
    private static final int[] MAX = {5, 5, 5, 5, 5, 1};
    private static final int[] BASE = {20, 30, 40, 30, 25, 300};

    private final SharedPreferences prefs;

    public Talents(Context context) {
        prefs = context.getSharedPreferences("talents", Context.MODE_PRIVATE);
    }

    public int level(int i) {
        return prefs.getInt("t" + i, 0);
    }

    public boolean maxed(int i) {
        return level(i) >= MAX[i];
    }

    public int max(int i) {
        return MAX[i];
    }

    /** Price of the next level: doubles with every level bought. */
    public long price(int i) {
        return (long) BASE[i] << level(i);
    }

    /** Buys the next level with Taler; false if maxed or too poor. */
    public boolean buy(int i, Wardrobe wardrobe) {
        if (maxed(i) || !wardrobe.spend(price(i))) return false;
        prefs.edit().putInt("t" + i, level(i) + 1).apply();
        return true;
    }

    /** What the talent does at its current level, in the active language. */
    public String describe(int i) {
        int l = level(i);
        switch (i) {
            case CUSHION:
                return Lang.t("+10 Startpunkte pro Stufe (jetzt +" + startPoints() + ")",
                        "+10 start points per level (now +" + startPoints() + ")");
            case THICK_SKIN:
                return Lang.t("Zähler sinkt 4 % langsamer pro Stufe (jetzt −" + 4 * l + " %)",
                        "Counter drops 4 % slower per level (now −" + 4 * l + " %)");
            case RHYTHM:
                return Lang.t("Trefferfenster 6 % breiter pro Stufe (jetzt +" + 6 * l + " %)",
                        "Timing window 6 % wider per level (now +" + 6 * l + " %)");
            case FEVER:
                return Lang.t("Fieber 2 Combo früher pro Stufe (jetzt ab " + feverCombo() + ")",
                        "Fever 2 combo sooner per level (now from " + feverCombo() + ")");
            case LUCK:
                return Lang.t("Goodies 8 % öfter pro Stufe (jetzt +" + 8 * l + " %)",
                        "Goodies 8 % more often per level (now +" + 8 * l + " %)");
            default:
                return Lang.t("Einmal pro Spiel bei −10 gerettet", "Saved once per game at −10");
        }
    }

    public int startPoints() {
        return 10 * level(CUSHION);
    }

    public double drainFactor() {
        return 1 - 0.04 * level(THICK_SKIN);
    }

    public float windowFactor() {
        return 1 + 0.06f * level(RHYTHM);
    }

    public int feverCombo() {
        return 16 - 2 * level(FEVER);
    }

    public float goodieInterval() {
        return 1 - 0.08f * level(LUCK);
    }

    public boolean secondChance() {
        return level(SECOND_CHANCE) > 0;
    }
}
