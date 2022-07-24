package net.afanasev.sekret.sample

import net.afanasev.sekret.Secret


@JvmInline
value class ModelId(val value: Long) : Comparable<Long> by value

@JvmInline
value class Name(val value: String) {
    override fun toString(): String = "my name is $value"
}

@JvmInline
value class NestedName(val name: Name)

@JvmInline
value class Description(val value: String)

// regular data class - expect Secket has no effect
data class MyModel(
    val id: ModelId,
    val name: Name,
    val nestedName: NestedName,
    val description: Description?,
)

// Secket should censor all @Secret fields from toString()
data class MyModelSecret(
    @Secret val id: ModelId,
    @Secret val name: Name,
    @Secret val nestedName: NestedName,
    @Secret val description: Description?,
)

// regular data class - expect Secket has no effect
data class MySubModel(
    override val id: ModelId,
    override val name: Name,
    override val nestedName: NestedName,
    override val description: Description?,
    val extraField: String,
) : MyModelInterface

// Secket should censor all @Secret fields from toString()
data class MySubModelSecret(
    @Secret override val id: ModelId,
    @Secret override val name: Name,
    @Secret override val nestedName: NestedName,
    @Secret override val description: Description?,
    @Secret val anotherExtraField: String,
) : MyModelInterface

interface MyModelInterface {
    val id: ModelId
    val name: Name
    val nestedName: NestedName
    val description: Description?
}

object VC {
    fun main() {
        val modelId = ModelId(111)
        val name = Name("name")
        val nestedName = NestedName(Name("nested"))
        val description = Description("description")

        println(modelId)
        println(name)
        println(description)

        println(MyModel(modelId, name, nestedName, description))

        println(MyModelSecret(modelId, name, nestedName, description))

        println(MySubModel(modelId, name, nestedName, description, "extra-field"))

        println(MySubModelSecret(modelId, name, nestedName, description, "another-extra-field"))
    }
}
