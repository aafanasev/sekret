package dev.afanasev.sekret.gradle

open class SekretGradlePluginExtension {

    var enabled: Boolean = true
    var annotations: List<String> = mutableListOf("dev.afanasev.sekret.Secret")

}