package net.afanasev.sekret.sample

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ValueClassSpec {

    private val mask = "■■■"
    private val modelId = ModelId(111)
    private val name = Name("name")
    private val nestedName = NestedName(Name("nested"))
    private val description = Description("description")

    @Test
    fun `should print non-annotated value class properties`() {
        assertEquals(
            "MyModel(" +
                "id=ModelId(value=111), " +
                "name=my name is name, " +
                "nestedName=NestedName(name=my name is nested), " +
                "description=Description(value=description)" +
                ")",
            MyModel(modelId, name, nestedName, description).toString()
        )
    }

    @Test
    fun `should hide annotated value class properties`() {
        assertEquals(
            "MyModelSecret(" +
                "id=$mask, " +
                "name=$mask, " +
                "nestedName=$mask, " +
                "description=$mask" +
                ")",
            MyModelSecret(modelId, name, nestedName, description).toString()
        )
    }

    @Test
    fun `should print non-annotated properties in data class implementing interface`() {
        assertEquals(
            "MySubModel(" +
                "id=ModelId(value=111), " +
                "name=my name is name, " +
                "nestedName=NestedName(name=my name is nested), " +
                "description=Description(value=description), " +
                "extraField=extra-field" +
                ")",
            MySubModel(modelId, name, nestedName, description, "extra-field").toString()
        )
    }

    @Test
    fun `should hide annotated properties in data class implementing interface`() {
        assertEquals(
            "MySubModelSecret(" +
                "id=$mask, " +
                "name=$mask, " +
                "nestedName=$mask, " +
                "description=$mask, " +
                "anotherExtraField=$mask" +
                ")",
            MySubModelSecret(modelId, name, nestedName, description, "another-extra-field").toString()
        )
    }
}
