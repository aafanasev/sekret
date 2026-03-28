package net.afanasev.sekret.sample

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ReplaceByRegexpTest {

    private val mask = DEFAULT_MASK

    @Test
    fun shouldHidePropertiesWithAnnotationEvenIfRegexpNotMatched() {
        assertEquals(
            "Phone(number=$mask, additional=$mask, zeroGroupAccess=$mask, unknownGroupAccess=$mask)",
            Phone("root", "1234", "1234", "1234").toString()
        )
    }

    @Test
    fun shouldReplacePropertiesWithAnnotation() {
        assertEquals(
            "Phone(number=123****45, additional=three digits, zeroGroupAccess=$mask, unknownGroupAccess=$mask)",
            Phone("12300000045", "123", "1234", "1234").toString()
        )
    }

    @Test
    fun shouldNotFailIfFieldValueIsNull() {
        assertEquals(
            "Phone(number=null, additional=three digits, zeroGroupAccess=$mask, unknownGroupAccess=$mask)",
            Phone(null, "123", "123", "123").toString()
        )
    }
}
