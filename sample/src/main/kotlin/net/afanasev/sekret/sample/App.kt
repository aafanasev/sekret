package net.afanasev.sekret.sample

import net.afanasev.sekret.Secret

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.SOURCE)
annotation class AnnotationWithReplacement(val search: String, val replacement: String)

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

data class Phone(
    @AnnotationWithReplacement("([0-9]{3})(.*)([0-9]{2})", "$1****$3")
    val number: String,

    @AnnotationWithReplacement("\\d{3}", "three digits")
    val additional: String,
)

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
    val phone: Phone,
) {
    companion object {
        const val CONST = "const"
    }

    val field1 = ""
    var field2 = false
}


fun main() {
    val student = Student("John", "Snow")

    println(User("John", "Snow", student))
    println(Admin("John", "Snow"))
    println(Phone("1238767676767645", "123"))
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
            phone = Phone("123000000045", "1234"),
        )
    )

    Interface.main()
    VC.main()

}
