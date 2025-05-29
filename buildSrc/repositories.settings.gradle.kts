// A settings.gradle.kts plugin for defining shared repositories used by both buildSrc and the root project

@Suppress("UnstableApiUsage") // Central declaration of repositories is an incubating feature
dependencyResolutionManagement {

    repositories {
        mavenLocal()
        mavenCentral()
//        gradlePluginPortal()
    }

    pluginManagement {
        repositories {
            mavenLocal()

            gradlePluginPortal()
            mavenCentral()
        }
    }
}
