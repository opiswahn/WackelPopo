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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
