# Wackel-Popo

Ein Comic-Rhythmusspiel für Android von **Jürgen Hölker**: Klatsch den Popo im Takt,
halte den Zähler über Wasser und sammle Awards, Talente, Outfits und Verwandlungen.

Die App ist in Java geschrieben, zeichnet mit Android Canvas und erzeugt Musik sowie
Soundeffekte selbst. Es gibt keine zusätzlichen App-Bibliotheken und keine Game-Engine.
Das Icon wird mit `tools/icon.py` gezeichnet. Die beiden Kanadier-PNGs entstehen mit
`tools/kanadier.py` aus einem eigenen, für die MIT-Freigabe autorisierten Porträtfoto
des Autors. Schriften und Emoji-Darstellung kommen vom Android-System.

- **Version:** `1.24` (lokaler `versionCode` 25; Quelle: [`version.txt`](version.txt)).
- **Geräte:** Android 7.0 / API 24 oder neuer, Hochformat, Touchscreen.
- **Sprachen:** Deutsch und Englisch, umschaltbar über DE/EN.
- **Lizenz:** [MIT](LICENSE), Copyright (c) 2026 Jürgen Hölker.
- **Fremdkomponenten:** Umfang, Original-Lizenztexte und Verträglichkeitsprüfung in
  [THIRD-PARTY-LICENSES](THIRD-PARTY-LICENSES). Fremdcode behält seine eigene Lizenz.

## Herunterladen und installieren

