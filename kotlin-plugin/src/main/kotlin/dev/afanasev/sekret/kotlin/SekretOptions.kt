package dev.afanasev.sekret.kotlin

import org.jetbrains.kotlin.config.CompilerConfigurationKey

object SekretOptions {

    val KEY_ENABLED = CompilerConfigurationKey.create<Boolean>("enabled")

}