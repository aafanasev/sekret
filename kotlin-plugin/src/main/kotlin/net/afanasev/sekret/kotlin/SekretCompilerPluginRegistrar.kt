package net.afanasev.sekret.kotlin

import com.google.auto.service.AutoService
import net.afanasev.sekret.kotlin.SekretOptions.KEY_ANNOTATIONS
import net.afanasev.sekret.kotlin.SekretOptions.KEY_ENABLED
import net.afanasev.sekret.kotlin.SekretOptions.KEY_MASK
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.messageCollector
import org.jetbrains.kotlin.name.FqName

@OptIn(ExperimentalCompilerApi::class)
@AutoService(CompilerPluginRegistrar::class)
class SekretCompilerPluginRegistrar : CompilerPluginRegistrar() {

    override val supportsK2: Boolean = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        if (!configuration.get(KEY_ENABLED, true)) {
            return
        }

        val annotations = configuration.get(KEY_ANNOTATIONS, listOf("net.afanasev.sekret.Secret")).map { FqName(it) }.toSet()
        val mask = configuration.get(KEY_MASK, "■■■")
        IrGenerationExtension.registerExtension(SekretGenerationExtension(annotations, mask, configuration.messageCollector))
    }
}
