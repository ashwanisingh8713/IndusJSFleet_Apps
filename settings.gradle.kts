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
// ijs-reports-lib and ijs-finance-lib are folded into ijs-network-lib
include(":ijs-error-lib")
include(":ijs-dispatcher-lib")
include(":ijs-datetime-picker")
include(":ijs-datetime-utils")
include(":ijs-pdf-report")

