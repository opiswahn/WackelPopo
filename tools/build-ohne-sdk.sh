#!/usr/bin/env bash
# Baut das Debug-APK ohne Android-Build-Tools: javac + dx + tools/mkapk.py.
# Die Version kommt aus version.txt ("<versionCode> <versionName>"). Für ein neues Update dort
# den versionCode erhöhen, bauen und das APK als wackel-popo-debug.apk mit version.txt pushen.
# Braucht: JDK, Python 3 mit "cryptography", ein android.jar (API 35) und dalvik-dx.jar.
#   ANDROID_JAR=/pfad/android.jar DX_JAR=/pfad/dalvik-dx-16.0.1.jar tools/build-ohne-sdk.sh
set -euo pipefail
cd "$(dirname "$0")/.."
out=build-ohne-sdk
rm -rf "$out" && mkdir -p "$out/classes"
javac -encoding UTF-8 -source 8 -target 8 -Xlint:-options -bootclasspath "$ANDROID_JAR" -d "$out/classes" $(find app/src/main/java -name '*.java')
java -cp "$DX_JAR" com.android.dx.command.Main --dex --min-sdk-version=24 --output="$out/classes.dex" "$out/classes"
read -r VERSION_CODE VERSION_NAME < version.txt
export VERSION_CODE VERSION_NAME
python3 tools/mkapk.py "$out/classes.dex" app/src/main/res/mipmap-xxxhdpi/ic_launcher.png "$out/wackel-popo-debug.apk" "$out/keys"
