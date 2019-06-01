package dev.afanasev.sekret.gradle

import com.google.auto.service.AutoService
import org.gradle.api.Project
import org.gradle.api.tasks.compile.AbstractCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonOptions
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinGradleSubplugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

@AutoService(KotlinGradleSubplugin::class)
class SekretGradleSubplugin : KotlinGradleSubplugin<AbstractCompile> {

    override fun isApplicable(project: Project, task: AbstractCompile) =
            project.plugins.hasPlugin(SekretGradlePlugin::class.java)

    override fun apply(
            project: Project,
            kotlinCompile: AbstractCompile,
            javaCompile: AbstractCompile?,
            variantData: Any?,
            androidProjectHandler: Any?,
            kotlinCompilation: KotlinCompilation<KotlinCommonOptions>?
    ): List<SubpluginOption> {
        val extension = project.extensions.findByType(SekretGradlePluginExtension::class.java)
                ?: SekretGradlePluginExtension()

        if (extension.enabled && extension.annotations.isEmpty()) {
            error("Sekret is enabled, but no annotations were set")
        }

        val enabled = SubpluginOption("enabled", extension.enabled.toString())
        val annotations = extension.annotations.map { SubpluginOption("annotations", it) }

        return annotations + enabled
    }

    override fun getCompilerPluginId() = "sekret"

    override fun getPluginArtifact() = SubpluginArtifact("dev.afanasev", "sekret-kotlin-plugin", "0.0.1")

}