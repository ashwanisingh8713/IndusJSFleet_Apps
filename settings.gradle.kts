rootProject.name = "IndusJSFleet"

pluginManagement {
    repositories {
        google {
            content { 
              	includeGroupByRegex("com\\.android.*")
              	includeGroupByRegex("com\\.google.*")
              	includeGroupByRegex("androidx.*")
              	includeGroupByRegex("android.*")
            }
        }
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            content { 
              	includeGroupByRegex("com\\.android.*")
              	includeGroupByRegex("com\\.google.*")
              	includeGroupByRegex("androidx.*")
              	includeGroupByRegex("android.*")
            }
        }
        mavenCentral()
    }
}
include(":sharedUI")
include(":androidApp")
include(":webApp")
include(":locationTracker")
include(":ijs-core-lib")
include(":ijs-network-lib")
include(":screen-driver")
include(":screen-vehicle")
include(":screen-trip")
include(":screen-customer")
include(":screen-payment")
include(":screen-team")
include(":screen-report")
include(":screen-finance")
include(":ijs-error-lib")
include(":ijs-dispatcher-lib")
include(":ijs-datetime-picker")
include(":ijs-datetime-utils")
include(":ijs-pdf-report")
include(":ijs-ui-components-lib")
include(":screen-user")
include(":screen-onboarding")
include(":screen-map")
include(":screen-alerts")
include(":screen-dashboard")
include(":ijs-logger-lib")
