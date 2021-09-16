package net.afanasev.sekret.gradle

/**
 * Sekret gradle plugin configuration options
 */
open class SekretGradlePluginExtension {

    var mask: String = "■■■"
    var enabled: Boolean = true
    var annotations: List<String> = mutableListOf("net.afanasev.sekret.Secret")

}
