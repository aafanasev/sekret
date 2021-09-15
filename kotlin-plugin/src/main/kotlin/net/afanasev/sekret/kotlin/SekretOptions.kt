package net.afanasev.sekret.kotlin

import org.jetbrains.kotlin.config.CompilerConfigurationKey

object SekretOptions {

    val KEY_MASK = CompilerConfigurationKey.create<String>("mask")
    val KEY_ENABLED = CompilerConfigurationKey.create<Boolean>("enabled")
    val KEY_ANNOTATIONS = CompilerConfigurationKey.create<List<String>>("annotations")

}
