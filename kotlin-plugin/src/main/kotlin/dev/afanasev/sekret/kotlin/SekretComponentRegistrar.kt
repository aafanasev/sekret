package dev.afanasev.sekret.kotlin

import com.google.auto.service.AutoService
import dev.afanasev.sekret.kotlin.SekretOptions.KEY_ENABLED
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
        if (configuration[KEY_ENABLED] == false) {
            // TODO
            // return
        }

        ClassBuilderInterceptorExtension.registerExtension(project, SekretClassGenerationInterceptor())
    }

}