plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    kotlin("kapt") // Untuk Glide
    id("com.google.devtools.ksp") // Versi sudah di level proyek
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
    // composeOptions {
    //     kotlinCompilerExtensionVersion = "..." // Coba hapus/komentari ini dulu jika pakai Kotlin 2.0 dan BOM terbaru
    // }
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
    debugImplementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3) // Gunakan yang ini (dari BOM)
    // implementation(libs.material3) // HAPUS INI JIKA DUPLIKAT
    implementation(libs.androidx.navigation.compose) // Navigasi untuk HP/Tablet
    // implementation(libs.androidx.compose.navigation) // HAPUS INI JIKA BUKAN WEAR OS

    implementation(libs.androidx.material.icons.core)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.play.services.tasks)
    implementation(libs.android.gif.drawable)
    implementation(libs.glide)
    // implementation(libs.android.gif.drawable.v1227) // Pertimbangkan untuk menghapus jika tidak spesifik dibutuhkan
    kapt(libs.compiler)
    implementation(libs.coil.compose)
    implementation(libs.coil.gif)
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    val room_version = "2.6.1"
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}