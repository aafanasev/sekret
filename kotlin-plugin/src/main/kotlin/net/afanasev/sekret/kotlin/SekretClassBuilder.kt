package net.afanasev.sekret.kotlin

import org.jetbrains.kotlin.codegen.ClassBuilder
import org.jetbrains.kotlin.codegen.DelegatingClassBuilder
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.descriptors.IrBasedSimpleFunctionDescriptor
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.resolve.jvm.diagnostics.JvmDeclarationOrigin
import org.jetbrains.kotlin.types.isNullable
import org.jetbrains.org.objectweb.asm.FieldVisitor
import org.jetbrains.org.objectweb.asm.Label
import org.jetbrains.org.objectweb.asm.MethodVisitor
import org.jetbrains.org.objectweb.asm.Opcodes

class SekretClassBuilder(
    internal val classBuilder: ClassBuilder,
    annotations: List<String>,
    private val mask: String,
    private val maskNulls: Boolean,
) : DelegatingClassBuilder() {

    private val annotations: List<FqName> = annotations.map { FqName(it) }
    private val fields = linkedMapOf<String /* field name in lowercase */, FieldInfo?>()
    private val inlinedFields = linkedMapOf<String, String>()
    private var generateToString = false

    override fun getDelegate(): ClassBuilder = classBuilder

    override fun newField(
        origin: JvmDeclarationOrigin,
        access: Int,
        name: String,
        desc: String,
        signature: String?,
        value: Any?
    ): FieldVisitor {
        if (generateToString) {
            (origin.descriptor as? PropertyDescriptor)?.let { descriptor ->
                val fieldKey = name.lowercase()
                if (fields.contains(fieldKey)) {
                    val isHidden = annotations.any { descriptor.annotations.hasAnnotation(it) }
                    fields[fieldKey] = FieldInfo(name, desc, isHidden, descriptor.type.isNullable())
                }
            }
        }
        return super.newField(origin, access, name, desc, signature, value)
    }

    override fun newMethod(
        origin: JvmDeclarationOrigin,
        access: Int,
        name: String,
        desc: String,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        return if (name == "toString" && isInDataClass(origin.descriptor)) {
            generateToString = true
            // skipping toString generation as it will be constructed manually in done() function.
            object : MethodVisitor(Opcodes.ASM5) {
                override fun visitFieldInsn(
                    opcode: Int,
                    owner: String,
                    name: String,
                    descriptor: String
                ) {
                    if (opcode == Opcodes.GETFIELD) {
                        fields[name.lowercase()] = null
                    }
                }

                override fun visitMethodInsn(
                    opcode: Int,
                    owner: String,
                    name: String,
                    descriptor: String,
                    isInterface: Boolean
                ) {
                    when {
                        isLocalGetter(opcode, owner, name) -> {
                            val fieldKey = name
                                .substring(if (name[0] == 'g') 3 else 2) // skip "get" or "is"
                                .substringBefore('-') // remove mangling
                                .lowercase()
                            fields[fieldKey] = null
                        }
                        isToStringOfInlinedClass(opcode, owner, name) -> {
                            val lastlyAddedFieldKey = fields.keys.last()
                            inlinedFields[lastlyAddedFieldKey] = owner
                        }
                    }
                }

                private fun isLocalGetter(opcode: Int, className: String, methodName: String) =
                    opcode == Opcodes.INVOKEVIRTUAL
                        && className == origin.classDescriptor()
                        && (methodName.startsWith("get") || methodName.startsWith("is"))

                private fun isToStringOfInlinedClass(opcode: Int, className: String, methodName: String) =
                    opcode == Opcodes.INVOKESTATIC
                        && className != origin.classDescriptor()
                        && methodName == INLINED_TO_STRING_NAME

            }
        } else {
            super.newMethod(origin, access, name, desc, signature, exceptions)
        }
    }

    override fun done() {
        if (generateToString) {
            generateToString()
        }
        super.done()
    }

    private fun generateToString() {
        val mv = newMethod(
            JvmDeclarationOrigin.NO_ORIGIN, Opcodes.ACC_PUBLIC, "toString", "()Ljava/lang/String;", null, null,
        )
        mv.visitCode()
        mv.visitTypeInsn(Opcodes.NEW, STRING_BUILDER)
        mv.visitInsn(Opcodes.DUP)
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, STRING_BUILDER, "<init>", "()V", false)
        mv.visitLdcInsn("${thisName.split("/").last()}(")
        appendToStringBuilder(mv)

        fields.onEachIndexed { index, (fieldKey, fieldInfo) ->
            checkNotNull(fieldInfo) { "Missing field info: $fieldKey" }

            mv.visitLdcInsn((if (index == 0) "" else ", ") + fieldInfo.name + '=')
            appendToStringBuilder(mv)

            if (fieldInfo.isHidden) {
                if (maskNulls && fieldInfo.isNullable) {
                    val loadNullLabel = Label()
                    val appendStringLabel = Label()

                    mv.visitVarInsn(Opcodes.ALOAD, 0)
                    mv.visitFieldInsn(Opcodes.GETFIELD, thisName, fieldInfo.name, fieldInfo.desc)
                    mv.visitJumpInsn(Opcodes.IFNULL, loadNullLabel)

                    mv.visitLdcInsn(mask)
                    mv.visitJumpInsn(Opcodes.GOTO, appendStringLabel)

                    mv.visitLabel(loadNullLabel)
                    mv.visitLdcInsn("null")

                    mv.visitLabel(appendStringLabel)
                    appendToStringBuilder(mv)
                } else {
                    mv.visitLdcInsn(mask)
                    appendToStringBuilder(mv)
                }
            } else {
                mv.visitVarInsn(Opcodes.ALOAD, 0)
                mv.visitFieldInsn(Opcodes.GETFIELD, thisName, fieldInfo.name, fieldInfo.desc)

                when {
                    inlinedFields.contains(fieldKey) -> {
                        mv.visitMethodInsn(
                            Opcodes.INVOKESTATIC,
                            inlinedFields[fieldKey],
                            INLINED_TO_STRING_NAME,
                            "(${fieldInfo.desc})Ljava/lang/String;",
                            false,
                        )
                        appendToStringBuilder(mv, "Ljava/lang/Object;")
                    }
                    // primitives or descriptors supported by StringBuilder
                    fieldInfo.desc.length == 1
                        || STRING_BUILDER_SUPPORTED_DESCRIPTORS.contains(fieldInfo.desc) -> {
                        appendToStringBuilder(mv, fieldInfo.desc)
                    }
                    // arrays
                    fieldInfo.desc[0] == '[' -> {
                        val isPrimitiveArray = fieldInfo.desc.length == 2
                        mv.visitMethodInsn(
                            Opcodes.INVOKESTATIC,
                            "java/util/Arrays",
                            "toString",
                            "(${if (isPrimitiveArray) fieldInfo.desc else "[Ljava/lang/Object;"})Ljava/lang/String;",
                            false,
                        )
                        appendToStringBuilder(mv)
                    }
                    // others go as Object
                    else -> {
                        appendToStringBuilder(mv, "Ljava/lang/Object;")
                    }
                }
            }
        }

        mv.visitLdcInsn(")")
        appendToStringBuilder(mv)

        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, STRING_BUILDER, "toString", "()Ljava/lang/String;", false)
        mv.visitInsn(Opcodes.ARETURN)
        mv.visitEnd()
    }

    private fun isInDataClass(descriptor: DeclarationDescriptor?): Boolean {
        if (descriptor is IrBasedSimpleFunctionDescriptor) {
            return descriptor.owner.origin == IrDeclarationOrigin.GENERATED_DATA_CLASS_MEMBER
        }
        return false
    }

    private fun appendToStringBuilder(methodVisitor: MethodVisitor, descriptor: String = "Ljava/lang/String;") {
        methodVisitor.visitMethodInsn(
            Opcodes.INVOKEVIRTUAL,
            STRING_BUILDER,
            "append",
            "($descriptor)Ljava/lang/StringBuilder;",
            false,
        )
    }

    private fun JvmDeclarationOrigin.classDescriptor() =
        descriptor?.containingDeclaration?.fqNameSafe?.asString()?.replace('.', '/')

    private companion object {
        const val INLINED_TO_STRING_NAME = "toString-impl"
        const val STRING_BUILDER = "java/lang/StringBuilder"
        val STRING_BUILDER_SUPPORTED_DESCRIPTORS =
            setOf("Ljava/lang/String;", "Ljava/lang/StringBuffer;", "Ljava/lang/CharSequence;")
    }
}
