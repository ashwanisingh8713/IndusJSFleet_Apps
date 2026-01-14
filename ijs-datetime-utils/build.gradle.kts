import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
}

@OptIn(ExperimentalWasmDsl::class)
kotlin {
    // Android target
    android {
        namespace = "com.indusjs.datetimeutils"
        compileSdk = 36
        minSdk = 23
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
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

