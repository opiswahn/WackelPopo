# Wackel-Popo

Ein Comic-Klatschspiel für Android: Klatsch den Popo im Takt der Musik, halt den Zähler über Wasser und
sammle Awards, Outfits und Verwandlungen. Komplett in Java ohne Bibliotheken; Grafik, Musik und Effekte
entstehen im Code.

**Herunterladen:** [neuestes Release](https://github.com/opiswahn/WackelPopo/releases/latest) →
`wackel-popo.apk` aufs Handy laden, öffnen, „Installation aus unbekannten Quellen“ erlauben. Danach
aktualisiert sich die App selbst (siehe [Updates](#updates)).

## So wird gespielt

Beim allerersten Spiel führt eine kurze Einführung durch alles: drei Treffer im Takt üben, dann vier Karten
zum Durchtippen. Der ❓-Knopf rechts oben (unter DE/EN) auf dem Startbildschirm startet sie jederzeit wieder.

- **Der Zähler** startet bei 25 und sinkt ständig, und zwar immer schneller (+1,1 % pro Sekunde, verdoppelt
  sich etwa jede Minute). Fällt er auf **−10**, ist das Spiel vorbei. Die ersten 8 Sekunden sind Aufwärmen
  ohne Abzug.
- **Klatschen im Takt:** Ein Ring schrumpft auf den Kreis um den Popo zu und trifft ihn genau auf dem Schlag.
  Die Schläge liegen immer auf der Musik (Achtelnoten des Songs, abgeglichen mit dem, was gerade aus dem
  Lautsprecher kommt), aber der Rhythmus wechselt alle zwei Takte. Ein neuer Rhythmus wird kurz angesagt:

  | Rhythmus | Schläge pro Takt | frei ab |
  |---|---|---|
  | Im Takt | auf Eins und Drei | Start |
  | Ganz langsam | nur auf der Eins | Start |
  | Doppeltempo! | alle vier Viertel | 25 s |
  | Nachschlag! | Eins, Drei, Vier | 40 s |
  | Synkope! | Eins, zwischen Zwei und Drei, Vier | 60 s |
  | Galopp! | Eins, Zwei, gleich danach, Vier | 90 s |
  | Doppelklatsch! | je zwei schnelle Schläge auf Eins und Drei | 120 s |

  Der Ring schließt sich genau in der Zeit seit dem letzten Schlag: schneller Ring, schneller Schlag. Auf der
  Eins ist er groß und verzeiht mehr, zwischen den Vierteln ist er klein und knapp. Im Fieber- und Boss-Song
  (176 statt 152 BPM) wird alles schneller; ohne Musik läuft der Takt im selben Tempo weiter.
  - Genau getroffen ist **PERFEKT** (x3), knapp daneben **Gut** (x1,5).
  - Neben dem Takt zeigt **Zu früh** oder **Zu spät** und beendet die Combo. Ein einzelner Ausrutscher ist
    gratis, jeder weitere Fehlschlag in Folge kostet mehr (ein halber, ein ganzer, anderthalb, bis zwei Klatscher).
  - **Spam-Strafe:** 4 Fehlschläge in 2 Sekunden nerven den Popo: 5 Sekunden lang sinkt der Zähler doppelt so
    schnell (😤 unter dem Shop-Knopf), und der Kanadier schaut vorbei.
  - Pro Schlag zählt ein Treffer; ein zweiter Tipp auf denselben Schlag wird ignoriert. Wildes Hämmern lohnt
    sich also nicht.
- **Combo:** Jeder Treffer in Folge gibt +10 % (bis x3). Ein ausgelassener Schlag oder ein Tipp neben dem
  Takt beendet sie. Ab Combo 16 startet der **Fieber-Modus** (alles x1,25, schnellerer Song) bis zum nächsten Fehler.
- **Mehrfinger-Griffe:** Finger, die fast gleichzeitig landen, bilden einen Griff. Er zählt wie **ein**
  Klatscher mal einem kleinen Faktor (x1,05 bei 2 Fingern bis x1,8 beim „Zehn-Finger-Gewitter“), und nur im
  Takt. Links/rechts der Popo-Mitte bestimmt den Namen (`Chords.java`).
- **Shop** (🛒 links oben, nur im Spiel): Punkte gegen Upgrades tauschen. Solange Shop oder Pokale offen
  sind, steht das Spiel still.

| Upgrade | Wirkung | Startpreis | Preisfaktor |
|---|---|---|---|
| 👏 Doppelhand | +1 pro Klatsch | 25 | x2 |
| 🤖 Auto-Klatscher | +0,5 pro Sekunde von allein | 40 | x2 |
| 🧴 Popo-Creme | Zähler sinkt 12 % langsamer (max. 10) | 60 | x2,2 |
| ⚡ Kritischer Klatscher | +4 % Chance auf x3 (max. 10) | 100 | x2,2 |
| 🔥 Combo-Turbo | +10 % ab Combo 5 (max. 10) | 150 | x2,2 |
| 👑 Goldener Popo | alles +15 % (max. 5) | 400 | x2,5 |

## Was zwischendurch passiert

- **Goodies** schweben alle 30 bis 50 Sekunden am Fallschirm herunter; antippen, bevor sie landen. Laufende
  Goodies stehen mit Restzeit unter dem Shop-Knopf.

| Goodie | Wirkung |
|---|---|
| 🍑 Pfirsich | Bonus-Punkte |
| 🧊 Frostpause | Zähler sinkt 8 Sekunden lang nicht |
| 🍫 Schoko-Power | Klatscher x2 für 10 Sekunden |
| 🍀 Glücksklee | +25 % Krit-Chance für 10 Sekunden |
| 🎁 Geschenk | Gratis-Stufe für das günstigste Upgrade |
| 🤖 Klatsch-Roboter | +3 Auto-Klatscher pro Sekunde für 10 Sekunden |
| 🧲 Sternen-Magnet | Der nächste Bonus-Stern kommt sofort |
| 💨 Stinkbombe | Falle: kostet 15 % vom Zähler, also fallen lassen |

- **Bonus-Stern:** alle 60 bis 90 Sekunden; antippen gibt einen dicken Bonus.
- **Boss-Popo:** nach gut einer Minute, dann alle 100 bis 140 Sekunden (lila, Wikingerhelm, Lebensbalken).
  Nur Treffer im Takt ziehen Lebenspunkte ab: 15, jeder weitere Boss im Spiel +5, und man hat 20 Sekunden.
  Sieg: Bonus, Popo-Taler, Krone und mit 35 % Chance ein Garderoben-Teil.
- **Popo-Evolution** nach Klatschern im Spiel: Bronze (1.000), Silber (5.000), Gold (15.000, Mega-Boss),
  Regenbogen (40.000), Galaxie (100.000, Mega-Boss), Lava (250.000). Mega-Bosse haben doppelte Lebenspunkte
  und doppelte Beute.
- **Feuerroter Popo:** Jeder Treffer heizt die Backen auf, ohne Tippen kühlen sie ab. Bei Dauerfeuer werden
  sie knallrot und dampfen.
- **Der Kanadier:** Wer wild hämmert (4 Fehlschläge in 2 Sekunden) oder die Stinkbombe fängt, bekommt Besuch:
  Unten rechts fährt ein Kopf im South-Park-Stil herein, klappt beim Reden auf und ruft „Einfach mal NEIN
  sagen!“. Höchstens alle 12 Sekunden.

## Fortschritt über viele Spiele

- **139 Awards** (Pokal rechts oben): 100 normale Awards nach den Klatschern **aller Spiele zusammen**
  (Schwelle 100 × Nummer^2,6, also Award 10 bei ~40.000 und Award 100 bei ~16 Mio.), jeder mit eigenem
  Namen und Effekt, dazu 39 Spezial-Awards für besondere Leistungen. Liste in `Awards.java`.
- **Popo-Taler** gibt es am Spielende (1 pro 500 Klatscher), für Bosse und für die Tages-Challenge.
- **Garderobe** (👕 links oben auf dem Startbildschirm): Hüte, Brillen und Hosenfarben für Taler.
- **Tages-Challenge:** jeden Tag eine von 10 Aufgaben, steht auf dem Startbildschirm. Geschafft gibt es
  40 Taler plus 10 pro Tag in Folge (bis +100).
- **Bestenliste:** Top 10 Spiele mit Datum. Auf der Game-Over-Karte macht „📤 Rekord teilen“ ein Bild
  zum Verschicken.

**Musik:** drei 80er-Retro-Metal-Songs, komplett im Code erzeugt (`MetalLoop.java`): ruhige Gitarre im Menü
(104 BPM), der Spiel-Song (152 BPM) und ein schnellerer Song einen Ton höher für Fieber und Boss (176 BPM).
Die Lautstärke folgt dem Geschehen; 🎸/🔇 unter dem Pokal schaltet sie ab.

**Sprache:** Der Knopf DE/EN rechts oben schaltet zwischen Deutsch und Englisch um, auch mitten im Spiel.

Die Versionsnummer steht klein unten links.

## Updates

Jeder Push auf `main` startet `.github/workflows/release.yml`: Es baut das APK, signiert es und
veröffentlicht es als Release `build-<versionCode>` mit der Datei `wackel-popo.apk`
(versionCode = 100 + Build-Nummer, Versionsname aus `version.txt`, die Build-Nummer steht im Release-Titel).

Die App fragt beim Start das neueste Release ab. Ist es neuer, erscheint unten auf dem Startbildschirm
„⬇ Neue Version …“; ein Tipp lädt und installiert es. Beim ersten Mal fragt Android, ob die App andere Apps
installieren darf. Meldet Android nichts zurück oder passt die Signatur nicht, bietet der Knopf den Download
im Browser an.

**Signatur:** Der Schlüssel wird bei jedem Build mit `tools/release-key.py` aus dem Repo-Secret
`SIGNING_PASSPHRASE` abgeleitet; gleiches Passwort, gleicher Schlüssel. Das Secret darf sich nie ändern,
sonst lassen sich Updates nicht mehr über die alte Version installieren.

## Projekt

| Pfad | Inhalt |
|---|---|
| `app/src/main/java/de/kgoh/wackelpopo/` | der ganze Code; `WackelView` zeichnet und steuert das Spiel |
| `app/src/main/assets/` | die zwei Hälften des Kanadier-Kopfs |
| `tools/build-ohne-sdk.sh` | baut das APK ohne Android-SDK (javac + dx + `mkapk.py`) |
| `tools/mkapk.py` | schreibt Manifest, Ressourcen und v2-Signatur selbst |
| `tools/release-key.py` | leitet den Signaturschlüssel aus dem Passwort ab |
| `tools/icon.py`, `tools/kanadier.py` | erzeugen App-Icon und Kanadier-Kopf |
| `version.txt` | `<versionCode> <versionName>` für lokale Builds |

**Selbst bauen:** Mit Android Studio öffnen oder `./gradlew assembleDebug` (braucht das Android-SDK).
Ohne SDK:

```
ANDROID_JAR=/pfad/android.jar DX_JAR=/pfad/dalvik-dx-16.0.1.jar tools/build-ohne-sdk.sh
```

Das ergibt `build-ohne-sdk/wackel-popo.apk`, signiert mit einem lokalen Schlüssel in `build-ohne-sdk/keys`
(oder `KEYS_DIR`). Ein so gebautes APK lässt sich nicht über die Release-Version installieren.
