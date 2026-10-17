package de.kgoh.wackelpopo;

/**
 * Multi-finger grips: fingers that land together count as one "Griff", i.e. one klatsch times the
 * grip's small multiplier (not one klatsch per finger). How many land left and right of the popo decides
 * the name and the multiplier.
 */
public final class Chords {
    public static final class Chord {
        /** Name in the language that was on when the grip landed; key stays the same in both. */
        public final String key, name;
        public final double multiplier;
        /** 0 = small, 1 = medium, 2 = big, 3 = huge: picks how wild the reaction is. */
        public final int tier;

        Chord(String key, String name, double multiplier, int tier) {
            this.key = key;
            this.name = name;
            this.multiplier = multiplier;
            this.tier = tier;
        }
    }

    private Chords() {
    }

    /** Returns the grip for this many fingers left/right, or null for a single finger. */
    public static Chord of(int left, int right) {
        int n = left + right;
        if (n < 2) return null;
        int lo = Math.min(left, right), hi = Math.max(left, right);
        String key = left + ":" + right;
        String side = left > right ? Lang.t("links", "left") : Lang.t("rechts", "right");
        if (n == 2) {
            if (lo == 1) return new Chord(key, Lang.t("Doppel-Klatsch!", "Double Slap!"), 1.1, 0);
            return new Chord(key, Lang.t("Zwillinge " + side + "!", "Twins " + side + "!"), 1.05, 0);
        }
        if (n == 3) {
            if (lo == 1) return new Chord(key, Lang.t("Dreier-Polka!", "Triple Polka!"), 1.15, 0);
            return new Chord(key, Lang.t("Dreizack " + side + "!", "Trident " + side + "!"), 1.1, 0);
        }
        if (n == 4) {
            if (lo == 2) return new Chord(key, Lang.t("Symmetrie-Klatsch!", "Symmetry Slap!"), 1.25, 1);
            if (lo == 1) return new Chord(key, Lang.t("Schiefer Turm!", "Leaning Tower!"), 1.2, 1);
            return new Chord(key, Lang.t("Vier-Finger-Wedel " + side + "!", "Four-Finger Fan " + side + "!"), 1.15, 1);
        }
        if (n == 5) {
            if (lo == 2) return new Chord(key, Lang.t("Fünf-Sterne-Klatsch!", "Five-Star Slap!"), 1.3, 1);
            if (lo == 1) return new Chord(key, Lang.t("Handschlag-Hammer!", "Handshake Hammer!"), 1.25, 1);
            return new Chord(key, Lang.t("Ganze Hand " + side + "!", "Whole Hand " + side + "!"), 1.2, 1);
        }
        if (n == 6) {
            if (lo == 3) return new Chord(key, Lang.t("Hexen-Klatsch!", "Hex Slap!"), 1.45, 2);
            if (lo == 2) return new Chord(key, Lang.t("Sechser-Salve!", "Six-Shot Salvo!"), 1.4, 2);
            return new Chord(key, Lang.t("Sechser-Sturm!", "Six-Finger Storm!"), 1.3, 2);
        }
        if (n == 7) {
            if (lo == 3) return new Chord(key, Lang.t("Glückliche Sieben!", "Lucky Seven!"), 1.5, 2);
            return new Chord(key, Lang.t("Sieben auf einen Streich!", "Seven in One Blow!"), 1.45, 2);
        }
        if (n == 8) {
            if (lo == 4) return new Chord(key, Lang.t("OKTOPUS-KLATSCH!", "OCTOPUS SLAP!"), 1.6, 3);
            return new Chord(key, Lang.t("Achterbahn!", "Rollercoaster!"), 1.55, 3);
        }
        if (n == 9) return new Chord(key, Lang.t("Neunmalklug!", "Cloud Nine!"), 1.7, 3);
        if (lo == 5 && hi == 5) return new Chord(key, Lang.t("ZEHN-FINGER-GEWITTER!", "TEN-FINGER THUNDER!"), 1.8, 3);
        return new Chord(key, Lang.t("Finger-Lawine!", "Finger Avalanche!"), 1.7, 3);
    }
}
