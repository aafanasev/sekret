package net.afanasev.sekret.kotlin

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.backend.jvm.ir.getValueArgument
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irConcat
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irGetField
import org.jetbrains.kotlin.ir.builders.irIfThenElse
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.addArgument
import org.jetbrains.kotlin.ir.expressions.impl.IrStringConcatenationImpl
import org.jetbrains.kotlin.ir.interpreter.getAnnotation
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.isArray
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.functions
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.isPrimitiveArray
import org.jetbrains.kotlin.ir.util.properties
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
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
        val regexReplaceFunction = pluginContext.referenceFunctions(CallableId(ClassId.fromString("kotlin.text.Regex"), Name.identifier("replace")))
            .firstOrNull {
                it.owner.valueParameters.size == 2
                    && it.owner.valueParameters.first().type == pluginContext.irBuiltIns.charSequenceClass.defaultType
                    && it.owner.valueParameters.get(1).type == pluginContext.irBuiltIns.stringType
            }
        val regexpConstructor = pluginContext.referenceConstructors(ClassId.fromString(Regex::class.qualifiedName!!))
            .firstOrNull { it.owner.valueParameters.size == 1 }
        val regexMatchesFunction = pluginContext.referenceFunctions(CallableId(ClassId.fromString("kotlin.text.Regex"), Name.identifier("matches")))
            .first { it.owner.valueParameters.size == 1 }

        override fun visitClass(declaration: IrClass): IrStatement {
            if (declaration.isData) {
                val toStringFunction = declaration.functions.find { it.name.asString() == "toString" }
                if (toStringFunction != null) {
                    modifyToStringFunction(toStringFunction, declaration)
                }
            }

            return super.visitClass(declaration)
        }

        private fun findReplacement(annotation: IrConstructorCall): Replacement? {
            if (annotation.valueArgumentsCount < 2) {
                return null
            }
            val rex = (annotation.getValueArgument(Name.identifier("search")) as? IrConst<String>)?.value ?: return null
            val replacement = (annotation.getValueArgument(Name.identifier("replacement")) as? IrConst<String>)?.value ?: return null
            return Replacement(rex, replacement)
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
                            val annotation = getSecretAnnotation(property)
                            if (annotation != null) {
                                val replacement = findReplacement(annotation)
                                if (replacement != null && regexReplaceFunction != null && regexpConstructor != null) {
                                    replaceByRegexp(replacement, toStringFunction, property, this)
                                } else {
                                    addArgument(irString(mask))
                                }
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

        private fun IrBlockBodyBuilder.replaceByRegexp(
            replacement: Replacement,
            toStringFunction: IrSimpleFunction,
            property: IrProperty,
            irStringConcatenationImpl: IrStringConcatenationImpl
        ) {
            checkNotNull(regexpConstructor)
            checkNotNull(regexReplaceFunction)
            /**
             * val regexp = Regexp("myRegexp")
             */
            val regexInstance = irCall(regexpConstructor).apply {
                putValueArgument(0, irString(replacement.searchRegexp))
            }

            /**
             *  regexp.matches(annotatedProperty)
             */
            val matchesCall = irCall(regexMatchesFunction).apply {
                dispatchReceiver = regexInstance
                putValueArgument(0, irGetField(irGet(toStringFunction.dispatchReceiverParameter!!), property.backingField!!))
            }

            /**
             * regexp.replace(annotatedProperty,replacement)
             */
            val replaceCall = irCall(regexReplaceFunction).apply {
                dispatchReceiver = regexInstance
                putValueArgument(0, irGetField(irGet(toStringFunction.dispatchReceiverParameter!!), property.backingField!!)) // Передаем замену
                putValueArgument(1, irString(replacement.replacementString)) // Передаем замену
            }

            /**
             * val masked =  if (regexp.matches(annotatedProperty)){
             *     regexp.replace(annotatedProperty,replacement)
             * }
             * else {
             *   ***
             * }
             */
            val resultExpression = irIfThenElse(
                context.irBuiltIns.stringType,
                matchesCall,
                replaceCall,
                irString(mask)
            )

            irStringConcatenationImpl.addArgument(resultExpression)
        }

        private fun getSecretAnnotation(property: IrProperty) = annotations.firstNotNullOfOrNull {
            when {
                property.hasAnnotation(it) -> property.getAnnotation(it)
                property.backingField?.hasAnnotation(it) == true -> property.backingField?.getAnnotation(it) ?: throw IllegalStateException("Should never happen")
                else -> null
            }
        }

        private fun isArray(property: IrProperty): Boolean {
            val type = property.backingField?.type ?: return false
            return type.isArray() || type.isPrimitiveArray()
        }
    }
}

data class Replacement(val searchRegexp: String, val replacementString: String)
