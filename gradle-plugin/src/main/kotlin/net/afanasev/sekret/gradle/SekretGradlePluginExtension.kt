package net.afanasev.sekret.gradle

open class SekretGradlePluginExtension {

    var mask: String = "■■■"
    var enabled: Boolean = true
    var maskNulls: Boolean = true
    var annotations: List<String> = mutableListOf("net.afanasev.sekret.Secret")

}
