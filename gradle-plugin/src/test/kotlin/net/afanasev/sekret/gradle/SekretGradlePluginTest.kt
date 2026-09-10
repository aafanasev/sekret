package net.afanasev.sekret.gradle

import org.gradle.tooling.provider.model.ToolingModelBuilderRegistry
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class SekretGradlePluginTest {

    private val plugin = SekretGradlePlugin(mock(ToolingModelBuilderRegistry::class.java))

    @ParameterizedTest
    @EnumSource(
        value = KotlinPlatformType::class,
        names = ["jvm", "androidJvm", "native"],
    )
    fun `plugin applies to platforms that generate a toString body`(platformType: KotlinPlatformType) {
        assertTrue(plugin.isApplicable(compilationOf(platformType)))
    }

    @ParameterizedTest
    @EnumSource(
        value = KotlinPlatformType::class,
        names = ["jvm", "androidJvm", "native"],
        mode = EnumSource.Mode.EXCLUDE,
    )
    fun `plugin skips platforms it has not been verified against`(platformType: KotlinPlatformType) {
        assertFalse(plugin.isApplicable(compilationOf(platformType)))
    }

    private fun compilationOf(platformType: KotlinPlatformType): KotlinCompilation<*> =
        mock(KotlinCompilation::class.java).also {
            `when`(it.platformType).thenReturn(platformType)
        }
}
