import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.android.kmp.library)
}

apply(from = rootProject.file("gradle/fleet-android-conventions.gradle"))

// Make resources (icons, fonts) accessible to dependent modules (sharedUI, feature modules)
compose.resources {
    publicResClass = true
}

@OptIn(ExperimentalWasmDsl::class)
kotlin {
    android {
        namespace = "com.indusjs.uicomponents"
        androidResources.enable = true
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    js { browser() }
    wasmJs { browser() }

    sourceSets {
        commonMain.dependencies {
            // Foundation — exposes shared DTOs, MVI base, error types, StatusConstants
            api(project(":ijs-core-lib"))

            // Compose Multiplatform
            implementation(libs.compose.runtime)
            implementation(libs.compose.ui)
            implementation(libs.compose.foundation)
            implementation(libs.compose.resources)
            implementation(libs.compose.material3)

            // Utilities
            implementation(libs.kermit)
            implementation(libs.kotlinx.datetime)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }

        androidMain.dependencies {
            implementation(libs.kotlinx.coroutines.android)
        }
    }
}

