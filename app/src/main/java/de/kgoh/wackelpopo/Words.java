package de.kgoh.wackelpopo;

import java.util.Random;

/** All the comic words that pop up while playing. */
public final class Words {
    public static final String[] TAP = {
            "KLATSCH!", "PATSCH!", "BOING!", "WOBBEL!", "SCHWABBEL!", "PLOPP!", "ZACK!", "BAMM!", "WUMMS!", "POFF!",
            "KAWUMM!", "FLATSCH!", "SPLATSCH!", "ZONK!", "WUSCH!", "BÄMM!", "PENG!", "KRACH!", "SCHMATZ!", "BLUBB!",
            "FLUTSCH!", "HUI!", "JUHU!", "HOPPLA!", "BOING-BOING!", "WABBEL!", "SCHLABBER!", "GLIBBER!", "DOING!", "PLONK!",
            "KLONK!", "TATSCH!", "KNUFF!", "SCHUBS!", "RUMMS!", "BUMM!", "SCHWUPP!", "ZING!", "ZISCH!", "PUFF!",
            "PIEKS!", "KLAPS!", "WACKEL!", "HOPS!", "FLUPP!", "WIPP-WIPP!", "GLUCKS!", "OHA!", "YEAH!", "WOW!",
            "SCHNAPP!", "BRUMM!", "TOCK!", "PITSCH!", "KLIRR!", "SURR!", "HUSCH!", "WHAMM!", "BAZONG!", "KABLAMM!",
    };

    public static final String[] PRAISE = {
            "Weiter so!", "Nicht nachlassen!", "Wackel-Wahnsinn!", "Unaufhaltsam!", "Was für ein Tempo!", "Popo-Power!",
            "Der Popo glüht!", "Klatsch-Gewitter!", "Mega!", "Unglaublich!", "Göttlich!", "Legendär!",
            "Rekordverdächtig!", "Feuer frei!", "Volle Kanne!", "Turbo-Popo!", "Die Backen beben!", "Hammerhart!",
            "Wahnsinn!", "Spitzenklasse!", "Weltklasse!", "Popo-Party!", "Gib alles!", "Bloß nicht aufhören!",
            "Grandios!", "Fantastisch!", "Sensationell!", "Bombastisch!", "Atemberaubend!", "Klatschtastisch!",
            "Der Wahnsinn geht weiter!", "Popo im Rausch!", "Heißer Hintern!", "Das ist Kunst!",
    };

    public static final String[] DANGER = {
            "Hilfe!", "Er schläft ein!", "Aufwachen!", "Schnell!", "Nicht aufgeben!", "Fast vorbei!",
            "Rette den Popo!", "Brrr, kalt hier!", "Bibber!", "Jetzt aber!", "Gas geben!", "Popo-Alarm!",
    };

    public static final String[] BUY = {
            "Gekauft!", "Upgrade!", "Ka-Tsching!", "Investiert!", "Shoppen!", "Stark!", "Neue Power!", "Level up!",
            "Schnäppchen!", "Aufgerüstet!",
    };

    public static final String[] RESCUE = {"Gerettet!", "Puh, knapp!", "Comeback!", "Zurück im Spiel!", "Der Popo lebt!"};

    /** Number of distinct animation styles a word can use (see WackelView.drawWord). */
    public static final int STYLES = 7;

    private Words() {
    }

    public static String pick(String[] pool, Random rnd) {
        return pool[rnd.nextInt(pool.length)];
    }
}
