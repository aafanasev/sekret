package net.afanasev.sekret.sample

import net.afanasev.sekret.Secret


@JvmInline
value class ModelId(val value: Long) : Comparable<Long> by value

@JvmInline
value class Name(val value: String) : CharSequence by value

@JvmInline
value class Description(val value: String)

// regular data class - expect Secket has no effect
data class MyModel(
    val id: ModelId,
    val name: Name,
    val description: Description?,
)

// Secket should censor all @Secret fields from toString()
data class MyModelSecret(
    @Secret val id: ModelId,
    @Secret val name: Name,
    @Secret val description: Description?,
)

// regular data class - expect Secket has no effect
data class MySubModel(
    override val id: ModelId,
    override val name: Name,
    override val description: Description?,
    val extraField: String,
) : MyModelInterface

// Secket should censor all @Secret fields from toString()
data class MySubModelSecret(
    @Secret override val id: ModelId,
    @Secret override val name: Name,
    @Secret override val description: Description?,
    @Secret val anotherExtraField: String,
) : MyModelInterface

interface MyModelInterface {
    val id: ModelId
    val name: Name
    val description: Description?
}

fun main() {

}
object VC {
    fun main() {
        val modelId = ModelId(111)
        val name = Name("name")
        val description = Description("description")

        println(modelId)
        println(name)
        println(description)

        println(MyModel(modelId, name, description))

        println(MyModelSecret(modelId, name, description))

        println(MySubModel(modelId, name, description, "extra-field"))

        println(MySubModelSecret(modelId, name, description, "another-extra-field"))

    }
}
