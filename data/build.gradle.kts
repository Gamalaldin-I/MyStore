plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
}

android {
    namespace = "com.example.data"
    compileSdk = 35

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    implementation(project(":domain"))
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    //room
    val roomVersion = "2.6.1"
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt("androidx.room:room-compiler:$roomVersion")
    testImplementation(libs.androidx.room.testing)
    implementation (libs.androidx.room.paging)
}