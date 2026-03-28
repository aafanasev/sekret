package net.afanasev.sekret.sample

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

class SekretSpec {

    private val mask = "■■■"

    @Test
    fun `should hide properties with annotation`() {
        assertEquals("Admin(login=root, password=$mask)", Admin("root", "pwd").toString())
    }

    @Test
    fun `should NOT hide properties without annotation`() {
        assertFalse(Student("student", "pwd").toString().contains(mask))
    }

    @Test
    fun `should hide nested object with annotation`() {
        assertEquals("User(login=root, password=$mask, student=$mask)", User("root", "pwd", Student("student", "")).toString())
    }

    @Test
    fun `should hide not only strings`() {
        assertEquals("DifferentTypes(integer=$mask, string=hi, boolean=$mask)", DifferentTypes(123, "hi", true).toString())
    }

    @Test
    fun `should hide arrays`() {
        assertEquals("Arrays(ints=$mask, strings=$mask)", Arrays(intArrayOf(1, 2), arrayOf("one", "two")).toString())
    }

    @Test
    fun `should hide getters`() {
        assertEquals("BaseImpl(str=$mask, id=$mask, arr=$mask, str2=$mask, str3=str3)", BaseImpl("str", 42, arrayOf("a", "r"), "str3", "str3").toString())
    }

    @Test
    fun `should hide properties of a complex object`() {
        assertEquals(
            "Complex(" +
                "str=$mask, " +
                "int=$mask, " +
                "long=$mask, " +
                "float=$mask, " +
                "list=$mask, " +
                "array=$mask, " +
                "boolean=$mask, " +
                "admin=Admin(login=admin, password=$mask), " +
                "student=$mask, " +
                "phone=Phone(number=123****45, additional=three digits, zeroGroupAccess=$mask, unknownGroupAccess=$mask)" +
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
                phone = Phone("123888888845", "123", "1234", "1234"),
            ).toString()
        )
    }
}
