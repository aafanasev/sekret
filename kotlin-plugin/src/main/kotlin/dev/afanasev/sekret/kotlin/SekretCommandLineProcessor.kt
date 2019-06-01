import com.google.auto.service.AutoService
import dev.afanasev.sekret.kotlin.SekretOptions.KEY_ANNOTATIONS
import dev.afanasev.sekret.kotlin.SekretOptions.KEY_ENABLED
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration

@AutoService(CommandLineProcessor::class)
class SekretCommandLineProcessor : CommandLineProcessor {

    private val enabled = "enabled"
    private val annotations = "annotations"

    override val pluginId: String = "sekret"

    override val pluginOptions: Collection<AbstractCliOption> = listOf(
            CliOption(
                    enabled,
                    "<true|false>",
                    "Whether plugin is enabled",
                    required = false
            ),
            CliOption(
                    annotations,
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
        enabled -> configuration.put(KEY_ENABLED, value.toBoolean())
        annotations -> configuration.appendList(KEY_ANNOTATIONS, value)
        else -> error("Unexpected config option ${option.optionName}")
    }

}