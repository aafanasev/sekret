import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.PluginOption
import com.tschuchort.compiletesting.SourceFile
import net.afanasev.sekret.Secret
import net.afanasev.sekret.kotlin.PLUGIN_ID
import net.afanasev.sekret.kotlin.SekretCommandLineProcessor
import net.afanasev.sekret.kotlin.SekretCompilerPluginRegistrar
import net.afanasev.sekret.kotlin.SekretOptions
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import test.MyAnnotation

@OptIn(ExperimentalCompilerApi::class)
class SekretPluginTest {

    @Test
    fun test() {
        val result = KotlinCompilation().apply {
            sources = listOf(
                SourceFile.kotlin(
                    "main.kt", """
                import net.afanasev.sekret.Secret
                import test.MyAnnotation
                data class DataClass(
                    @test.MyAnnotation("search", "replacement") val id: Int,
                    @Secret val nameAnnotated: String,
                    var publicVariable: String?,
                    private var privateVariable: String?,
                    private val privateVal: String?,
                    val array1: IntArray,
                    val array2: Array<String>,
                    val list: List<String>,
                ) {
                    var localPrimitve: Int = -1
                    lateinit var localLateinit: Any
                }
            """.trimIndent()
                )
            )
            commandLineProcessors = listOf(SekretCommandLineProcessor())
            compilerPluginRegistrars = listOf(SekretCompilerPluginRegistrar())
            inheritClassPath = true
            messageOutputStream = System.out
            pluginOptions = listOf(
                PluginOption(PLUGIN_ID, SekretOptions.KEY_ANNOTATIONS.toString(), MyAnnotation::class.qualifiedName!!),
                PluginOption(PLUGIN_ID, SekretOptions.KEY_ANNOTATIONS.toString(), Secret::class.qualifiedName!!),
            )
        }.compile()

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

}
