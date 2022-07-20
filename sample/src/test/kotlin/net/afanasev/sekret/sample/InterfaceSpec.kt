package net.afanasev.sekret.sample

import org.junit.jupiter.api.Assertions.assertEquals
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object InterfaceSpec : Spek({

    describe("data class that implements an interface") {
        val mask = "■■■"

        it("annotated properties should be hidden") {
            assertEquals(
                "ComplexImpl(" +
                    "str=$mask, " +
                    "int=$mask, " +
                    "long=$mask, " +
                    "float=$mask, " +
                    "list=$mask, " +
                    "array=$mask, " +
                    "boolean=$mask, " +
                    "admin=Admin(login=admin, password=$mask), " +
                    "student=$mask" +
                    ")",
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
                ).toString()
            )
        }
    }
})
