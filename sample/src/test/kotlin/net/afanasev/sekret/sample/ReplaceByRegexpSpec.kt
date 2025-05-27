package net.afanasev.sekret.sample

import org.junit.jupiter.api.Assertions.assertEquals
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object ReplaceByRegexpSpec : Spek({

    describe("Plugin") {
        val mask = "■■■"
        it("should hide properties with annotation even if regexp not matched") {
            assertEquals("Phone(number=$mask, additional=$mask)", Phone("root", "1234").toString())
        }
        it("should replace properties with annotation") {
            assertEquals("Phone(number=123****45, additional=three digits)", Phone("12300000045", "123").toString())
        }
        it("should not fail if field value is null") {
            assertEquals("Phone(number=null, additional=three digits)", Phone(null, "123").toString())
            println(Phone(null, "123").toString())
        }
    }

})
