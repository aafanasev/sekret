package net.afanasev.sekret.kotlin

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.addArgument
import org.jetbrains.kotlin.ir.types.isArray
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

class SekretGenerationExtension(
    private val annotations: Set<FqName>,
    private val mask: String,
) : IrGenerationExtension {

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        moduleFragment.transformChildrenVoid(ToStringTransformer(pluginContext, annotations, mask))
    }

    private class ToStringTransformer(
        private val pluginContext: IrPluginContext,
        private val annotations: Set<FqName>,
        private val mask: String,
    ) : IrElementTransformerVoid() {

        override fun visitClass(declaration: IrClass): IrStatement {
            if (declaration.isData) {
                val toStringFunction = declaration.functions.find { it.name.asString() == "toString" }
                if (toStringFunction != null) {
                    modifyToStringFunction(toStringFunction, declaration)
                }
            }

            return super.visitClass(declaration)
        }

        private fun modifyToStringFunction(toStringFunction: IrSimpleFunction, irClass: IrClass) {
            val constructor = irClass.constructors.first { it.isPrimary }
            val parameters = mutableSetOf<Name>()
            val properties = mutableListOf<IrProperty>()

            constructor.valueParameters.forEach { parameter ->
                parameters.add(parameter.name)
            }

            irClass.properties.forEach { property ->
                if (parameters.contains(property.name)) {
                    properties.add(property)
                }
            }

            val builder = DeclarationIrBuilder(pluginContext, toStringFunction.symbol)
            toStringFunction.body = builder.irBlockBody {
                +irReturn(
                    irConcat().apply {
                        addArgument(irString(irClass.name.asString() + "("))

                        properties.onEachIndexed { index, property ->
                            addArgument(irString(property.name.asString() + "="))

                            if (hasSekretAnnotation(property)) {
                                addArgument(irString(mask))
                            } else {
                                val propertyGetter = builder.irCall(property.getter!!.symbol).apply {
                                    dispatchReceiver = irGet(toStringFunction.dispatchReceiverParameter!!)
                                }
                                if (isArray(property)) {
                                    addArgument(builder.irCall(pluginContext.irBuiltIns.dataClassArrayMemberToStringSymbol).apply {
                                        putValueArgument(0, propertyGetter)
                                    })
                                } else {
                                    addArgument(propertyGetter)
                                }
                            }

                            if (index < properties.size - 1) {
                                addArgument(irString(", "))
                            }
                        }

                        addArgument(irString(")"))
                    }
                )
            }
        }

        private fun hasSekretAnnotation(property: IrProperty) = annotations.any {
            property.hasAnnotation(it) || property.backingField?.hasAnnotation(it) == true
        }

        private fun isArray(property: IrProperty): Boolean {
            val type = property.backingField?.type ?: return false
            return type.isArray() || type.isPrimitiveArray()
        }
    }
}
