# Wackel-Popo

Ein Comic-Popo-Klatschspiel. Jedes Tippen klatscht den **Popo-Klatsch-Counter** hoch, aber der
Popo kühlt ab: Der Counter sinkt ständig, und zwar immer schneller (die Abkühlung wächst um 1,1 % pro Sekunde,
verdoppelt sich also etwa alle 63 Sekunden). Fällt der Counter auf **-10**, ist das Spiel vorbei. Jede Runde startet mit **25 Punkten** und **8 Sekunden Aufwärmen** ohne Abzug (ab 1.10).

**Klatschen im Takt (ab 1.8):** Um den Popo liegt ein gestrichelter Kreis, ein zweiter Ring schrumpft darauf
zu und trifft ihn genau auf dem Schlag. Tippen in dem Moment ist **PERFEKT** (x3), knapp daneben **Gut** (x1,5).
Jeder Treffer in Folge erhöht die Combo (+10 % pro Treffer, bis x3); ab Combo 16 startet der Fieber-Modus.
Tippen neben dem Takt zeigt **Zu früh** oder **Zu spät**: kostet keine Punkte, bringt aber nichts und die Combo ist weg (ab 1.14). Ein zweiter Tipp auf denselben Schlag wird ignoriert.
Einen Schlag ganz auslassen beendet die Combo ebenfalls. Pro Schlag zählt nur ein Treffer, also bringt wildes
Hämmern nichts. Der Takt startet gemütlich (0,9 s) und wird mit der Zeit schneller (bis 0,45 s).
Griffe mit mehreren Fingern zählen nur, wenn sie im Takt landen. Bosse verlieren nur durch Treffer im Takt
Lebenspunkte (15, jeder weitere +5). Goodies kommen alle 30 bis 50 Sekunden, der Bonus-Stern alle 60 bis 90.
Die Versionsnummer steht unten links.

**Shop** (Einkaufswagen oben links, nur im Spiel): Counter-Punkte gegen Upgrades tauschen. Solange der Shop (oder die Pokale) offen ist, pausiert das Spiel (ab 1.13). Die
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
Echo), komplett im Code erzeugt (`MetalLoop.java`), keine Audiodatei. Seit 1.4 gibt es drei Songs: ruhige
Gitarre im Menü, den Haupt-Song im Spiel und einen schnelleren Song einen Ton höher im Fieber-Modus und beim Boss. Der Knopf 🎸/🔇 unter dem Pokal
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

**39 Spezial-Awards** (lila Sterne im Award-Raster, antippen zeigt die Bedingung): z. B. Oktopus (4+4 Finger),
Zehn-Finger-Gott, Rhythmus-Meister, Fieberwahn, Tippmaschine (12 pro Sekunde), Eiserner Popo (10 Minuten),
Haarscharf (unter -9 und gerettet), Comeback-Kid, Geizhals, Dagobert, Sternenfänger, Nachteule, Popo-Veteran.
Dazu kommen Goodie-Jäger, Bescherung, Nasenklammer und Ausweichkünstler. Dazu Boss-Bezwinger, Boss-Jäger, Goldener Hintern, Vulkan-Po und Mega-Bezwinger. Insgesamt 139 Awards. Liste in `Awards.java`.

**Bestenliste:** Top 10 Spiele nach Gesamt-Klatschern, mit Datum.

**Boss-Popos (ab 1.4):** Nach gut einer Minute und dann alle 100 bis 140 Sekunden wird der Popo zum Boss
(lila, größer, Wikingerhelm, Sonnenbrille, Lebensbalken oben). Jeder Finger auf dem Popo zieht einen Punkt ab;
der Boss hat 60 Lebenspunkte, jeder weitere im selben Spiel 25 mehr, und man hat 20 Sekunden. Sieg: dicker
Punkte-Bonus, Popo-Taler, Krone und mit 35 % Chance ein Garderoben-Teil als Beute. Während des Bosses läuft
der schnelle Fieber-Song in voller Lautstärke.

**Popo-Evolution (ab 1.6):** Bei bestimmten Klatschern in einem Spiel verwandelt sich der Popo bis zum
Spielende: Bronze (1.000), Silber (5.000), Gold (15.000, ruft sofort einen Mega-Boss), Regenbogen (40.000,
Farbe läuft durch alle Töne, Sternchen-Spur beim Tippen), Galaxie (100.000, dunkelviolett mit funkelnden
Sternen, wieder ein Mega-Boss) und Lava (250.000, glühend pulsierend). Jede Verwandlung: Drehung, Blitz,
Partikel-Explosion, Farbe blendet über, dazu eine pulsierende Aura. Mega-Bosse haben doppelte Lebenspunkte
und geben doppelte Beute.

