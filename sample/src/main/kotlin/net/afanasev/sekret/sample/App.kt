package net.afanasev.sekret.sample

import net.afanasev.sekret.Secret
import java.lang.StringBuilder

data class User(
    val id: Int,
    val lon: Long,
    @Secret val flo: Float,
    val dou: Double?,
    val login: String,
    val an: Any,
    val buffer1: StringBuffer,
    @Secret val buffer2: StringBuilder,
    val char: Char,
    @Secret val charSeq: CharSequence,
    val bool: Boolean,
    val charArr: CharArray,
    @Secret val password: String?,
    @net.afanasev.sekret.Secret val student: Student,
    val array: Array<String>,
    val list: List<Any>,
    @Secret val Map: Map<String, Int>,
)

class Student(
    val login: String,
    val password: String
)

fun main() {
    val student = Student("John", "Snow")

    println(
        User(
            42, 10L, 3.2f, 55.6, "John",
            "anything", StringBuffer(), StringBuilder(), 's', "char seq", false, charArrayOf('a', 'b', 'c'),
            null, student, arrayOf("str1", "str2"), listOf("zyx", "111"), mapOf("twenty" to 20),
        )
    )

}
