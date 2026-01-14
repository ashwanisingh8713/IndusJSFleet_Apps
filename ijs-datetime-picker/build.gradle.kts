import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

@OptIn(ExperimentalWasmDsl::class)
kotlin {
    // Android target
    android {
        namespace = "com.indusjs.datetimepicker"
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
            // IndusJS Libraries
            api(project(":ijs-datetime-utils"))

            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
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

