package net.afanasev.sekret.gradle

import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.tooling.provider.model.ToolingModelBuilderRegistry
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption
import javax.inject.Inject

/**
 * Kotlin gradle subplugin that adds the compiler dependency to Gradle project
 */
class SekretGradlePlugin @Inject internal constructor(
    private val registry: ToolingModelBuilderRegistry,
) : KotlinCompilerPluginSupportPlugin {

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean =
        (kotlinCompilation.platformType == KotlinPlatformType.jvm || kotlinCompilation.platformType == KotlinPlatformType.androidJvm)

    override fun apply(target: Project) {
        target.extensions.create("sekret", SekretGradlePluginExtension::class.java)
    }

    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        val project = kotlinCompilation.target.project

        return project.provider {
            val extension = project.extensions.getByType(SekretGradlePluginExtension::class.java)

            val mask = SubpluginOption("mask", extension.mask)
            val enabled = SubpluginOption("enabled", extension.enabled.toString())
            val maskNulls = SubpluginOption("maskNulls", extension.maskNulls.toString())
            val annotations = extension.annotations.map { SubpluginOption("annotations", it) }

            annotations + mask + enabled + maskNulls
        }
    }

    override fun getCompilerPluginId() = "sekret"

    override fun getPluginArtifact() = SubpluginArtifact("net.afanasev", "sekret-kotlin-plugin", "0.1.1-RC3")

}
