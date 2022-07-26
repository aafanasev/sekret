package net.afanasev.sekret.kotlin

data class FieldInfo(
    val name: String,
    val desc: String,
    val isHidden: Boolean,
    val isNullable: Boolean,
)
