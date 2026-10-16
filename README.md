# Wackel-Popo

Ein Comic-Popo-Klatschspiel. Jedes Tippen klatscht den **Popo-Klatsch-Counter** hoch, aber der
Popo kühlt ab: Der Counter sinkt ständig, und zwar immer schneller (die Abkühlung verdoppelt sich
etwa jede Minute). Fällt der Counter auf **-10**, ist das Spiel vorbei.

**Shop** (Einkaufswagen oben links, nur im Spiel): Counter-Punkte gegen Upgrades tauschen. Die
Punkte sind dann weg, und der Counter sinkt währenddessen weiter. Preise steigen pro Stufe exponentiell.

| Upgrade | Wirkung | Startpreis | Preisfaktor |
|---|---|---|---|
| 👏 Doppelhand | +1 pro Klatsch | 15 | x1,6 |
| 🤖 Auto-Klatscher | +1 pro Sekunde von allein | 30 | x1,6 |
| 🧴 Popo-Creme | Counter sinkt 15 % langsamer (max. 10) | 50 | x1,9 |
| ⚡ Kritischer Klatscher | +5 % Chance auf x5-Klatsch (max. 10) | 80 | x1,7 |
| 🔥 Combo-Turbo | +20 % bei schnellem Tippen (max. 20) | 120 | x1,8 |
| 👑 Goldener Popo | alles x1,3 (max. 10) | 500 | x4 |

**100 Awards** (Pokal oben rechts → Reiter „Awards“): freigeschaltet über die Gesamt-Klatscher eines
Spiels. Die Schwelle wächst pro Award um 22 %: Award 1 bei 25, Award 10 bei ~150, Award 30 bei ~8.000,
Award 50 bei ~430.000, Award 70 bei ~23 Mio., Award 100 bei ~9 Mrd. Jeder Award hat einen eigenen
Namen und löst einen von 12 Effekten aus (Konfetti, Regenbogen-Pups, Krone, Disco, Rakete, Feuerwerk,
Herzchen, Riesen-Popo, Sternschnuppen, Seifenblasen, Goldmünzen, Erdbeben), je später desto mehr davon.
Die Namen stehen in `app/src/main/java/de/kgoh/wackelpopo/Awards.java`.

**Mehrfinger-Griffe:** Alle Finger werden erkannt (bis 10). Finger, die fast gleichzeitig landen, bilden
einen Griff. Ein Griff zählt wie **ein** Klatscher mal dem Griff-Faktor (nicht ein Klatscher pro Finger).
Links/rechts der Popo-Mitte entscheidet den Namen. Den gleichen Griff schnell wiederholen gibt eine
Serie (+10 % pro Wiederholung, bis +40 %).

| Finger (links:rechts) | Griff | Faktor |
|---|---|---|
| 1:1 | Doppel-Klatsch | x1,4 |
| 2:0 / 0:2 | Zwillinge | x1,2 |
| 2:1 | Dreier-Polka | x1,6 |
| 3:0 | Dreizack | x1,4 |
| 2:2 | Symmetrie-Klatsch | x2 |
| 3:1 | Schiefer Turm | x1,8 |
| 4:0 | Vier-Finger-Wedel | x1,6 |
| 3:2 | Fünf-Sterne-Klatsch | x2,2 |
| 4:1 | Handschlag-Hammer | x2 |
| 5:0 | Ganze Hand | x1,8 |
| 3:3 | Hexen-Klatsch | x2,6 |
| 4:2 | Sechser-Salve | x2,4 |
| 5:1 | Sechser-Sturm | x2,2 |
| 4:3 | Glückliche Sieben | x2,8 |
| 5:2 | Sieben auf einen Streich | x2,6 |
| 4:4 | OKTOPUS-KLATSCH | x3,2 |
| 5:3 | Achterbahn | x3 |
| 5:4 | Neunmalklug | x3,5 |
| 5:5 | ZEHN-FINGER-GEWITTER | x4 |

**Fieber-Modus:** 2,5 Sekunden lang sehr schnell tippen (7+ pro Sekunde) → Sonnenstrahlen-Hintergrund,
roter Popo, Dauertanz und alle Klatscher x1,5, bis man nachlässt.

**Bonus-Stern:** Alle 30 bis 60 Sekunden fliegt ein goldener Stern durchs Bild. Wer ihn antippt, bekommt einen dicken Bonus.

**Popo-Bewegungen:** 22 Bewegungen (Hops, Doppelhops, Schütteln, Kreisen, Pirouette, Pendel, Wackelpudding,
Salto, Moonwalk, Stampfen, Quetschen, Pulsieren, Zappeln, Welle, Seitsprung, Schwindelig mit Sternchen,
Sprungfeder, Rutschpartie, Verbeugung, Hula, Fußtippen, Kick). Nie zweimal dieselbe hintereinander; die
Beine heben sich dabei. Ohne Tippen macht der Popo nach 3 Sekunden kleine Leerlauf-Tänze. Mit jedem Award
wechselt das Outfit: 8 Hosenfarben, 8 Muster (Punkte, Streifen, Herzen, Sterne, Karo, Zickzack, Blumen,
Bund) und 7 Turnschuhfarben. Dazu rote Handabdrücke, ein Mini-Effekt an jedem Finger, ein blau
frierender Popo im Minus und ein schlafender Popo nach dem Spiel.

**Wörter:** über 120 Comic-Wörter (60 Klatsch-Laute, 34 Lobsprüche, Gefahr-, Shop- und Rettungs-Sprüche)
in 7 Animationsarten: Plopp, Wackeln, Heranzoomen, Bogenflug, Zittern, Regenbogen-Welle, Gummi-Dehnen.

**30 Spezial-Awards** (lila Sterne im Award-Raster, antippen zeigt die Bedingung): z. B. Oktopus (4+4 Finger),
Zehn-Finger-Gott, Rhythmus-Meister, Fieberwahn, Tippmaschine (12 pro Sekunde), Eiserner Popo (10 Minuten),
Haarscharf (unter -9 und gerettet), Comeback-Kid, Geizhals, Dagobert, Sternenfänger, Nachteule, Popo-Veteran.
Insgesamt jetzt 130 Awards. Liste in `Awards.java`.

**Bestenliste:** Top 10 Spiele nach Gesamt-Klatschern, mit Datum.

- `app/` – Android-Projekt (Java, eine eigene `View`, die alles auf ein Canvas zeichnet; keine Bibliotheken)
- `wackel-popo-debug.apk` – fertiges Debug-APK (minSdk 24 / Android 7, targetSdk 35, v2-signiert mit Debug-Schlüssel)
- `tools/` – Build ohne Android-SDK (siehe unten)

## Installieren
APK aufs Handy kopieren, öffnen und „Installation aus unbekannten Quellen“ erlauben,
oder per `adb install wackel-popo-debug.apk`.

## Selbst bauen
Normal mit Android Studio öffnen, oder `./gradlew assembleDebug` (braucht Android-SDK).

Das beiliegende APK wurde ohne SDK gebaut, weil die Build-Umgebung keinen Zugriff auf
Googles Server hatte: `tools/build-ohne-sdk.sh` (javac + dx + `tools/mkapk.py`, das
Manifest, Ressourcen und v2-Signatur selbst schreibt).
