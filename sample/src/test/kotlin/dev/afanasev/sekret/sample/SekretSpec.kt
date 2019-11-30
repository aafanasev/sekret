package dev.afanasev.sekret.sample

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
    }

})