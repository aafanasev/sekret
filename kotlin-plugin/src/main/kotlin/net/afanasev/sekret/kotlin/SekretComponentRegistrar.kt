package net.afanasev.sekret.kotlin

import com.google.auto.service.AutoService
import net.afanasev.sekret.kotlin.SekretOptions.KEY_ANNOTATIONS
import net.afanasev.sekret.kotlin.SekretOptions.KEY_ENABLED
import net.afanasev.sekret.kotlin.SekretOptions.KEY_MASK
import net.afanasev.sekret.kotlin.SekretOptions.KEY_MASK_NULLS
import org.jetbrains.kotlin.codegen.extensions.ClassBuilderInterceptorExtension
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration

@AutoService(ComponentRegistrar::class)
class SekretComponentRegistrar : ComponentRegistrar {

    override fun registerProjectComponents(
        project: MockProject,
        configuration: CompilerConfiguration
    ) {
        if (!configuration.get(KEY_ENABLED, true)) {
            return
        }

        val annotations = configuration.get(KEY_ANNOTATIONS, listOf("net.afanasev.sekret.Secret"))
        val mask = configuration.get(KEY_MASK, "■■■")
        val maskNulls = configuration.get(KEY_MASK_NULLS, true)

        ClassBuilderInterceptorExtension.registerExtension(
            project,
            SekretClassGenerationInterceptor(annotations, mask, maskNulls)
        )
    }
}
