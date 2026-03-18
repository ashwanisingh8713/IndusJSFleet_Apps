import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.metro)
    alias(libs.plugins.room)
    alias(libs.plugins.ksp)
    alias(libs.plugins.buildConfig)
}

apply(from = rootProject.file("gradle/fleet-android-conventions.gradle"))

@OptIn(ExperimentalWasmDsl::class)
kotlin {
    // Android target (compileSdk, minSdk, jvmTarget set by fleet-android-conventions)
    android {
        namespace = "com.indusjs.fleet"
        androidResources.enable = true
    }

    js { browser() }
    wasmJs { browser() }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            // IndusJS Libraries
            implementation(project(":ijs-core-lib"))
            implementation(project(":ijs-network-lib"))
            implementation(project(":ijs-driver-lib"))
            implementation(project(":ijs-vehicle-lib"))
            implementation(project(":ijs-trip-lib"))
            implementation(project(":ijs-customer-lib"))
            implementation(project(":ijs-payment-lib"))
            implementation(project(":ijs-team-lib"))
            implementation(project(":ijs-reports-lib"))
            implementation(project(":ijs-finance-lib"))
            implementation(project(":ijs-error-lib"))
            implementation(project(":ijs-dispatcher-lib"))
            implementation(project(":ijs-datetime-picker"))
            implementation(project(":ijs-datetime-utils"))
            api(project(":ijs-pdf-report"))

            implementation(libs.compose.runtime)
            implementation(libs.compose.ui)
            implementation(libs.compose.foundation)
            implementation(libs.compose.resources)
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.compose.material3)
            implementation(libs.kermit)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.serialization)
            implementation(libs.ktor.serialization.json)
            implementation(libs.ktor.client.logging)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime)
            implementation(libs.compose.nav3)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.multiplatformSettings)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kstore)
            implementation(libs.materialKolor)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.compose.ui.test)
            implementation(libs.kotlinx.coroutines.test)
        }

        androidMain.dependencies {
            implementation(libs.compose.ui.tooling)
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.kstore.file)
            implementation(libs.room.runtime)
        }

        webMain.dependencies {
            implementation(libs.kstore.storage)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
            implementation(libs.kstore.file)
            implementation(libs.room.runtime)
        }

    }

    targets
        .withType<KotlinNativeTarget>()
        .matching { it.konanTarget.family.isAppleFamily }
        .configureEach {
            binaries {
                framework {
                    baseName = "SharedUI"
                    isStatic = true
                }
            }
        }
}

buildConfig {
    // BuildConfig configuration here.
    // https://github.com/gmazzo/gradle-buildconfig-plugin#usage-in-kts
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    with(libs.room.compiler) {
        add("kspAndroid", this)
        add("kspIosX64", this)
        add("kspIosArm64", this)
        add("kspIosSimulatorArm64", this)
    }
}
