package net.afanasev.sekret.sample.kmp

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Runs on every configured target, so a regression that only affects one backend still fails
 * the build. Kotlin/Native is the reason this module exists: JVM tests alone cannot prove the
 * plugin reached an iOS compilation.
 */
class SekretKmpTest {

    @Test
    fun `annotated property is masked`() {
        assertEquals(
            "Credentials(login=bob@example.com, password=■■■)",
            Credentials("bob@example.com", "hunter2").toString(),
        )
    }

    @Test
    fun `annotated class masks every property`() {
        assertEquals(
            "AuthToken(■■■)",
            AuthToken("eyJhbGciOiJIUzI1NiJ9", 1700000000).toString(),
        )
    }

    @Test
    fun `array property is masked while unannotated arrays still print`() {
        val session = Session("s-1", byteArrayOf(1, 2, 3), arrayOf("read", "write"))

        assertEquals(
            "Session(id=s-1, key=■■■, scopes=[read, write])",
            session.toString(),
        )
    }

    @Test
    fun `matching value is partially revealed by the replacement pattern`() {
        assertEquals(
            "PaymentCard(number=****-****-****-3456)",
            PaymentCard("1234567890123456").toString(),
        )
    }

    @Test
    fun `non-matching value falls back to the default mask`() {
        assertEquals(
            "PaymentCard(number=■■■)",
            PaymentCard("not-a-card").toString(),
        )
    }

    @Test
    fun `null value prints as null rather than the mask`() {
        assertEquals("Contact(phone=null)", Contact(null).toString())
    }

    @Test
    fun `nullable value is masked when present`() {
        assertEquals("Contact(phone=***-4567)", Contact("1234567").toString())
    }

    @Test
    fun `secret never appears anywhere in the output`() {
        assertTrue("hunter2" !in Credentials("bob@example.com", "hunter2").toString())
    }
}
