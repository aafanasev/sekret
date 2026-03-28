package net.afanasev.sekret.sample

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ReplaceByRegexpSpec {

    private val mask = "■■■"

    @Test
    fun `should hide properties with annotation even if regexp not matched`() {
        assertEquals("Phone(number=$mask, additional=$mask, zeroGroupAccess=$mask, unknownGroupAccess=$mask)", Phone("root", "1234", "1234", "1234").toString())
    }

    @Test
    fun `should replace properties with annotation`() {
        assertEquals("Phone(number=123****45, additional=three digits, zeroGroupAccess=$mask, unknownGroupAccess=$mask)", Phone("12300000045", "123", "1234", "1234").toString())
    }

    @Test
    fun `should not fail if field value is null`() {
        assertEquals("Phone(number=null, additional=three digits, zeroGroupAccess=$mask, unknownGroupAccess=$mask)", Phone(null, "123", "123", "123").toString())
    }
}
