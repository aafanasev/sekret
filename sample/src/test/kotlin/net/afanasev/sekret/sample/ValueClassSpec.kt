package net.afanasev.sekret.sample

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ValueClassTest {

    private val mask = DEFAULT_MASK
    private val modelId = ModelId(111)
    private val name = Name("name")
    private val nestedName = NestedName(Name("nested"))
    private val description = Description("description")

    @Test
    fun shouldPrintNonAnnotatedProperties() {
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
    fun shouldHideAnnotatedProperties() {
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
    fun implementingInterface_shouldPrintNonAnnotatedProperties() {
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
    fun implementingInterface_shouldHideAnnotatedProperties() {
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
