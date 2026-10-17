package de.kgoh.wackelpopo;

import java.util.Random;

/** All the comic words that pop up while playing. */
public final class Words {
    public static final String[] PRAISE = {
            "Weiter so!", "Nicht nachlassen!", "Wackel-Wahnsinn!", "Unaufhaltsam!", "Was für ein Tempo!", "Popo-Power!",
            "Der Popo glüht!", "Klatsch-Gewitter!", "Mega!", "Unglaublich!", "Göttlich!", "Legendär!",
            "Rekordverdächtig!", "Feuer frei!", "Volle Kanne!", "Turbo-Popo!", "Die Backen beben!", "Hammerhart!",
            "Wahnsinn!", "Spitzenklasse!", "Weltklasse!", "Popo-Party!", "Gib alles!", "Bloß nicht aufhören!",
            "Grandios!", "Fantastisch!", "Sensationell!", "Bombastisch!", "Atemberaubend!", "Klatschtastisch!",
            "Der Wahnsinn geht weiter!", "Popo im Rausch!", "Heißer Hintern!", "Das ist Kunst!",
    };

    public static final String[] PRAISE_EN = {
            "Keep it up!", "Don't stop now!", "Wobble madness!", "Unstoppable!", "What a pace!", "Booty Power!",
            "The booty is glowing!", "Slap storm!", "Mega!", "Unbelievable!", "Divine!", "Legendary!",
            "Record material!", "Fire away!", "Full throttle!", "Turbo Booty!", "The cheeks are quaking!", "Hammer time!",
            "Insane!", "Top class!", "World class!", "Booty party!", "Give it all!", "Don't you dare stop!",
            "Magnificent!", "Fantastic!", "Sensational!", "Bombastic!", "Breathtaking!", "Slaptastic!",
            "The madness goes on!", "Booty on a roll!", "Hot booty!", "That's art!",
    };

    public static final String[] DANGER = {
            "Hilfe!", "Er schläft ein!", "Aufwachen!", "Schnell!", "Nicht aufgeben!", "Fast vorbei!",
            "Rette den Popo!", "Brrr, kalt hier!", "Bibber!", "Jetzt aber!", "Gas geben!", "Popo-Alarm!",
    };

    public static final String[] DANGER_EN = {
            "Help!", "It's dozing off!", "Wake up!", "Quick!", "Don't give up!", "Almost over!",
            "Save the booty!", "Brrr, it's cold!", "Shiver!", "Now or never!", "Step on it!", "Booty alert!",
    };

    public static final String[] BUY = {
            "Gekauft!", "Upgrade!", "Ka-Tsching!", "Investiert!", "Shoppen!", "Stark!", "Neue Power!", "Level up!",
            "Schnäppchen!", "Aufgerüstet!",
    };

    public static final String[] BUY_EN = {
            "Bought!", "Upgrade!", "Ka-ching!", "Invested!", "Shopping!", "Nice!", "New power!", "Level up!",
            "Bargain!", "Powered up!",
    };

    public static final String[] RESCUE = {"Gerettet!", "Puh, knapp!", "Comeback!", "Zurück im Spiel!", "Der Popo lebt!"};
    public static final String[] RESCUE_EN = {"Saved!", "Phew, close one!", "Comeback!", "Back in the game!",
            "The booty lives!"};

    /** Number of distinct animation styles a word can use (see WackelView.drawWord). */
    public static final int STYLES = 7;

    private Words() {
    }

    public static String pick(String[] pool, Random rnd) {
        return pool[rnd.nextInt(pool.length)];
    }
}
