package net.afanasev.sekret.kotlin

import com.google.auto.service.AutoService
import net.afanasev.sekret.kotlin.SekretOptions.KEY_ANNOTATIONS
import net.afanasev.sekret.kotlin.SekretOptions.KEY_ENABLED
import net.afanasev.sekret.kotlin.SekretOptions.KEY_MASK
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration

const val PLUGIN_ID = "sekret"

@OptIn(ExperimentalCompilerApi::class)
@AutoService(CommandLineProcessor::class)
class SekretCommandLineProcessor : CommandLineProcessor {

    override val pluginId: String = PLUGIN_ID

    override val pluginOptions: Collection<AbstractCliOption> = listOf(
        CliOption(
            KEY_MASK.toString(),
            "Some value which will be used for masking ",
            "Mask, by default it's three squares",
            required = false
        ),
        CliOption(
            KEY_ENABLED.toString(),
            "<true|false>",
            "Whether plugin is enabled",
            required = false
        ),
        CliOption(
            KEY_ANNOTATIONS.toString(),
            "<fqname>",
            "Secret annotations",
            required = false,
            allowMultipleOccurrences = true
        )
    )

    override fun processOption(
        option: AbstractCliOption,
        value: String,
        configuration: CompilerConfiguration
    ) = when (option.optionName) {
        KEY_MASK.toString() -> configuration.put(KEY_MASK, value)
        KEY_ENABLED.toString() -> configuration.put(KEY_ENABLED, value.toBoolean())
        KEY_ANNOTATIONS.toString() -> configuration.appendList(KEY_ANNOTATIONS, value)
        else -> error("Unexpected config option ${option.optionName}")
    }

}
