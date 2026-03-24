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
include(":feat-driver")
include(":feat-vehicle")
include(":feat-trip")
include(":feat-customer")
include(":feat-payment")
include(":feat-team")
include(":feat-report")
include(":feat-finance")
include(":ijs-error-lib")
include(":ijs-dispatcher-lib")
include(":ijs-datetime-picker")
include(":ijs-datetime-utils")
include(":ijs-pdf-report")
include(":ijs-ui-components-lib")
include(":feat-user")
include(":feat-onboarding")
include(":feat-map")
include(":feat-alerts")
include(":feat-dashboard")

