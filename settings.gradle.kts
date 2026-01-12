import java.net.URI
import java.util.Properties

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
val localProperties = Properties()
val localPropertiesFile = File(rootDir, "local.properties")

if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use {
        localProperties.load(it)
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { setUrl("https://jitpack.io") }
        maven {
            url = uri("https://maven.innovatrics.com/releases")
        }
        maven {
            url = uri("https://gitlab.com/api/v4/projects/36441060/packages/maven")
            credentials {
                // Ensure these keys match your local.properties EXACTLY
                username = localProperties.getProperty("mobai.header")
                password = localProperties.getProperty("mobai.token")
            }

        }

    }
}

rootProject.name = "Biometric SDK Example"
include(":app")
 