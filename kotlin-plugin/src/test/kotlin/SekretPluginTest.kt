import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import net.afanasev.sekret.kotlin.SekretCommandLineProcessor
import net.afanasev.sekret.kotlin.SekretCompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCompilerApi::class)
class SekretPluginTest {

    @Test
    fun test() {
        val result = KotlinCompilation().apply {
            sources = listOf(
                SourceFile.kotlin(
                    "main.kt", """
                import net.afanasev.sekret.Secret
                
                data class DataClass(
                    val id: Int,
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
        }.compile()

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

}
