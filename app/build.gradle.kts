plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose") // Ini cara standar untuk plugin Compose
    kotlin("kapt") // Biarkan jika masih dipakai Glide
    id("com.google.devtools.ksp") // <-- HAPUS 'version "1.9.23-1.0.19"' DARI SINI
}

android {
    namespace = "com.taskive"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.taskive"
        minSdk = 27
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14" // Sesuaikan
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.compose.navigation)
    // implementation(libs.androidx.compose.navigation) // Ini sepertinya duplikat dengan libs.androidx.navigation.compose
    debugImplementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose) // Ini yang benar untuk navigation compose
    // implementation(libs.material3) // Kemungkinan duplikat, periksa libs.versions.toml Anda. Jika sama dengan androidx.material3, hapus.
    implementation(libs.androidx.material.icons.core)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.play.services.tasks)
    implementation(libs.android.gif.drawable)
    implementation(libs.glide)
    // implementation(libs.android.gif.drawable.v1227) // Cek apakah ini benar-benar perlu atau versi utama sudah cukup
    kapt(libs.compiler)
    implementation(libs.coil.compose)
    implementation(libs.coil.gif)

    // Pastikan libs.desugar.jdk.libs terdefinisi di libs.versions.toml
    // Jika tidak, gunakan string langsung: "com.android.tools:desugar_jdk_libs:2.0.4" (cek versi terbaru)
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // --- ROOM PERSISTENCE LIBRARY (Contoh jika belum ada di libs.versions.toml) ---
    val room_version = "2.6.1"
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler) // Menggunakan KSP untuk Room compiler
    implementation(libs.androidx.room.ktx)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}