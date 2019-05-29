package dev.afanasev.sekret.sample

import dev.afanasev.sekret.Secret

data class User(
        val login: String,
        @Secret val password: String
)

data class Admin(
        val login: String,
        val password: String
) {
    override fun toString() = "secret"
}

class Student(
        val login: String,
        val password: String
)

fun main(args: Array<String>) {
    println(User("John", "Snow"))
    println(Admin("John", "Snow"))
    println(Student("John", "Snow"))
}
