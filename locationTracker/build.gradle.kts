plugins {
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlinx.serialization)
}

apply(from = rootProject.file("gradle/fleet-android-conventions.gradle"))
// compileSdk, targetSdk, compileOptions, jvmTarget set by fleet-android-conventions

android {
    namespace = "com.indusjs.fleet.locationtracker"

    defaultConfig {
        minSdk = 24 // Override: locationTracker requires API 24+

        applicationId = "com.indusjs.fleet.locationtracker"
        versionCode = 1
        versionName = "1.0.0"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/INDEX.LIST"
            excludes += "/META-INF/io.netty.versions.properties"
        }
    }
}

dependencies {
    // Compose
    implementation(platform("androidx.compose:compose-bom:2024.02.02"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Core Android
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")

    // Location Services
    implementation("com.google.android.gms:play-services-location:21.2.0")

    // MQTT - HiveMQ MQTT Client (more modern and actively maintained)
    implementation("com.hivemq:hivemq-mqtt-client:1.3.3")

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // DataStore for preferences
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // WorkManager for background work
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // Kermit for logging
    implementation(libs.kermit)
}

