package net.afanasev.sekret.sample

import org.junit.jupiter.api.Assertions.assertEquals
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object ValueClassSpec : Spek({
    val modelId = ModelId(111)
    val name = Name("name")
    val nestedName = NestedName(Name("nested"))
    val description = Description("description")

    describe("data class with value class properties") {
        val mask = "■■■"

        it("should print non-annotated properties") {
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

        it("should hide annotated properties") {
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

        describe("that implement an interface") {

            it("should print non-annotated properties") {
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

            it("should hide annotated properties") {
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
    }

})
