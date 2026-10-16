package de.kgoh.wackelpopo;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * The 100 awards. Each one unlocks when a game's total Klatscher reach its threshold; thresholds
 * grow by 22% per award, so the first come quickly and the last ones take serious upgrading.
 */
public final class Awards {
    public static final String[] NAMES = {
            // 1-10: Anfänger
            "Erster Klatscher", "Pobacken-Praktikant", "Wackel-Lehrling", "Klatsch-Kadett", "Hintern-Held",
            "Schwabbel-Schüler", "Popo-Pionier", "Backen-Bändiger", "Wackelpudding-Wächter", "Klatsch-Kapitän",
            // 11-20
            "Pfirsich-Prinz", "Gummiball-Guru", "Hüftschwung-Hero", "Po-Poet", "Sitzfleisch-Sensei",
            "Doppelbacken-Diplom", "Schwabbel-Sheriff", "Klatschmohn-Gärtner", "Turnhosen-Titan", "Wackel-Wunderkind",
            // 21-30
            "Hosenboden-Häuptling", "Popo-Professor", "Backpfeifen-Baron", "Wobbel-Wikinger", "Klatsch-Kommissar",
            "Gelee-General", "Pudding-Pirat", "Hinterteil-Hüpfer", "Plumps-Pilot", "Rundungs-Ritter",
            // 31-40
            "Wackel-Weltenbummler", "Popo-Paparazzo", "Schaukel-Schamane", "Bürzel-Boss", "Klatsch-Kaiser",
            "Federkern-Fürst", "Schwabbel-Samurai", "Hopsasa-Herzog", "Pobacken-Pilot", "Kissen-Kanzler",
            // 41-50
            "Wackel-Wizard", "Gummibärchen-Graf", "Trampolin-Tycoon", "Boing-Botschafter", "Schwung-Scheich",
            "Wackel-Weltmeister", "Popo-Präsident", "Klatsch-Komet", "Pfirsich-Pharao", "Halbzeit-Hintern",
            // 51-60
            "Luftpolster-Legende", "Wobbel-Wolkenkratzer", "Hüpfburg-Herrscher", "Wackel-Vulkan", "Schwabbel-Supernova",
            "Popo-Polarstern", "Klatsch-Orkan", "Backen-Blitz", "Wackel-Tornado", "Pudding-Planet",
            // 61-70
            "Galaktischer Gesäß-Guru", "Mond-Moonwalker", "Sternenstaub-Sitzer", "Wackel-Raumfahrer", "Popo-Pulsar",
            "Schwarzes Wackelloch", "Kometen-Klatscher", "Milchstraßen-Möpp", "Nebel-Nudler", "Urknall-Wackler",
            // 71-80
            "Zeitreise-Zappler", "Dino-Discotänzer", "Ritter der Rundung", "Wackel-Wiki-Autor", "Popo-Philosoph",
            "Klatsch-Komponist", "Schwabbel-Symphonie", "Wackel-Oper", "Pudding-Picasso", "Mona Popo",
            // 81-90
            "Wackel-Weltwunder", "Popo-Pyramide", "Eiffel-Wackel-Turm", "Freiheits-Popo", "Großer Popo von Gizeh",
            "Wackel-Atlantis", "Klatsch-Olymp", "Pobacken-Pantheon", "Ewiges Wackeln", "Unendlich-Schwabbel",
            // 91-100
            "Wackel-Gottheit", "Kosmischer Klatscher", "Multiversum-Popo", "Quanten-Wackler", "Popo des Jahrtausends",
            "Hall of Fame-Hintern", "Legendärer Wackelkönig", "Allmächtiger Schwabbel", "Der Eine Popo", "POPO-UNIVERSUM",
    };

    public static final int COUNT = NAMES.length;

