package de.kgoh.wackelpopo;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Random;

/**
 * The long game: experience from every hit levels the player up over many rounds, and three missions are
 * always open. A mission is done within a single game; it pays experience and Taler and is replaced by a
 * new one, a bit harder, when the next game starts. Every five levels bring a new title and more start points.
 */
public final class Career {
    public static final int PERFECTS = 0, COMBO = 1, SURVIVE = 2, BOSSES = 3, GOODIES = 4, SLAPS = 5,
            OFFBEATS = 6, FEVERS = 7, KINDS = 8;
    public static final int SLOTS = 3, MAX_LEVEL = 50;
    private static final String[] TITLES = {"Popo-Neuling", "Klatsch-Lehrling", "Takt-Tänzer", "Backen-Profi",
            "Rhythmus-Ritter", "Groove-Graf", "Beat-Baron", "Klatsch-König", "Popo-Legende", "Wackel-Gott"};
    private static final String[] TITLES_EN = {"Booty Rookie", "Slap Apprentice", "Beat Dancer", "Cheek Pro",
            "Rhythm Knight", "Groove Count", "Beat Baron", "Slap King", "Booty Legend", "Wobble God"};

    private final SharedPreferences prefs;
    private final Random rnd = new Random();
    private int level;
    private long xp;
    private final int[] kind = new int[SLOTS];
    private final long[] target = new long[SLOTS];
    private final boolean[] done = new boolean[SLOTS];

    public Career(Context context) {
        prefs = context.getSharedPreferences("career", Context.MODE_PRIVATE);
        level = prefs.getInt("level", 1);
        xp = prefs.getLong("xp", 0);
        for (int i = 0; i < SLOTS; i++) {
            kind[i] = prefs.getInt("kind" + i, -1);
            target[i] = prefs.getLong("target" + i, 0);
            done[i] = prefs.getBoolean("done" + i, false);
            if (kind[i] < 0) newMission(i);
        }
    }

    public int level() {
        return level;
    }

    /** Experience collected within the current level, and what the level needs in total. */
    public long xp() {
        return xp;
    }

    public long xpNeeded() {
        return 100 + 50L * (level - 1);
    }

    public String title() {
        return Lang.pick(TITLES, TITLES_EN)[Math.min(TITLES.length - 1, (level - 1) / 5)];
    }

    /** Extra points every game starts with: five more for every five levels. */
    public int startBonus() {
        return 5 * (level / 5);
    }

    /** Adds experience; returns how many levels that gained (each one already paid out in Taler). */
    public int addXp(long amount, Wardrobe wardrobe) {
        if (level >= MAX_LEVEL) return 0;
        xp += amount;
        int gained = 0;
        while (level < MAX_LEVEL && xp >= xpNeeded()) {
            xp -= xpNeeded();
            level++;
            gained++;
            wardrobe.addTaler(5 + level);
        }
        if (gained > 0) save();
        return gained;
    }

    public int kind(int slot) {
        return kind[slot];
    }

    public long target(int slot) {
        return target[slot];
    }

    public boolean done(int slot) {
        return done[slot];
    }

    public long xpReward() {
        return 40 + 8L * level;
    }

    public int talerReward() {
        return 3 + level / 2;
    }

    /** What a mission asks for, in the active language. */
    public String text(int slot) {
        long n = target[slot];
        switch (kind[slot]) {
            case PERFECTS:
                return Lang.t(n + "× PERFEKT in einem Spiel", n + "× PERFECT in one game");
            case COMBO:
                return Lang.t("Combo " + n + " erreichen", "Reach combo " + n);
            case SURVIVE:
                return Lang.t(time(n) + " überleben", "Survive " + time(n));
            case BOSSES:
                return n == 1 ? Lang.t("Einen Boss besiegen", "Defeat a boss")
                        : Lang.t(n + " Bosse in einem Spiel", n + " bosses in one game");
            case GOODIES:
                return Lang.t(n + " Goodies fangen", "Catch " + n + " goodies");
            case SLAPS:
                return Lang.t(n + " Klatscher in einem Spiel", n + " slaps in one game");
            case OFFBEATS:
                return Lang.t(n + " kleine Ringe treffen", "Hit " + n + " small rings");
            default:
                return n == 1 ? Lang.t("Fieber-Modus erreichen", "Reach fever mode")
                        : Lang.t(n + "× Fieber in einem Spiel", n + "× fever in one game");
        }
    }

    private static String time(long seconds) {
        return seconds % 60 == 0 ? (seconds / 60) + " min" : (seconds / 60) + ":" + String.format("%02d", seconds % 60) + " min";
    }

    /**
     * Checks the open missions against this game's numbers (indexed by kind) and writes the slots finished
     * just now into finished. Their Taler are paid here; the caller adds the experience, so level-ups show.
     */
    public int check(long[] stats, Wardrobe wardrobe, int[] finished) {
        int count = 0;
        for (int i = 0; i < SLOTS; i++) {
            if (done[i] || stats[kind[i]] < target[i]) continue;
            done[i] = true;
            wardrobe.addTaler(talerReward());
            finished[count++] = i;
        }
        if (count > 0) save();
        return count;
    }

    /** Called when a game starts: finished missions make room for new ones. */
    public void refresh() {
        for (int i = 0; i < SLOTS; i++) if (done[i]) newMission(i);
        save();
    }

    private void newMission(int slot) {
        int k;
        do {
            k = rnd.nextInt(KINDS);
        } while (taken(k, slot) || (k == OFFBEATS && level < 3) || (k == BOSSES && level < 2));
        int l = level;
        long[] goals = {
                Math.min(150, 10 + 3 * l), Math.min(80, 12 + 2 * l), Math.min(600, 90 + 15 * l),
                Math.min(4, 1 + l / 8), Math.min(6, 2 + l / 10), 1500 + 600L * l, Math.min(40, 4 + l),
                Math.min(4, 1 + l / 12)};
        kind[slot] = k;
        target[slot] = goals[k];
        done[slot] = false;
    }

    private boolean taken(int k, int slot) {
        for (int i = 0; i < SLOTS; i++) if (i != slot && kind[i] == k) return true;
        return false;
    }

    public void save() {
        SharedPreferences.Editor e = prefs.edit().putInt("level", level).putLong("xp", xp);
        for (int i = 0; i < SLOTS; i++) {
            e.putInt("kind" + i, kind[i]).putLong("target" + i, target[i]).putBoolean("done" + i, done[i]);
        }
        e.apply();
    }
}
