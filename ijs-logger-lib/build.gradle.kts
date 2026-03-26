import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
}

apply(from = rootProject.file("gradle/fleet-android-conventions.gradle"))

@OptIn(ExperimentalWasmDsl::class)
kotlin {
    // Android target (compileSdk, minSdk, jvmTarget set by fleet-android-conventions)
    android {
        namespace = "com.indusjs.logger"
    }

    // iOS targets
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    // Web targets
    js { browser() }
    wasmJs { browser() }

    sourceSets {
        commonMain.dependencies {
            // Expose FleetLogger interface from ijs-core-lib
            api(project(":ijs-core-lib"))
            implementation(libs.kermit)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }

        androidMain.dependencies {
            implementation(libs.kotlinx.coroutines.android)
        }
    }
}

