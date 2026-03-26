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
    android {
        namespace = "com.ijs.customer"
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    js { browser() }
    wasmJs { browser() }

    sourceSets {
        commonMain.dependencies {
            // Data layer (networking, auth, HTTP client)
            api(project(":ijs-network-lib"))

            // PDF report generation (for customer trips/payments/financials export)
            implementation(project(":ijs-pdf-report"))

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

