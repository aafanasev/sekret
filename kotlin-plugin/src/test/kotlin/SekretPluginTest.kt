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
        assertEquals(
            "DataClass(id=■■■, nameAnnotated=■■■, publicVariable=pub, privateVariable=null, privateVal=pv, " +
                "array1=[1, 2], array2=[a], list=[x])",
            result.invokeToString("DataClass", 42, "name", "pub", null, "pv", intArrayOf(1, 2), arrayOf("a"), listOf("x"))
        )
    }

    @Test
    fun `regex replacement is applied to non-CharSequence property`() {
        val result = compile("""
            import net.afanasev.sekret.Secret
            data class D(@test.MyAnnotation("(\\d)\\d*", "${'$'}1**") val n: Int, @Secret val s: String)
        """)
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
        assertEquals("D(n=1**, s=■■■)", result.invokeToString("D", 12345, "s"))
    }

    @Test
    fun `non-matching regex on non-CharSequence property is masked`() {
        val result = compile("""
            data class D(@test.MyAnnotation("(\\d)\\d*", "${'$'}1**") val n: Int)
        """)
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
        assertEquals("D(n=■■■)", result.invokeToString("D", -5))
    }

    @Test
    fun `null non-CharSequence property with regex prints null`() {
        val result = compile("""
            data class D(@test.MyAnnotation("(\\d)\\d*", "${'$'}1**") val n: Long?)
        """)
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
        assertEquals("D(n=null)", result.invokeToString("D", null))
        assertEquals("D(n=9**)", result.invokeToString("D", 987L))
    }

    @Test
    fun `regex replacement on String property is unchanged`() {
        val result = compile("""
            data class D(
                @test.MyAnnotation("(\\d{3})\\d+(\\d{2})", "${'$'}1****${'$'}2") val phone: String,
                @test.MyAnnotation("(\\d{3})\\d+(\\d{2})", "${'$'}1****${'$'}2") val other: String?,
            )
        """)
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
        assertEquals("D(phone=123****45, other=null)", result.invokeToString("D", "12300000045", null))
        assertEquals("D(phone=■■■, other=■■■)", result.invokeToString("D", "root", "abc"))
    }

    @Test
    fun `regex replacement on value class property uses its toString`() {
        val result = compile("""
            @JvmInline value class Name(val value: String) {
                override fun toString(): String = "name:${'$'}value"
            }
            @JvmInline value class Id(val value: Long)
            data class D(
                @test.MyAnnotation("name:(\\w)\\w*", "${'$'}1***") val name: Name,
                @test.MyAnnotation("Id\\(value=(\\d)\\d*\\)", "${'$'}1***") val id: Id?,
            )
            fun render(): String = D(Name("john"), Id(123)).toString()
            fun renderNull(): String = D(Name("john"), null).toString()
        """)
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
        val facade = result.classLoader.loadClass("MainKt")
        assertEquals("D(name=j***, id=1***)", facade.getMethod("render").invoke(null))
        assertEquals("D(name=j***, id=null)", facade.getMethod("renderNull").invoke(null))
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
