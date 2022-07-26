package net.afanasev.sekret.sample

import net.afanasev.sekret.Secret

@Suppress("ArrayInDataClass")
data class ComplexImpl(
    @Secret override val str: String,
    @Secret override val int: Int,
    @Secret override val long: Long,
    @Secret override val float: Float,
    @Secret override val list: List<String>,
    @Secret override val array: Array<Char>,
    @Secret override var boolean: Boolean,
    override val admin: Admin,
    @Secret override val student: Student,
) : ComplexInterface {
    companion object {
        const val CONST = "const"
    }

    val field1 = ""
    var field2 = false
}

interface ComplexInterface {
    val str: String
    val int: Int
    val long: Long
    val float: Float
    val list: List<String>
    val array: Array<Char>
    var boolean: Boolean
    val admin: Admin
    val student: Student
}

object Interface {
    fun main() {
        println(
            ComplexImpl(
                str = "string",
                int = 3,
                long = 4L,
                float = 2f,
                list = listOf(),
                array = arrayOf(),
                boolean = true,
                admin = Admin("admin", "pwd"),
                student = Student("student", "pwd"),
            )
        )
    }
}
