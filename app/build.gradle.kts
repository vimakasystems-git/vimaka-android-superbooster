plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val repo = (findProperty("githubRepo") as String?) ?: "vimakasystems-git/vimaka-android-superbooster"
val ver = (findProperty("appVersion") as String?) ?: "1.1.0"
val code = ((findProperty("appCode") as String?) ?: "2").toInt()
val ks = System.getenv("KEYSTORE_FILE")

dependencies { implementation("androidx.core:core-ktx:1.13.1") }

android {
    namespace = "br.vimaka.superbooster"
    compileSdk = 35
    defaultConfig {
        applicationId = "br.vimaka.superbooster"
        minSdk = 26
        targetSdk = 35
        versionCode = code
        versionName = ver
        buildConfigField("String", "GITHUB_REPO", "\"$repo\"")
    }
    flavorDimensions += "distribution"
    productFlavors {
        create("play") {
            dimension = "distribution"
            buildConfigField("Boolean", "DEVELOPER_EDITION", "false")
        }
        create("developer") {
            dimension = "distribution"
            buildConfigField("Boolean", "DEVELOPER_EDITION", "true")
        }
    }
    buildFeatures { buildConfig = true }
    signingConfigs {
        create("release") {
            if (ks != null) {
                storeFile = file(ks)
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName(if (ks != null) "release" else "release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}
