package de.kgoh.wackelpopo;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Calendar;

/**
 * One challenge per day, picked from the date, to be done within a single game. Doing it on
 * consecutive days builds a streak, which raises the Taler reward.
 */
public final class Daily {
    public static final int GOODIES = 0, SURVIVE = 1, KLATSCHER = 2, BOSS = 3, SYMMETRY = 4, FEVER = 5,
            UPGRADES = 6, NO_SHOP = 7, STARS = 8, DODGE = 9;
    public static final String[] TEXTS = {"Fang 4 Goodies", "Überlebe 4 Minuten", "Schaffe 8.000 Klatscher",
            "Besiege einen Boss", "Mach 5 Symmetrie-Klatscher (2+2 Finger)", "Erreiche den Fieber-Modus",
            "Kaufe 10 Upgrades", "Überlebe 2 Minuten ohne Shop", "Fang 2 Bonus-Sterne", "Lass 2 Stinkbomben fallen"};
    public static final String[] TEXTS_EN = {"Catch 4 Goodies", "Survive 4 minutes", "Score 8,000 slaps",
            "Defeat a boss", "Do 5 symmetry slaps (2+2 fingers)", "Reach Fever mode",
            "Buy 10 upgrades", "Survive 2 minutes without the shop", "Catch 2 bonus stars", "Let 2 Stink Bombs drop"};
    public static final int[] GOALS = {4, 240, 8000, 1, 5, 1, 10, 120, 2, 2};

    private final SharedPreferences prefs;

    public Daily(Context context) {
        prefs = context.getSharedPreferences("daily", Context.MODE_PRIVATE);
    }

    private static int today() {
        Calendar c = Calendar.getInstance();
        return c.get(Calendar.YEAR) * 10000 + (c.get(Calendar.MONTH) + 1) * 100 + c.get(Calendar.DAY_OF_MONTH);
    }

    private static int yesterday() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, -1);
        return c.get(Calendar.YEAR) * 10000 + (c.get(Calendar.MONTH) + 1) * 100 + c.get(Calendar.DAY_OF_MONTH);
    }

    /** Today's challenge. */
    public int kind() {
        int d = today();
        return (int) (((d * 2654435761L) >>> 8) % TEXTS.length);
    }

    public boolean doneToday() {
        return prefs.getInt("done", 0) == today();
    }

    /** Days in a row with the challenge done, counting today only once it is done. */
    public int streak() {
        int done = prefs.getInt("done", 0);
        return done == today() || done == yesterday() ? prefs.getInt("streak", 0) : 0;
    }

    public int reward() {
        return 40 + 10 * Math.min(streak() + (doneToday() ? 0 : 1), 10);
    }

    /** Marks today's challenge done and returns the Taler earned. */
    public int complete() {
        int reward = reward();
        int streak = streak() + 1;
        prefs.edit().putInt("done", today()).putInt("streak", streak).apply();
        return reward;
    }
}
