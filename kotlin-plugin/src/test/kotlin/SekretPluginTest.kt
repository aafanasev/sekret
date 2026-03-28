import com.tschuchort.compiletesting.JvmCompilationResult
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

    private fun compile(source: String, vararg extraAnnotations: String): JvmCompilationResult {
        return KotlinCompilation().apply {
            sources = listOf(SourceFile.kotlin("main.kt", source.trimIndent()))
            commandLineProcessors = listOf(SekretCommandLineProcessor())
            compilerPluginRegistrars = listOf(SekretCompilerPluginRegistrar())
            inheritClassPath = true
            messageOutputStream = System.out
            pluginOptions = buildList {
                add(PluginOption(PLUGIN_ID, SekretOptions.KEY_ANNOTATIONS.toString(), Secret::class.qualifiedName!!))
                add(PluginOption(PLUGIN_ID, SekretOptions.KEY_ANNOTATIONS.toString(), MyAnnotation::class.qualifiedName!!))
                extraAnnotations.forEach {
                    add(PluginOption(PLUGIN_ID, SekretOptions.KEY_ANNOTATIONS.toString(), it))
                }
            }
        }.compile()
    }

    private fun JvmCompilationResult.invokeToString(className: String, vararg args: Any?): String {
        val klass = classLoader.loadClass(className)
        val constructor = klass.constructors.first()
        val instance = constructor.newInstance(*args)
        return instance.toString()
    }

    @Test
    fun `compilation succeeds`() {
        val result = compile("""
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
        """)
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `annotated property is masked in toString`() {
        val result = compile("""
            import net.afanasev.sekret.Secret
            data class Credentials(val login: String, @Secret val password: String)
        """)
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
        assertEquals(
            "Credentials(login=admin, password=■■■)",
            result.invokeToString("Credentials", "admin", "secret123")
        )
    }

    @Test
    fun `unannotated properties are not masked`() {
        val result = compile("""
            import net.afanasev.sekret.Secret
            data class User(val name: String, @Secret val token: String)
        """)
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
        val output = result.invokeToString("User", "alice", "tok123")
        assertEquals("User(name=alice, token=■■■)", output)
    }

    @Test
    fun `class-level annotation masks entire toString`() {
        val result = compile("""
            import net.afanasev.sekret.Secret
            @Secret data class SensitiveData(val a: String, val b: Int)
        """)
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
        assertEquals("SensitiveData(■■■)", result.invokeToString("SensitiveData", "value", 42))
    }

    @Test
    fun `custom mask is applied`() {
        val result = KotlinCompilation().apply {
            sources = listOf(SourceFile.kotlin("main.kt", """
                import net.afanasev.sekret.Secret
                data class Credentials(val login: String, @Secret val password: String)
            """.trimIndent()))
            commandLineProcessors = listOf(SekretCommandLineProcessor())
            compilerPluginRegistrars = listOf(SekretCompilerPluginRegistrar())
            inheritClassPath = true
            messageOutputStream = System.out
            pluginOptions = listOf(
                PluginOption(PLUGIN_ID, SekretOptions.KEY_ANNOTATIONS.toString(), Secret::class.qualifiedName!!),
                PluginOption(PLUGIN_ID, SekretOptions.KEY_MASK.toString(), "***"),
            )
        }.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
        assertEquals(
            "Credentials(login=admin, password=***)",
            result.invokeToString("Credentials", "admin", "secret123")
        )
    }

    @Test
    fun `plugin disabled leaves toString unchanged`() {
        val result = KotlinCompilation().apply {
            sources = listOf(SourceFile.kotlin("main.kt", """
                import net.afanasev.sekret.Secret
                data class Credentials(val login: String, @Secret val password: String)
            """.trimIndent()))
            commandLineProcessors = listOf(SekretCommandLineProcessor())
            compilerPluginRegistrars = listOf(SekretCompilerPluginRegistrar())
            inheritClassPath = true
            messageOutputStream = System.out
            pluginOptions = listOf(
                PluginOption(PLUGIN_ID, SekretOptions.KEY_ANNOTATIONS.toString(), Secret::class.qualifiedName!!),
                PluginOption(PLUGIN_ID, SekretOptions.KEY_ENABLED.toString(), "false"),
            )
        }.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
        assertEquals(
            "Credentials(login=admin, password=secret123)",
            result.invokeToString("Credentials", "admin", "secret123")
        )
    }
}
