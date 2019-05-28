package sekret.sample

data class User(
        val login: String,
        val password: String
)

fun main(args: Array<String>) {
    println(User("John", "Snow"))
}
