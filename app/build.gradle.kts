plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.htopstore"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.htopstore"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "com.example.htopstore.HiltTestRunner"
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

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    // ==========================
    // AndroidX Core & UI
    // ==========================
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(project(":domain"))
    implementation(project(":data"))
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // ==========================
    // Lifecycle
    // ==========================
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.fragment:fragment-ktx:1.8.5")

    // ==========================
    // Testing
    // ==========================
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // ==========================
    // Hilt DI
    // ==========================
    implementation("com.google.dagger:hilt-android:2.51.1")
    kapt("com.google.dagger:hilt-compiler:2.51.1")
    implementation("androidx.hilt:hilt-navigation-fragment:1.2.0")
    kapt("androidx.hilt:hilt-compiler:1.2.0")

    // ==========================
    // Room
    // ==========================
    val roomVersion = "2.6.1"
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt("androidx.room:room-compiler:$roomVersion")
    testImplementation(libs.androidx.room.testing)
    implementation(libs.androidx.room.paging)

    // ==========================
    // Glide (Image loading)
    // ==========================
    implementation("com.github.bumptech.glide:glide:4.16.0")
    kapt("com.github.bumptech.glide:compiler:4.16.0")
    implementation("com.makeramen:roundedimageview:2.3.0")

    // ==========================
    // QR Code & ML Kit
    // ==========================
    implementation("com.google.zxing:core:3.5.3")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    val camerax_version = "1.3.4"
    implementation("androidx.camera:camera-core:$camerax_version")
    implementation("androidx.camera:camera-camera2:$camerax_version")
    implementation("androidx.camera:camera-lifecycle:$camerax_version")
    implementation("androidx.camera:camera-view:$camerax_version")
    implementation("androidx.camera:camera-extensions:$camerax_version")
    implementation("com.google.mlkit:barcode-scanning:17.2.0")
    implementation("com.google.android.gms:play-services-vision:20.1.3")

    // ==========================
    // Charts & Utilities
    // ==========================
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("com.google.guava:guava:31.1-android")

    // ==========================
    // Kotlin Coroutines
    // ==========================
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // ==========================
    // Supabase
    // ==========================
    implementation("io.github.jan-tennert.supabase:storage-kt:2.4.1")
    implementation("io.github.jan-tennert.supabase:gotrue-kt:2.4.1")
    implementation("io.github.jan-tennert.supabase:postgrest-kt:2.4.1")
    implementation("io.github.jan-tennert.supabase:realtime-kt:2.4.1")

    // ==========================
    // Ktor Client (for Supabase SDK)
    // ==========================
    implementation("io.ktor:ktor-client-android:2.3.5")

    //=============================
    //Google services (sign)
    //=============================
    implementation ("com.google.android.gms:play-services-auth:21.1.0")

    //=============================
    //splash api
    //=============================
    implementation ("androidx.core:core-splashscreen:1.0.1")


}
