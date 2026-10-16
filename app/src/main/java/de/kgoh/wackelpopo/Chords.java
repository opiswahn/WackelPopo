package de.kgoh.wackelpopo;

/**
 * Multi-finger grips: fingers that land together count as one "Griff", i.e. one klatsch times the
 * grip's multiplier (not one klatsch per finger). How many land left and right of the popo decides
 * the name and the multiplier.
 */
public final class Chords {
    public static final class Chord {
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
        String side = left > right ? "links" : "rechts";
        if (n == 2) {
            if (lo == 1) return new Chord(key, "Doppel-Klatsch!", 1.4, 0);
            return new Chord(key, "Zwillinge " + side + "!", 1.2, 0);
        }
        if (n == 3) {
            if (lo == 1) return new Chord(key, "Dreier-Polka!", 1.6, 0);
            return new Chord(key, "Dreizack " + side + "!", 1.4, 0);
        }
        if (n == 4) {
            if (lo == 2) return new Chord(key, "Symmetrie-Klatsch!", 2, 1);
            if (lo == 1) return new Chord(key, "Schiefer Turm!", 1.8, 1);
            return new Chord(key, "Vier-Finger-Wedel " + side + "!", 1.6, 1);
        }
        if (n == 5) {
            if (lo == 2) return new Chord(key, "Fünf-Sterne-Klatsch!", 2.2, 1);
            if (lo == 1) return new Chord(key, "Handschlag-Hammer!", 2, 1);
            return new Chord(key, "Ganze Hand " + side + "!", 1.8, 1);
        }
        if (n == 6) {
            if (lo == 3) return new Chord(key, "Hexen-Klatsch!", 2.6, 2);
            if (lo == 2) return new Chord(key, "Sechser-Salve!", 2.4, 2);
            return new Chord(key, "Sechser-Sturm!", 2.2, 2);
        }
        if (n == 7) {
            if (lo == 3) return new Chord(key, "Glückliche Sieben!", 2.8, 2);
            return new Chord(key, "Sieben auf einen Streich!", 2.6, 2);
        }
        if (n == 8) {
            if (lo == 4) return new Chord(key, "OKTOPUS-KLATSCH!", 3.2, 3);
            return new Chord(key, "Achterbahn!", 3, 3);
        }
        if (n == 9) return new Chord(key, "Neunmalklug!", 3.5, 3);
        if (lo == 5 && hi == 5) return new Chord(key, "ZEHN-FINGER-GEWITTER!", 4, 3);
        return new Chord(key, "Finger-Lawine!", 3.6, 3);
    }
}
