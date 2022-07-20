package net.afanasev.sekret.sample

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object SekretSpec : Spek({

    describe("Plugin") {
        val mask = "■■■"

        it("should hide properties with annotation") {
            assertEquals("Admin(login=root, password=$mask)", Admin("root", "pwd").toString())
        }

        it("should NOT hide properties without annotation") {
            assertFalse(Student("student", "pwd").toString().contains(mask))
        }

        it("should hide nested object with annotation") {
            assertEquals("User(login=root, password=$mask, student=$mask)", User("root", "pwd", Student("student", "")).toString())
        }

        it("should hide not only strings") {
            assertEquals("DifferentTypes(integer=$mask, string=hi, boolean=$mask)", DifferentTypes(123, "hi", true).toString())
        }

        it("should hide arrays") {
            assertEquals("Arrays(ints=$mask, strings=$mask)", Arrays(intArrayOf(1, 2), arrayOf("one", "two")).toString())
        }

        it("should hide getters") {
            assertEquals("BaseImpl(str=$mask, id=$mask, arr=$mask, str2=$mask, str3=str3)", BaseImpl("str", 42, arrayOf("a", "r"), "str3", "str3").toString())
        }

        it("should hide getters") {
            assertEquals("BaseImpl(str=$mask, id=$mask, arr=$mask, str2=$mask, str3=str3)", BaseImpl("str", 42, arrayOf("a", "r"), "str3", "str3").toString())
        }

        it("should hide properties of a complex object") {
            assertEquals(
                "Complex(" +
                    "str=■■■, " +
                    "int=■■■, " +
                    "long=■■■, " +
                    "float=■■■, " +
                    "list=■■■, " +
                    "array=■■■, " +
                    "boolean=■■■, " +
                    "admin=Admin(login=admin, password=■■■), " +
                    "student=■■■" +
                    ")",
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
                ).toString()
            )
        }
    }

})