**Feuerroter Popo (ab 1.7):** Jeder Klatscher heizt die Backen auf, ohne Tippen kühlen sie langsam ab. Bei
schnellem Dauerklatschen werden sie immer röter bis knallrot, die Bäckchen wachsen, und ganz heiß steigt
Dampf auf („Aua, heiß!“). Das funktioniert auf jeder Evolutionsstufe.

**Garderobe (ab 1.4):** Der Knopf 👕 oben links (außerhalb des Spiels) öffnet die Garderobe. Mit Popo-Talern
kauft man dauerhaft Hüte (Kappe, Partyhut, Cowboyhut, Zylinder, Wikingerhelm), Brillen (Sonnen-, Nerd-,
Herzbrille) und feste Hosenfarben. Taler gibt es am Spielende (1 pro 500 Klatscher), für Bosse (10 + 5 pro
Boss im Spiel) und für die Tages-Challenge.

**Tages-Challenge (ab 1.4):** Jeden Tag eine andere Aufgabe aus 10 (z. B. 4 Goodies fangen, 4 Minuten
überleben, einen Boss besiegen, 2 Minuten ohne Shop). Steht auf dem Startbildschirm. Geschafft gibt es
40 Taler plus 10 pro Tag in Folge (bis +100).

**Der Kanadier (ab 1.9):** Wer wild aufs Display hämmert (4 Fehlschläge in 2 Sekunden) oder die
Stinkbombe fängt, bekommt Besuch: Unten rechts fährt ein Kopf im South-Park-Kanadier-Stil herein, klappt
beim Reden die obere Hälfte auf, hebt die Hand und ruft „Einfach mal NEIN sagen!“. Höchstens alle
12 Sekunden. Die Kopfhälften liegen in `app/src/main/assets/`, gebaut mit `tools/kanadier.py`.

**Rekord teilen (ab 1.4):** Auf der Game-Over-Karte macht „📤 Rekord teilen“ ein Bild mit Popo samt Outfit,
Punktzahl und Spruch und öffnet das Android-Teilen-Menü (WhatsApp usw.).

- `app/` – Android-Projekt (Java, eine eigene `View`, die alles auf ein Canvas zeichnet; keine Bibliotheken)
- `wackel-popo-debug.apk` – fertiges Debug-APK (minSdk 24 / Android 7, targetSdk 35, v2-signiert mit Debug-Schlüssel)
- `tools/` – Build ohne Android-SDK (siehe unten)

## Installieren
APK aufs Handy kopieren, öffnen und „Installation aus unbekannten Quellen“ erlauben,
oder per `adb install wackel-popo-debug.apk`.

## Updates direkt aus GitHub
Jeder Push auf `main` startet den GitHub-Build (`.github/workflows/release.yml`): Er baut das APK mit
`tools/build-ohne-sdk.sh`, signiert es mit dem Release-Schlüssel und veröffentlicht es als Release
`build-<versionCode>` mit der Datei `wackel-popo.apk` (versionCode = 100 + Build-Nummer).

Der Release-Schlüssel wird mit `tools/release-key.py` bei jedem Build aus dem Repo-Secret
`SIGNING_PASSPHRASE` abgeleitet; gleiches Passwort ergibt immer denselben Schlüssel. Einmalig einrichten:
GitHub → Repo → Settings → Secrets and variables → Actions → „New repository secret“, Name
`SIGNING_PASSPHRASE`, Wert ein eigenes Passwort (mindestens 8 Zeichen, gut aufheben, nie ändern).
Das Release-APK hat einen anderen Schlüssel als das Debug-APK: Beim Wechsel einmal deinstallieren.

Beim Start fragt die App das neueste Release ab. Ist es neuer als die installierte Version, erscheint auf dem Startbildschirm unten
„⬇ Neue Version …: tippen zum Installieren“. Ein Tipp lädt das APK und übergibt es dem Android-Installer;
beim ersten Mal fragt Android, ob die App andere Apps installieren darf. Gibt es noch kein Release, nimmt die
App `version.txt` und `wackel-popo-debug.apk` aus dem Repo. Neue Versionen müssen mit demselben Schlüssel
signiert sein, sonst lehnt Android das Update ab.

## Selbst bauen
Normal mit Android Studio öffnen, oder `./gradlew assembleDebug` (braucht Android-SDK).

Das beiliegende APK wurde ohne SDK gebaut, weil die Build-Umgebung keinen Zugriff auf
Googles Server hatte: `tools/build-ohne-sdk.sh` (javac + dx + `tools/mkapk.py`, das
Manifest, Ressourcen und v2-Signatur selbst schreibt).
