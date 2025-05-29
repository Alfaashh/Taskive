plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose") // Ini cara standar untuk plugin Compose
    kotlin("kapt")
}

android {
    namespace = "com.taskive"
    compileSdk = 35 // Sesuai dengan yang Anda gunakan

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
            isMinifyEnabled = false // Anda set false, bisa diubah ke true dengan ProGuard
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        // Aktifkan Core Library Desugaring
        isCoreLibraryDesugaringEnabled = true // <-- DIPASTIKAN ADA
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14" // <-- SESUAIKAN DENGAN VERSI COMPOSE BOM ANDA
        // Jika menggunakan Compose BOM, ini seringkali tidak perlu didefinisikan eksplisit
        // Untuk Compose BOM 2024.05.00, versi compiler bisa 1.5.14 atau cek dokumentasi
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
    implementation(platform(libs.androidx.compose.bom)) // Menggunakan BOM sangat direkomendasikan
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    // implementation(libs.androidx.ui.tooling.preview) // Sebaiknya debugImplementation
    debugImplementation(libs.androidx.ui.tooling.preview) // <-- Diubah ke debugImplementation
    implementation(libs.androidx.material3) // Ini adalah dependensi Material 3 utama
    implementation(libs.androidx.navigation.compose)
    implementation(libs.material3) // Ini kemungkinan duplikat dari libs.androidx.material3, bisa dihapus jika sama
    implementation(libs.androidx.material.icons.core)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.play.services.tasks) // Ini yang memerlukan desugaring
    implementation(libs.android.gif.drawable) // Ini memungkinkan penggunaan gif
    implementation (libs.glide)
    implementation (libs.android.gif.drawable.v1227) // Untuk GifImageView
    kapt (libs.compiler)  // Jika kamu nanti pakai anotasi Glide
    implementation(libs.coil.compose)
    implementation(libs.coil.gif)    // Tambahan untuk dukungan GIF
    coreLibraryDesugaring(libs.desugar.jdk.libs) // <-- DIPASTIKAN ADA
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}