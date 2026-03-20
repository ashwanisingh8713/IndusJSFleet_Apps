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
include(":ijs-driver-lib")
include(":ijs-vehicle-lib")
include(":ijs-trip-lib")
include(":ijs-customer-lib")
include(":ijs-payment-lib")
include(":ijs-team-lib")
include(":ijs-reports-lib")
include(":ijs-finance-lib")
include(":ijs-error-lib")
include(":ijs-dispatcher-lib")
include(":ijs-datetime-picker")
include(":ijs-datetime-utils")
include(":ijs-pdf-report")
include(":ijs-ui-components-lib")

