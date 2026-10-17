val version = rootProject.file("version.txt").readText().trim().split(Regex("\\s+"))

plugins {
    id("com.android.application")
}

android {
    namespace = "de.kgoh.wackelpopo"
    compileSdk = 35

    defaultConfig {
        applicationId = "de.kgoh.wackelpopo"
        minSdk = 24
        targetSdk = 35
        versionCode = version[0].toInt()
        versionName = version[1]
    }

    compileOptions {
        // Sprachlevel 21 (LTS): D8 desugart die Features runter, minSdk 24 bleibt die Geräte-Grenze.
        // Java 25 ginge erst mit Gradle 9.1+/AGP-Upgrade — lohnt für dieses Projekt nicht.
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}
