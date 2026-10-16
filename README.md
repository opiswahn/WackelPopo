# Wackel-Popo

Ein Comic-Popo-Klatschspiel. Jedes Tippen klatscht den **Popo-Klatsch-Counter** hoch, aber der
Popo kühlt ab: Der Counter sinkt ständig, und zwar immer schneller (die Abkühlung wächst um 1,5 % pro Sekunde,
verdoppelt sich also etwa alle 47 Sekunden). Fällt der Counter auf **-10**, ist das Spiel vorbei.

**Shop** (Einkaufswagen oben links, nur im Spiel): Counter-Punkte gegen Upgrades tauschen. Die
Punkte sind dann weg, und der Counter sinkt währenddessen weiter. Preise steigen pro Stufe exponentiell.

| Upgrade | Wirkung | Startpreis | Preisfaktor |
|---|---|---|---|
| 👏 Doppelhand | +1 pro Klatsch | 25 | x2 |
| 🤖 Auto-Klatscher | +0,5 pro Sekunde von allein | 40 | x2 |
| 🧴 Popo-Creme | Counter sinkt 12 % langsamer (max. 10) | 60 | x2,2 |
| ⚡ Kritischer Klatscher | +4 % Chance auf x3-Klatsch (max. 10) | 100 | x2,2 |
| 🔥 Combo-Turbo | +10 % bei schnellem Tippen (max. 10) | 150 | x2,2 |
| 👑 Goldener Popo | alles +15 % (max. 5) | 400 | x2,5 |

**100 Awards** (Pokal oben rechts → Reiter „Awards“): freigeschaltet über die Klatscher **aller Spiele
zusammen**, jeder Award nur einmal. Man arbeitet sich also über viele Spiele hoch. Schwelle
100 × (Nummer)^2,6: Award 1 bei 100, Award 5 bei ~6.600, Award 10 bei ~40.000, Award 20 bei ~240.000,
Award 50 bei ~2,6 Mio., Award 100 bei ~16 Mio. Ein gutes Spiel (5 Klatscher/s, ~6 Minuten) bringt
~19.000 Klatscher, also etwa 7 Awards im ersten Spiel, ~18 nach 10 Spielen, ~33 nach 50 Spielen.
Seit Version 1.1 zählen Awards über alle Spiele; die alten Punkte-Awards wurden dafür einmal zurückgesetzt. Jeder Award hat einen eigenen
Namen und löst einen von 12 Effekten aus (Konfetti, Regenbogen-Pups, Krone, Disco, Rakete, Feuerwerk,
Herzchen, Riesen-Popo, Sternschnuppen, Seifenblasen, Goldmünzen, Erdbeben), je später desto mehr davon.
Die Namen stehen in `app/src/main/java/de/kgoh/wackelpopo/Awards.java`.

**Mehrfinger-Griffe:** Alle Finger werden erkannt (bis 10). Finger, die fast gleichzeitig landen, bilden
einen Griff. Ein Griff zählt wie **ein** Klatscher mal dem Griff-Faktor (nicht ein Klatscher pro Finger).
Links/rechts der Popo-Mitte entscheidet den Namen. Den gleichen Griff schnell wiederholen gibt eine
Serie (+5 % pro Wiederholung, bis +20 %). Die Griff-Effekte sind bewusst klein gehalten.

| Finger (links:rechts) | Griff | Faktor |
|---|---|---|
| 1:1 | Doppel-Klatsch | x1,1 |
| 2:0 / 0:2 | Zwillinge | x1,05 |
| 2:1 | Dreier-Polka | x1,15 |
| 3:0 | Dreizack | x1,1 |
| 2:2 | Symmetrie-Klatsch | x1,25 |
| 3:1 | Schiefer Turm | x1,2 |
| 4:0 | Vier-Finger-Wedel | x1,15 |
| 3:2 | Fünf-Sterne-Klatsch | x1,3 |
| 4:1 | Handschlag-Hammer | x1,25 |
| 5:0 | Ganze Hand | x1,2 |
| 3:3 | Hexen-Klatsch | x1,45 |
| 4:2 | Sechser-Salve | x1,4 |
| 5:1 | Sechser-Sturm | x1,3 |
| 4:3 | Glückliche Sieben | x1,5 |
| 5:2 | Sieben auf einen Streich | x1,45 |
| 4:4 | OKTOPUS-KLATSCH | x1,6 |
| 5:3 | Achterbahn | x1,55 |
| 5:4 | Neunmalklug | x1,7 |
| 5:5 | ZEHN-FINGER-GEWITTER | x1,8 |

**Fieber-Modus:** 2,5 Sekunden lang sehr schnell tippen (8+ pro Sekunde) → Sonnenstrahlen-Hintergrund,
roter Popo, Dauertanz und alle Klatscher x1,25, bis man nachlässt.

**Bonus-Stern:** Alle 30 bis 60 Sekunden fliegt ein goldener Stern durchs Bild. Wer ihn antippt, bekommt einen dicken Bonus.

**Goodies (ab 1.2):** Alle 15 bis 30 Sekunden schwebt an einem kleinen Fallschirm ein Goodie herunter.
Man hat etwa 5 Sekunden, es anzutippen, bevor es landet. Laufende Goodies stehen mit Restzeit unter dem Shop-Knopf.

