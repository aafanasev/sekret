package net.afanasev.sekret.sample

import org.junit.jupiter.api.Assertions.assertEquals
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object ValueClassSpec : Spek({
    val modelId = ModelId(111)
    val name = Name("name")
    val description = Description("description")

    describe("data class with value class properties") {
        val mask = "■■■"

        it("should print properties that are value classes, and not annotated") {
            assertEquals(
                "MyModel(id=ModelId(value=111), name=Name(value=name), description=Description(value=description))",
                MyModel(modelId, name, description).toString()
            )
        }

        it("should hide properties that are value classes, and are annotated") {
            assertEquals(
                "MyModelSecret(id=$mask, name=$mask, description=$mask)",
                MyModelSecret(modelId, name, description).toString()
            )
        }

        describe("that implement an interface") {

            it("should print properties that are value classes, and not annotated") {
                assertEquals(
                    "MySubModel(id=ModelId(value=111), name=Name(value=name), description=Description(value=description), extraField=extra-field)",
                    MySubModel(modelId, name, description, "extra-field").toString()
                )
            }

            it("should hide properties that are value classes, and are annotated") {
                assertEquals(
                    "MySubModelSecret(id=$mask, name=$mask, description=$mask, anotherExtraField=$mask)",
                    MySubModelSecret(modelId, name, description, "another-extra-field").toString()
                )
            }
        }
    }

})