    /** Special awards for doing something remarkable rather than reaching a score. */
    public static final String[][] SPECIALS = {
            {"Zweihänder", "Klatsche mit zwei Fingern gleichzeitig"},
            {"Symmetriker", "2 Finger links + 2 rechts"},
            {"Schiefer Turm von Pisa", "3 Finger auf einer Seite, 1 auf der anderen"},
            {"Hexenmeister", "3 Finger links + 3 rechts"},
            {"Oktopus", "4 Finger links + 4 rechts"},
            {"Zehn-Finger-Gott", "5 Finger links + 5 rechts"},
            {"Rhythmus-Meister", "Denselben Griff 5-mal hintereinander"},
            {"Fieberwahn", "Erreiche den Fieber-Modus"},
            {"Dauerfieber", "30 Sekunden am Stück im Fieber-Modus"},
            {"Tippmaschine", "12 Klatscher in einer Sekunde"},
            {"Dauerfeuer", "1.000 Klatscher in einem Spiel"},
            {"Klatsch-Marathon", "5.000 Klatscher in einem Spiel"},
            {"Durchhalter", "Überlebe 3 Minuten"},
            {"Eiserner Popo", "Überlebe 10 Minuten"},
            {"Unsterblich", "Überlebe 20 Minuten"},
            {"Haarscharf", "Rutsche unter -9 und rette dich zurück ins Plus"},
            {"Comeback-Kid", "Aus dem Minus bis auf 1.000 Punkte"},
            {"Shopping-Queen", "25 Upgrades in einem Spiel"},
            {"Geizhals", "500 Punkte auf dem Counter, ohne etwas zu kaufen"},
            {"Dagobert", "1 Million Punkte auf dem Counter"},
            {"Sternenfänger", "Fang einen Bonus-Stern"},
            {"Sternensammler", "Fang 5 Bonus-Sterne in einem Spiel"},
            {"Glückspilz", "3 Krit-Klatscher hintereinander"},
            {"Maxed Out", "Bringe ein Upgrade auf die Höchststufe"},
            {"Goldjunge", "Goldener Popo auf Stufe 5"},
            {"Nachteule", "Spiele zwischen Mitternacht und 4 Uhr"},
            {"Frühaufsteher", "Spiele zwischen 5 und 7 Uhr morgens"},
            {"Stammgast", "Spiele 10 Spiele"},
            {"Popo-Veteran", "Spiele 50 Spiele"},
            {"Sammelwut", "Sammle 50 normale Awards"},
    };

    public static final int TWO_FINGERS = 0, SYMMETRY = 1, PISA = 2, WITCH = 3, OCTOPUS = 4, TEN_FINGERS = 5,
            RHYTHM = 6, FEVER = 7, LONG_FEVER = 8, TAP_MACHINE = 9, TAPS_1000 = 10, TAPS_5000 = 11,
            SURVIVE_3 = 12, SURVIVE_10 = 13, SURVIVE_20 = 14, CLOSE_CALL = 15, COMEBACK = 16, SHOPPER = 17,
            MISER = 18, SCROOGE = 19, STAR = 20, STARS_5 = 21, LUCKY = 22, MAXED = 23, GOLDEN = 24,
            NIGHT_OWL = 25, EARLY_BIRD = 26, REGULAR = 27, VETERAN = 28, COLLECTOR = 29;

    public static final int SPECIAL_COUNT = SPECIALS.length;
    public static final int TOTAL = COUNT + SPECIAL_COUNT;

    private final SharedPreferences prefs;
    private final boolean[] unlocked = new boolean[TOTAL];

    public Awards(Context context) {
        prefs = context.getSharedPreferences("awards", Context.MODE_PRIVATE);
        String raw = prefs.getString("unlocked", "");
        for (int i = 0; i < TOTAL && i < raw.length(); i++) unlocked[i] = raw.charAt(i) == '1';
    }

    /** Total Klatscher needed for award i (0-based): 25, 30, 37, ... about 9 billion for the last. */
    public static long threshold(int i) {
        double raw = 25 * Math.pow(1.22, i);
        // Round to two significant digits so the numbers look tidy.
        double mag = Math.pow(10, Math.max(0, Math.floor(Math.log10(raw)) - 1));
        return (long) (Math.round(raw / mag) * mag);
    }

    /** Index into the combined list (normal awards first, then specials). */
    public boolean isUnlocked(int i) {
        return unlocked[i];
    }

    public int unlockedCount() {
        int n = 0;
        for (boolean b : unlocked) if (b) n++;
        return n;
    }

    public int normalUnlockedCount() {
        int n = 0;
        for (int i = 0; i < COUNT; i++) if (unlocked[i]) n++;
        return n;
    }

    /** Marks award i (combined index) as won; returns true if it is new. */
    public boolean unlock(int i) {
        if (unlocked[i]) return false;
        unlocked[i] = true;
        StringBuilder sb = new StringBuilder();
        for (boolean b : unlocked) sb.append(b ? '1' : '0');
        prefs.edit().putString("unlocked", sb.toString()).apply();
        return true;
    }

    /** Counts a started game and returns how many have been played so far. */
    public int countGame() {
        int games = prefs.getInt("games", 0) + 1;
        prefs.edit().putInt("games", games).apply();
        return games;
    }
}
