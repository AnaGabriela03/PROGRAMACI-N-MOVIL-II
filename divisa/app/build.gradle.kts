plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    kotlin("kapt") // Agregar el plugin kapt aquí
}

android {
    namespace = "com.example.divisa"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.divisa"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    // Dependencias de Compose
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation ("androidx.work:work-runtime-ktx:2.7.0")
    // Dependencias de Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

        // Coroutines para el trabajo en segundo plano
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.0") // Versión actual
        implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.4.0") // Para lifecycleScope
    

    // Dependencias de Room
    implementation(libs.room.runtime)
    kapt(libs.room.compiler) // Usamos kapt aquí

    // Dependencia de WorkManager para la sincronización cada hora
    implementation(libs.work.runtime)
    implementation("androidx.compose.ui:ui:1.4.0") // Dependencia de Jetpack Compose UI
    implementation("androidx.compose.material3:material3:1.0.0") // Dependencia de Material 3
    implementation("androidx.compose.runtime:runtime:1.4.0") // Dependencia para el manejo del estado
    // Dependencias para pruebas
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
