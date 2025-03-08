package net.afanasev.sekret.sample

import org.junit.jupiter.api.Assertions.assertEquals
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object ReplaceByRegexpSpec : Spek({

    describe("Plugin") {
        val mask = "■■■"
        it("should hide properties with annotation even if regexp not matched") {
            assertEquals("Phone(number=$mask)", Phone("root").toString())
        }
        it("should replace properties with annotation ") {
            assertEquals("Phone(number=123****45)", Phone("12300000045").toString())
        }
    }

})
