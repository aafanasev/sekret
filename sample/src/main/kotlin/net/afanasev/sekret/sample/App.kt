package net.afanasev.sekret.sample

import net.afanasev.sekret.Secret

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
    val hash: String,
)

data class BaseImpl(
    @Secret override val str: String,
    @Secret override val id: Int?,
    @Secret override val arr: Array<String>,
    @Secret val str2: String,
    val str3: String
) : Base(str, id, arr, str3)

@Suppress("ArrayInDataClass")
data class Complex(
    @Secret val str: String,
    @Secret val int: Int,
    @Secret val long: Long,
    @Secret val float: Float,
    @Secret val list: List<String>,
    @Secret val array: Array<Char>,
    @Secret var boolean: Boolean,
    val admin: Admin,
    @Secret val student: Student,
) {
    companion object {
        const val CONST = "const"
    }

    val field1 = ""
    var field2 = false
}

data class NamingTestImpl(
    @Secret val UpperCaseName: String,
    @Secret override val UpperCaseNameInterface: String,
    @Secret val snake_case: String,
    @Secret override val snake_caseInterface: String,
    @Secret val justBoolean: Boolean,
    @Secret override val justBooleanInterface: Boolean,
    @Secret val hasBoolean: Boolean,
    @Secret override val hasBooleanInterface: Boolean,
    @Secret val isBoolean: Boolean,
    @Secret override val isBooleanInterface: Boolean,
    @Secret val booleanInterface: Boolean,
) : NamingTestInterface

interface NamingTestInterface {
    val UpperCaseNameInterface: String
    val snake_caseInterface: String
    val justBooleanInterface: Boolean
    val hasBooleanInterface: Boolean
    val isBooleanInterface: Boolean
}

fun main() {
    val student = Student("John", "Snow")

    println(User("John", "Snow", student))
    println(Admin("John", "Snow"))
    println(student)

    println(DifferentTypes(1, "hello", true))

    println(Arrays(intArrayOf(1, 2), arrayOf("one", "two")))

    println(BaseImpl("str", null, arrayOf("he", "lo"), "str2", "str3"))

    println(
        Complex(
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

    Interface.main()
    VC.main()

    println(
        NamingTestImpl(
            "uppercase",
            "uppercase2",
            "snakecase",
            "snakecase2",
            true,
            true,
            true,
            true,
            true,
            true,
        )
    )
}
