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

fun main() {
    val student = Student("John", "Snow")

    println(User("John", "Snow", student))
    println(Admin("John", "Snow"))
    println(student)

    println(DifferentTypes(1, "hello", true))
}
