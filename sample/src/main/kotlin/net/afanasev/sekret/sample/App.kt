package net.afanasev.sekret.sample

import net.afanasev.sekret.Secret

data class User(
    val login: String,
    @Secret val password: String,
    @net.afanasev.sekret.Secret val student: Student
)

class Student(
        val login: String,
        val password: String
)

fun main() {
    val student = Student("John", "Snow")

    println(User("John", "Snow", student))

}
