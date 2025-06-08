package net.afanasev.sekret.kotlin

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.addArgument
import org.jetbrains.kotlin.ir.expressions.impl.IrStringConcatenationImpl
import org.jetbrains.kotlin.ir.interpreter.getAnnotation
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.isArray
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name


class SekretGenerationExtension(
    private val annotations: Set<FqName>,
    private val mask: String,
    private val messageCollector: MessageCollector,
) : IrGenerationExtension {

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        moduleFragment.transformChildrenVoid(ToStringTransformer(pluginContext, annotations, mask, messageCollector))
    }

    private class ToStringTransformer(
        private val pluginContext: IrPluginContext,
        private val annotations: Set<FqName>,
        private val mask: String,
        val messageCollector: MessageCollector,
    ) : IrElementTransformerVoid() {
        val usedGroupRegex = "(\\$(\\d+))".toRegex()
        val regexClassId = ClassId.fromString(Regex::class.qualifiedName!!)
        val regexConstructor = pluginContext.referenceConstructors(regexClassId)
            .firstOrNull { it.owner.valueParameters.size == 1 }
        val regexMatchesFunction = pluginContext.referenceFunctions(CallableId(regexClassId, Name.identifier("matches")))
            .firstOrNull { it.owner.valueParameters.size == 1 }
        val regexReplaceFunction = pluginContext.referenceFunctions(CallableId(regexClassId, Name.identifier("replace")))
            .firstOrNull {
                it.owner.valueParameters.size == 2
                    && it.owner.valueParameters[0].type == pluginContext.irBuiltIns.charSequenceClass.defaultType
                    && it.owner.valueParameters[1].type == pluginContext.irBuiltIns.stringType
            }

        init {
            if(regexConstructor==null){
                messageCollector.report(
                    CompilerMessageSeverity.WARNING,
                    "Replace functionality is DISABLED : Unable to find  constructor having exactly 1 argument ${regexClassId.asFqNameString()}")
            }
            if(regexMatchesFunction==null){
                messageCollector.report(
                    CompilerMessageSeverity.WARNING,
                    "Replace functionality is DISABLED : Unable to find 'Regexp.matches' ")
            }
            if(regexReplaceFunction==null){
                messageCollector.report(
                    CompilerMessageSeverity.WARNING,
                    "Replace functionality is DISABLED : Unable to find 'Regexp.replace' ")
            }
        }

        override fun visitClass(declaration: IrClass): IrStatement {
            if (declaration.isData && hasAnySecretAnnotation(declaration)) {
                val toStringFunction = declaration.functions.find { it.name.asString() == "toString" }
                if (toStringFunction != null) {
                    modifyToStringFunction(toStringFunction, declaration)
                }
            }

            return super.visitClass(declaration)
        }

        private fun hasAnySecretAnnotation(kclass: IrClass): Boolean {
            return annotations.any { annotation ->
                kclass.properties.any { property ->
                    property.hasAnnotation(annotation) || property.backingField?.hasAnnotation(annotation) ?: false
                }
            }
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
                                val replacement = findReplacement(annotation, irClass, property)
                                if (replacement != null) {
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

        private fun findReplacement(
            annotation: IrConstructorCall,
            classContainedProperty: IrClass,
            annotatedProperty: IrProperty
        ): Replacement? {
            if (regexConstructor == null
                || regexMatchesFunction == null
                || regexReplaceFunction == null
            ) {
                // did not "load" kotlin.text.Regex class and its functions
                return null
            }

            if (annotation.valueArgumentsCount != 2) {
                return null
            }

            val searchRegex = getValueArgument(annotation, "search")?.value
            val replacement = getValueArgument(annotation, "replacement")?.value
            if (searchRegex == null || replacement == null) {
                messageCollector.report(
                    CompilerMessageSeverity.WARNING,
                    "Unable to apply ${annotation.type.classFqName} to ${classContainedProperty.name}.${annotatedProperty.name}." +
                        "Annotation must have search and replacement attributes." +
                        " Some of them is missing. search =  $searchRegex, replacement = $replacement  "
                )
                return null
            }
            val toRegex = searchRegex.toRegex()
            val availableRegexpGroups = (1..toRegex.toPattern().matcher("").groupCount()).toSet()
            val referencedRegexpGroups = usedGroupRegex
                .findAll(replacement)
                .map { it.groupValues[2] }
                .mapNotNull { it.toIntOrNull() }
                .toSet()
            // in  case @SomeAnnotation(search="No groups search",replacement="$0")
            if (referencedRegexpGroups.contains(0)) {
                messageCollector.report(
                    CompilerMessageSeverity.WARNING,
                    "Unable to apply ${annotation.type.classFqName} to ${classContainedProperty.name}.${annotatedProperty.name}." +
                        "Replacement [${replacement}] referenced to $0  which represents original unmasked value. For security reasons " +
                        "value  will be replaced by default mask "
                )
                return null
            }

            val unknownGroups = referencedRegexpGroups - availableRegexpGroups
            // in  case @SomeAnnotation(search="No groups search",replacement="$1-***-$2")
            if (unknownGroups.isNotEmpty()) {
                messageCollector.report(
                    CompilerMessageSeverity.WARNING,
                    "Unable to apply ${annotation.type.classFqName} to ${classContainedProperty.name}.${annotatedProperty.name}." +
                        "Replacement [${replacement}] referenced to groups (${unknownGroups}) that does not exists  in search [${searchRegex}]. " +
                        "Value  will be replaced by default mask "
                )
                return null
            }
            return Replacement(searchRegex, replacement)
        }

        private fun getValueArgument(annotation: IrConstructorCall, name: String): IrConst<String>? {
            @Suppress("UNCHECKED_CAST")
            return annotation.getValueArgument(Name.identifier(name)) as? IrConst<String>
        }

        private fun IrBlockBodyBuilder.replaceByRegexp(
            replacement: Replacement,
            toStringFunction: IrSimpleFunction,
            property: IrProperty,
            irStringConcatenationImpl: IrStringConcatenationImpl
        ) {
            checkNotNull(regexConstructor)
            checkNotNull(regexMatchesFunction)
            checkNotNull(regexReplaceFunction)

            // val regexp = Regexp("myRegexp")
            val regexInstance = irCall(regexConstructor).apply {
                putValueArgument(0, irString(replacement.searchRegexp))
            }

            // regexp.matches(annotatedProperty)
            val matchesCall = irCall(regexMatchesFunction).apply {
                dispatchReceiver = regexInstance
                putValueArgument(0, irGetField(irGet(toStringFunction.dispatchReceiverParameter!!), property.backingField!!))
            }

            // regexp.replace(annotatedProperty, replacement)
            val replaceCall = irCall(regexReplaceFunction).apply {
                dispatchReceiver = regexInstance
                putValueArgument(0, irGetField(irGet(toStringFunction.dispatchReceiverParameter!!), property.backingField!!))
                putValueArgument(1, irString(replacement.replacementString))
            }

            /**
             * val masked =  if (regexp.matches(annotatedProperty)){
             *     regexp.replace(annotatedProperty,replacement)
             * }
             * else {
             *   ***
             * }
             */

            val regexpExpression = irIfThenElse(
                context.irBuiltIns.stringType,
                matchesCall,
                replaceCall,
                irString(mask)
            )
            val nonNullExpression = irIfThenElse(
                context.irBuiltIns.stringType,
                irEqualsNull(irGetField(irGet(toStringFunction.dispatchReceiverParameter!!), property.backingField!!)),
                irString("null"),
                regexpExpression
            )

            irStringConcatenationImpl.addArgument(nonNullExpression)
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

internal data class Replacement(
    val searchRegexp: String,
    val replacementString: String,
)
