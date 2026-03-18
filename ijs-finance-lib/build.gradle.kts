import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.metro)
}

apply(from = rootProject.file("gradle/fleet-android-conventions.gradle"))

@OptIn(ExperimentalWasmDsl::class)
kotlin {
    android {
        namespace = "com.indusjs.fleet.finance"
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    js { browser() }
    wasmJs { browser() }

    sourceSets {
        commonMain.dependencies {
            api(project(":ijs-network-lib"))

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kermit)
            implementation(libs.ktor.client.core)
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