| Goodie | Wirkung |
|---|---|
| 🍑 Pfirsich | Bonus-Punkte (wächst mit Abkühlung und Klatsch-Stärke) |
| 🧊 Frostpause | Counter sinkt 8 Sekunden lang gar nicht |
| 🍫 Schoko-Power | Klatscher x2 für 10 Sekunden |
| 🍀 Glücksklee | +25 % Krit-Chance für 10 Sekunden |
| 🎁 Geschenk | Gratis-Stufe für das günstigste Upgrade |
| 🤖 Klatsch-Roboter | +3 Auto-Klatscher pro Sekunde für 10 Sekunden |
| 🧲 Sternen-Magnet | Der nächste Bonus-Stern kommt sofort |
| 💨 Stinkbombe | Falle! Antippen kostet 15 % vom Counter, also lieber fallen lassen |

**Musik (ab 1.2):** Ein 80er-Retro-Metal-Loop (E-Moll, 152 BPM, Galopp-Riff, Doublebass, Square-Lead mit
Echo), komplett im Code erzeugt (`MetalLoop.java`), keine Audiodatei. Der Knopf 🎸/🔇 unter dem Pokal
schaltet sie an und aus. Die Lautstärke folgt dem Spiel: leise im Menü, im Shop und nach Game Over, lauter beim
schnellen Klatschen, im Minus und am lautesten im Fieber- und Disco-Modus, mit sanftem Übergang.

**Popo-Bewegungen:** 22 Bewegungen (Hops, Doppelhops, Schütteln, Kreisen, Pirouette, Pendel, Wackelpudding,
Salto, Moonwalk, Stampfen, Quetschen, Pulsieren, Zappeln, Welle, Seitsprung, Schwindelig mit Sternchen,
Sprungfeder, Rutschpartie, Verbeugung, Hula, Fußtippen, Kick). Nie zweimal dieselbe hintereinander; die
Beine heben sich dabei. Ohne Tippen macht der Popo nach 3 Sekunden kleine Leerlauf-Tänze. Mit jedem Award
wechselt das Outfit: 8 Hosenfarben, 8 Muster (Punkte, Streifen, Herzen, Sterne, Karo, Zickzack, Blumen,
Bund) und 7 Turnschuhfarben. Dazu rote Handabdrücke, ein Mini-Effekt an jedem Finger, ein blau
frierender Popo im Minus und ein schlafender Popo nach dem Spiel.

**Wörter:** über 120 Comic-Wörter (60 Klatsch-Laute, 34 Lobsprüche, Gefahr-, Shop- und Rettungs-Sprüche)
in 7 Animationsarten: Plopp, Wackeln, Heranzoomen, Bogenflug, Zittern, Regenbogen-Welle, Gummi-Dehnen.

**34 Spezial-Awards** (lila Sterne im Award-Raster, antippen zeigt die Bedingung): z. B. Oktopus (4+4 Finger),
Zehn-Finger-Gott, Rhythmus-Meister, Fieberwahn, Tippmaschine (12 pro Sekunde), Eiserner Popo (10 Minuten),
Haarscharf (unter -9 und gerettet), Comeback-Kid, Geizhals, Dagobert, Sternenfänger, Nachteule, Popo-Veteran.
Dazu kommen Goodie-Jäger, Bescherung, Nasenklammer und Ausweichkünstler. Insgesamt 134 Awards. Liste in `Awards.java`.

**Bestenliste:** Top 10 Spiele nach Gesamt-Klatschern, mit Datum.

- `app/` – Android-Projekt (Java, eine eigene `View`, die alles auf ein Canvas zeichnet; keine Bibliotheken)
- `wackel-popo-debug.apk` – fertiges Debug-APK (minSdk 24 / Android 7, targetSdk 35, v2-signiert mit Debug-Schlüssel)
- `tools/` – Build ohne Android-SDK (siehe unten)

## Installieren
APK aufs Handy kopieren, öffnen und „Installation aus unbekannten Quellen“ erlauben,
oder per `adb install wackel-popo-debug.apk`.

## Updates direkt aus GitHub (ab Version 1.1)
Beim Start schaut die App in diesem Repo nach `version.txt` (`<versionCode> <versionName>`). Ist der
versionCode dort höher als der installierte, erscheint oben die Pille „⬇ Neue Version …: tippen zum
Installieren“. Ein Tipp lädt `wackel-popo-debug.apk` und übergibt es dem Android-Installer; beim ersten
Mal fragt Android, ob die App andere Apps installieren darf. Ist das Repo privat, fragt die App nach
einem GitHub-Token mit Lesezugriff. Neue Versionen müssen mit demselben Schlüssel signiert sein,
sonst lehnt Android das Update ab.

Neue Version veröffentlichen: versionCode in `version.txt` erhöhen, `tools/build-ohne-sdk.sh` bauen,
APK und `version.txt` nach `main` pushen.

## Selbst bauen
Normal mit Android Studio öffnen, oder `./gradlew assembleDebug` (braucht Android-SDK).

Das beiliegende APK wurde ohne SDK gebaut, weil die Build-Umgebung keinen Zugriff auf
Googles Server hatte: `tools/build-ohne-sdk.sh` (javac + dx + `tools/mkapk.py`, das
Manifest, Ressourcen und v2-Signatur selbst schreibt).
