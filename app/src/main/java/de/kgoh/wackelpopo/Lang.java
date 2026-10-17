package de.kgoh.wackelpopo;

/**
 * German or English. Every text in the game is written as a pair, and the switch takes effect on the next
 * frame, so it works in the middle of a round too.
 */
public final class Lang {
    /** True for English; WackelView loads and saves it with the other settings. */
    public static volatile boolean en;

    private Lang() {
    }

    /** The German or the English text, whichever is switched on. */
    public static String t(String de, String english) {
        return en ? english : de;
    }

    /** Same for whole lists: German and English arrays must have the same length and order. */
    public static <T> T pick(T de, T english) {
        return en ? english : de;
    }
}
