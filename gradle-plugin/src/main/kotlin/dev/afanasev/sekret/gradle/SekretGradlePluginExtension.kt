package dev.afanasev.sekret.gradle

open class SekretGradlePluginExtension {

    var mask: String = "■■■"
    var enabled: Boolean = true
    var maskNulls: Boolean = true
    var annotations: List<String> = mutableListOf("dev.afanasev.sekret.Secret")

}
