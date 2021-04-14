package net.afanasev.sekret.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

class SekretGradlePlugin : Plugin<Project> {

    override fun apply(project: Project): Unit = project.run {
        extensions.create("sekret", SekretGradlePluginExtension::class.java)
    }

}
