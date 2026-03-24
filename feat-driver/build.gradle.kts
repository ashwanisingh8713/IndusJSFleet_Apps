import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.metro)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.multiplatform)
}

apply(from = rootProject.file("gradle/fleet-android-conventions.gradle"))
apply(from = rootProject.file("gradle/fleet-compose-conventions.gradle"))

@OptIn(ExperimentalWasmDsl::class)
kotlin {
    // Android target (compileSdk, minSdk, jvmTarget set by fleet-android-conventions)
    android {
        namespace = "com.ijs.driver"
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
            // Expose ijs-network-lib transitively (includes ijs-core-lib → ijs-error-lib, ijs-dispatcher-lib)
            api(project(":ijs-network-lib"))

            // Cross-feature: team members for caretaker assignment
            implementation(project(":feat-team"))

            // PDF report generation + date/time picker
            implementation(project(":ijs-pdf-report"))
            implementation(project(":ijs-datetime-picker"))

            // Kotlin
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kermit)
            implementation(libs.ktor.client.core)
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

