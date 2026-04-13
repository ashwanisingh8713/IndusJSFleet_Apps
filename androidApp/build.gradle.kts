plugins {
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

apply(from = rootProject.file("gradle/fleet-android-conventions.gradle"))
// compileSdk, minSdk, targetSdk, compileOptions, jvmTarget set by fleet-android-conventions

android {
    namespace = "com.indusjs.fleet.androidApp"

    defaultConfig {
        applicationId = "com.indusjs.fleet.androidApp"
        versionCode = 1
        versionName = "1.0.0"
    }

    // Enable BuildConfig generation
    buildFeatures {
        buildConfig = true
    }


    buildTypes {
        debug {
            // Disable Crashlytics in debug builds for faster builds
            configure<com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension> {
                mappingFileUploadEnabled = false
            }
        }
        release {
            // Enable mapping file upload for release builds
            configure<com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension> {
                mappingFileUploadEnabled = true
                nativeSymbolUploadEnabled = true
            }
        }
    }
}


dependencies {
    implementation(project(":sharedUI"))
    implementation(project(":screen-payment"))
    implementation(project(":ijs-logger-lib"))
    implementation(libs.androidx.activityCompose)

    // Firebase BOM - manages all Firebase library versions
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)

    // Razorpay Android SDK — required for AppActivity to implement PaymentResultWithDataListener
    implementation("com.razorpay:checkout:1.6.40")
}