Im [neuesten GitHub-Release](https://github.com/opiswahn/WackelPopo/releases/latest)
die Datei `wackel-popo.apk` herunterladen und auf dem Handy öffnen. Falls nötig, die
Installation aus dieser Quelle in Android erlauben. Dies ist der direkte APK-Vertrieb,
kein Google-Play-Release.

Eine lokal gebaute Debug-APK hat einen anderen Signaturschlüssel als die Release-APK.
Sie kann deshalb nicht als Update darüber installiert werden. Eine Deinstallation kann
lokale Spielstände löschen; für Updates muss die Signatur gleich bleiben.

## So wird gespielt

Beim ersten Spiel erklärt ein Tutorial den Takt und die Bedienelemente. Der ❓-Knopf
auf dem Startbildschirm startet die Einführung erneut.

- **Zähler:** Start bei 25 Punkten, zusätzliche Startpunkte durch Karriere und Talente.
  Nach 8 Sekunden Aufwärmen sinkt er zunehmend schneller. Bei −10 endet das Spiel,
  sofern das Talent „Zweite Chance“ dich nicht einmalig rettet.
- **Takt treffen:** Der schrumpfende Ring zeigt den nächsten Schlag. PERFEKT gibt den
  Faktor ×3, Gut ×1,5. Große Ringe auf der Eins haben großzügigere Trefferfenster als
  kleine Zwischenbeats. Pro Schlag zählt ein Treffer; zusätzliche Finger können einen
  gemeinsamen Mehrfinger-Griff bilden.
- **Fehler:** Zu früh, zu spät oder verpasst beendet die Combo und kostet nach der
  Aufwärmphase Punkte. Wiederholte Fehlschläge werden teurer. Ein Tipp innerhalb von
  0,3 Sekunden nach einem Fehlschlag kann nicht treffen. Drei Fehlschläge binnen zwei
  Sekunden lösen die Spam-Strafe aus: 6 Sekunden lang dreifacher Zählerabfluss.
- **Combo und Fieber:** Treffer in Folge erhöhen den Combo-Faktor bis ×3. Ohne Talente
  beginnt Fieber ab Combo 16, gibt ×1,25 und einen schnelleren Song. Ein Fehler beendet es.
- **Mehrfinger-Griffe:** Fast gleichzeitig landende Finger zählen als ein Griff, nicht
  als unabhängige Klatscher. Fingerzahl und Links-/Rechts-Verteilung bestimmen Namen
  und Bonus; die Regeln stehen in `Chords.java`.
- **Shop:** Während einer Runde Punkte gegen Doppelhand, Auto-Klatscher, Popo-Creme,
  kritische Treffer, Combo-Turbo und Goldenen Popo tauschen. Die Upgrades gelten für
  diese Runde. Offene Shop- und Pokalansichten pausieren das Spiel.

Die sieben Rhythmusmuster werden mit der Überlebenszeit freigeschaltet:

| Muster | Schläge im 4/4-Takt | Frei ab |
|---|---|---|
| Im Takt | Eins, Drei | Start |
| Ganz langsam | Eins | Start |
| Doppeltempo | Eins, Zwei, Drei, Vier | 25 s |
| Nachschlag | Eins, Drei, Vier | 40 s |
| Synkope | Eins, zwischen Zwei und Drei, Vier | 60 s |
| Galopp | Eins, Zwei, direkt danach, Vier | 90 s |
| Doppelklatsch | je zwei schnelle Schläge auf Eins und Drei | 120 s |

## Ereignisse und langfristiger Fortschritt

- **Goodies:** Pfirsich, Frostpause, Schoko-Power, Glücksklee, Geschenk, Klatsch-Roboter
  und Sternen-Magnet bringen Boni. Die Stinkbombe kostet 15 % vom Zähler: fallen lassen!
  Goodies erscheinen am Fallschirm; zusätzlich gibt es Bonus-Sterne.
- **Bosse und Evolution:** Bosse haben ein Zeitlimit von 20 Sekunden und nehmen durch
  Treffer im Takt Schaden. Punkte im laufenden Spiel führen zu Bronze-, Silber-, Gold-,
  Regenbogen-, Galaxie- und Lava-Verwandlungen. Gold und Galaxie können Mega-Bosse rufen.
- **Der Kanadier:** Ein aus einem eigenen Foto erzeugter Comic-Kopf erscheint bei Spam oder einer
  gefangenen Stinkbombe und mahnt zum Nein-Sagen. Die Einblendung hat 12 Sekunden Abklingzeit.
- **Karriere:** Bis Level 50 mit Erfahrung durch Treffer, Bosse und Aufträge. Drei
  Aufträge sind parallel aktiv; erledigte werden beim nächsten Spiel ersetzt.
- **Awards:** 100 reguläre Awards für die Klatscher aller abgeschlossenen Spiele plus
  39 Spezial-Awards, insgesamt 139. Definitionen: `Awards.java`.
- **Talente:** Permanente Verbesserungen für Startpunkte, Zählerabfluss, Trefferfenster,
  Fieber, Goodie-Häufigkeit und eine zweite Chance.
- **Bühnen:** Hinterhof, Strand, Disco, Weltall und Vulkan mit eigenen Hintergründen,
  Tempo-/Tonartänderungen und steigender Schwierigkeit. Das Bühnenziel ruft einen
  Endboss; dessen Sieg schaltet die nächste Bühne frei.
- **Popo-Taler und Garderobe:** Taler durch Spielrunden, Bosse, Karriere und Tagesaufgaben
  für Hüte, Brillen, Hosenfarben und Talente ausgeben; keine Echtgeldkäufe implementiert.
- **Tages-Challenge und Bestenliste:** Zehn mögliche Tagesaufgaben, Serienbonus,
  lokale Top 10 und ein teilbares Rekordbild.

Die drei prozeduralen Retro-Metal-Songs sind in `MetalLoop.java` definiert: Menü,
Spiel und Fieber/Boss. Musik lässt sich abschalten; der Rhythmus bleibt spielbar.
Spielstände liegen lokal in Android `SharedPreferences`. Ein Konto ist nicht erforderlich.

## Updates und Berechtigungen

Beim App-Start fragt `Updater.java` das neueste Release über die GitHub-API ab. Das Spiel
selbst funktioniert offline; die Update-Abfrage benötigt Internet und übermittelt dabei
übliche Verbindungsdaten an GitHub. Eine neuere Version wird auf dem Startbildschirm
angeboten, aber erst nach Antippen heruntergeladen. Android verlangt die Zustimmung zur
Installation; bei Problemen gibt es einen Browser-Fallback.

Das Manifest deklariert:

- `VIBRATE`: haptische Effekte.
- `INTERNET`: GitHub-Updateprüfung und APK-Download.
- `REQUEST_INSTALL_PACKAGES`: den Android-Installationsdialog für APK-Updates öffnen.

`android:allowBackup="true"` erlaubt Android-Systembackups. Der private `ShareProvider`
teilt Rekordbilder über zeitweise URI-Berechtigungen.

## Selbst bauen: Gradle / Android Studio

Voraussetzungen:

- **JDK 21**; `JAVA_HOME` und `PATH` müssen auf das gewünschte JDK zeigen.
- Android-SDK mit **Platform 35**, Platform-Tools und den von AGP benötigten Build-Tools
  (bei AGP 8.7 standardmäßig 34.0.0); SDK-Lizenzen akzeptiert. Gradle kann fehlende
  SDK-Pakete nachladen, wenn die Umgebung und Lizenzen dies erlauben.
- Der mitgelieferte Wrapper lädt **Gradle 8.14.3**; verwendet wird **AGP 8.7.3**.

SDK-Pfad über `ANDROID_HOME` oder eine nicht versionierte `local.properties` setzen:

```properties
sdk.dir=/absoluter/pfad/zum/Android/Sdk
```

```sh
./gradlew assembleDebug lintDebug
# Optional: Release-Bundle bauen; produktive Signierung ist nicht konfiguriert.
./gradlew bundleRelease
```

Unter Windows `gradlew.bat` verwenden. Das Projekt kann auch in einer mit der Toolchain
kompatiblen Android-Studio-Version geöffnet werden.

- Debug-APK: `app/build/outputs/apk/debug/app-debug.apk`
- Lint-Bericht: `app/build/reports/lint-results-debug.html`
- Release-Bundle: `app/build/outputs/bundle/release/app-release.aab`

Der Gradle-Build setzt Java-Quell- und Zielkompatibilität auf **21**. Das erhöht nicht
`minSdk`: API 24 bleibt die Geräte-Untergrenze. Verfügbarkeit neuer Sprachfeatures und
Java-APIs hängt dennoch von D8/AGP, Desugaring und Android ab; nicht jede JDK-21-API ist
auf Android verfügbar. Core-Library-Desugaring ist derzeit nicht konfiguriert.
`compileSdk` und `targetSdk` stehen auf **35**, nicht 36. Ein erzeugtes Bundle allein
bedeutet daher noch keine Freigabe oder Erfüllung aller aktuellen Play-Store-Vorgaben.

Es sind aktuell keine eigenen Unit- oder Instrumentation-Tests eingecheckt. Build und
Lint ersetzen keinen Spieltest auf einem Gerät bzw. Emulator.

## Alternativer APK-Build ohne installiertes Android-SDK

Der aktuelle GitHub-Release-Workflow benutzt **nicht Gradle**, sondern
`tools/build-ohne-sdk.sh`: JDK 21 führt `javac` mit **Java-8-Quell-/Bytecode-Ziel** aus,
`dalvik-dx` erzeugt DEX, `mkapk.py` baut und signiert die APK. Dieser separate Weg
unterstützt keine Java-21-Sprachfeatures; Änderungen am Quellcode müssen beide Wege
berücksichtigen. Auch dieser Build setzt minSdk 24 und targetSdk 35.

Benötigt werden Bash, JDK 21, Python 3 mit `cryptography`, ein API-35-`android.jar`
und `com.jakewharton.android.repackaged:dalvik-dx:16.0.1`:

```sh
ANDROID_JAR=/pfad/android.jar \
DX_JAR=/pfad/dalvik-dx-16.0.1.jar \
tools/build-ohne-sdk.sh
```

Die APK landet in `build-ohne-sdk/wackel-popo.apk`. Ohne `KEYS_DIR` entstehen lokale
Signaturschlüssel in `build-ohne-sdk/keys`; dieser Ausgabeordner wird beim nächsten
Build gelöscht. Für eine stabile lokale Signatur einen externen `KEYS_DIR` verwenden.
`VERSION_CODE` und `VERSION_NAME` überschreiben bei Bedarf `version.txt`.

Die optionalen Asset-Generatoren `tools/icon.py` und `tools/kanadier.py` benötigen
zusätzlich Pillow. Die erzeugten PNGs sind bereits eingecheckt.

### Automatische GitHub-Releases

Ein Push auf `main` oder ein manueller Start von `.github/workflows/release.yml` baut
und veröffentlicht bei erfolgreichem Lauf `wackel-popo.apk` als `build-<versionCode>`.
Der Workflow nutzt Temurin 21, Python 3.12, `cryptography` sowie heruntergeladene
API-35-Stubs und dx. `versionCode` ist `100 + GITHUB_RUN_NUMBER`, der Versionsname
kommt aus `version.txt`. Lint und eigene Tests sind keine Schritte dieses Workflows.

Das Repo-Secret `SIGNING_PASSPHRASE` wird von `tools/release-key.py` deterministisch in
den Release-Schlüssel umgewandelt. Es muss stark, geheim, gesichert und unverändert
bleiben, damit Updates dieselbe Signatur behalten. Schlüssel und Passphrase gehören
niemals ins Git-Repository. Dieses individuelle Signierungsverfahren ist kein Ersatz
für ein separat geprüftes Release-/Play-App-Signing-Konzept.

## Projektstruktur

| Pfad | Inhalt |
|---|---|
| `app/src/main/java/de/kgoh/wackelpopo/` | Spiel, Fortschritt, Audio, Updates und Teilen |
| `WackelView.java` im obigen Verzeichnis | Canvas-Darstellung, Eingaben und Spielablauf |
| `app/src/main/assets/` | beide Hälften des Kanadier-Kopfs |
| `app/src/main/res/` | Icon und App-Name |
| `tools/build-ohne-sdk.sh` | alternativer javac/dx-Build |
| `tools/mkapk.py` | binäres Manifest, Ressourcen und APK-v2-Signatur |
| `tools/release-key.py` | deterministische Release-Schlüsselableitung |
| `tools/icon.py`, `tools/kanadier.py` | PNG-Generatoren mit Pillow |
| `version.txt` | lokale Versionsnummer und Versionsname |
| `LICENSE` | MIT-Lizenz für die eigenen Projektbestandteile |
| `THIRD-PARTY-LICENSES` | Fremdkomponenten, Lizenztexte und Verträglichkeit |
