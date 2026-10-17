package de.kgoh.wackelpopo;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Things the popo can wear for good: hats, glasses and shorts, bought with Popo-Taler. Taler come
 * from finished games (1 per 500 Klatscher), beaten bosses and daily challenges.
 */
public final class Wardrobe {
    public static final int HATS = 0, GLASSES = 1, SHORTS = 2;
    public static final String[] CATEGORIES = {"Hüte", "Brillen", "Hosen"};
    public static final String[] CATEGORIES_EN = {"Hats", "Glasses", "Shorts"};

    /** Per category: icon, name, price in Taler. Index 0 is always "nothing" or "surprise". */
    public static final String[][][] ITEMS = {
            {{"✖", "Ohne", "0"}, {"🧢", "Kappe", "30"}, {"🎉", "Partyhut", "60"}, {"🤠", "Cowboyhut", "100"},
                    {"🎩", "Zylinder", "150"}, {"⚔", "Wikingerhelm", "250"}},
            {{"✖", "Ohne", "0"}, {"🕶", "Sonnenbrille", "40"}, {"🤓", "Nerdbrille", "70"}, {"😍", "Herzbrille", "120"}},
            {{"🎲", "Jedes Spiel neu", "0"}, {"🔵", "Blau", "15"}, {"🔴", "Rot", "15"}, {"🟢", "Grün", "20"},
                    {"🟣", "Lila", "25"}, {"🟠", "Orange", "25"}, {"🩵", "Türkis", "30"}, {"🩷", "Pink", "30"},
                    {"⚫", "Schwarz", "40"}},
    };

    /** English twin of ITEMS: same icons and prices, only the names translated. */
    public static final String[][][] ITEMS_EN = {
            {{"✖", "None", "0"}, {"🧢", "Cap", "30"}, {"🎉", "Party Hat", "60"}, {"🤠", "Cowboy Hat", "100"},
                    {"🎩", "Top Hat", "150"}, {"⚔", "Viking Helmet", "250"}},
            {{"✖", "None", "0"}, {"🕶", "Sunglasses", "40"}, {"🤓", "Nerd Glasses", "70"}, {"😍", "Heart Glasses", "120"}},
            {{"🎲", "New every game", "0"}, {"🔵", "Blue", "15"}, {"🔴", "Red", "15"}, {"🟢", "Green", "20"},
                    {"🟣", "Purple", "25"}, {"🟠", "Orange", "25"}, {"🩵", "Turquoise", "30"}, {"🩷", "Pink", "30"},
                    {"⚫", "Black", "40"}},
    };

    private final SharedPreferences prefs;

    public Wardrobe(Context context) {
        prefs = context.getSharedPreferences("wardrobe", Context.MODE_PRIVATE);
    }

    public long taler() {
        return prefs.getLong("taler", 0);
    }

    public void addTaler(long n) {
        prefs.edit().putLong("taler", taler() + n).apply();
    }

    public boolean owned(int cat, int item) {
        return item == 0 || prefs.getBoolean("own_" + cat + "_" + item, false);
    }

    public int worn(int cat) {
        return prefs.getInt("wear_" + cat, 0);
    }

    public static int price(int cat, int item) {
        return Integer.parseInt(ITEMS[cat][item][2]);
    }

    /** Wears the item, buying it first if needed; returns false if the Taler are not enough. */
    public boolean pick(int cat, int item) {
        if (!owned(cat, item)) {
            if (taler() < price(cat, item)) return false;
            prefs.edit().putLong("taler", taler() - price(cat, item)).putBoolean("own_" + cat + "_" + item, true).apply();
        }
        prefs.edit().putInt("wear_" + cat, item).apply();
        return true;
    }

    /** Gives a random item not yet owned (boss loot); returns its name, or null if everything is owned. */
    public String gift(java.util.Random rnd) {
        java.util.List<int[]> missing = new java.util.ArrayList<>();
        for (int cat = 0; cat < ITEMS.length; cat++) {
            for (int item = 1; item < ITEMS[cat].length; item++) {
                if (!owned(cat, item)) missing.add(new int[]{cat, item});
            }
        }
        if (missing.isEmpty()) return null;
        int[] got = missing.get(rnd.nextInt(missing.size()));
        prefs.edit().putBoolean("own_" + got[0] + "_" + got[1], true).apply();
        return Lang.pick(ITEMS, ITEMS_EN)[got[0]][got[1]][1];
    }
}
