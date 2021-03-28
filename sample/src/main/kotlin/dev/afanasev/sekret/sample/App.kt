package dev.afanasev.sekret.sample

import dev.afanasev.sekret.Secret

data class User(
        val login: String,
        @Secret val password: String,
        @Secret val student: Student
)

data class Admin(
        val login: String,
        @Secret val password: String
)

class Student(
        val login: String,
        val password: String
)

data class DifferentTypes(
        @Secret val integer: Int,
        val string: String,
        @Secret val boolean: Boolean
)

@Suppress("ArrayInDataClass")
data class Arrays(
        @Secret val ints: IntArray,
        @Secret val strings: Array<String>
)

abstract class Base(
        open val str: String,
        open val id: Int?,
        open val arr: Array<String>,
        val hash: String

)

data class BaseImpl(
        @Secret override val str: String,
        @Secret override val id: Int?,
        @Secret override val arr: Array<String>,
        @Secret val str2: String,
        val str3: String
) : Base(str, id, arr, str3)

fun main() {
    val student = Student("John", "Snow")

    println(User("John", "Snow", student))
    println(Admin("John", "Snow"))
    println(student)

    println(DifferentTypes(1, "hello", true))

    println(Arrays(intArrayOf(1, 2), arrayOf("one", "two")))

    println(BaseImpl("str", null, arrayOf("he", "lo"), "str2", "str3"))
}
