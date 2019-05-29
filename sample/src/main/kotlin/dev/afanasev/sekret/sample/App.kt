package dev.afanasev.sekret.sample

data class User(
        val login: String,
        val password: String
)

data class Admin(
        val login: String,
        val password: String
)

class Student(
        val login: String,
        val password: String
)

fun main(args: Array<String>) {
    println(User("John", "Snow"))
    println(Admin("John", "Snow"))
    println(Student("John", "Snow"))
}

