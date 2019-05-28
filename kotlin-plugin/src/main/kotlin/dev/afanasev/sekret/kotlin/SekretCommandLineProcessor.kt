import com.google.auto.service.AutoService
import dev.afanasev.sekret.kotlin.SekretOptions.KEY_ENABLED
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration

@AutoService(CommandLineProcessor::class)
class SekretCommandLineProcessor : CommandLineProcessor {

    override val pluginId: String = "sekret"

    override val pluginOptions: Collection<AbstractCliOption> = listOf(
            CliOption("enabled", "<true|false>", "whether plugin is enabled", false)
    )

    override fun processOption(
            option: AbstractCliOption,
            value: String,
            configuration: CompilerConfiguration
    ) = when (option.optionName) {
        "enabled" -> configuration.put(KEY_ENABLED, value.toBoolean())
        else -> error("Unexpected config option ${option.optionName}")
    }

}