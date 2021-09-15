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
import org.jetbrains.org.objectweb.asm.FieldVisitor
import org.jetbrains.org.objectweb.asm.MethodVisitor
import org.jetbrains.org.objectweb.asm.Opcodes

class SekretClassBuilder(
    internal val classBuilder: ClassBuilder,
    annotations: List<String>,
    private val mask: String,
    @Deprecated("to be removed") private val maskNulls: Boolean
) : DelegatingClassBuilder() {

    private val annotations: List<FqName> = annotations.map { FqName(it) }
    private val fields = linkedMapOf<String, Pair<String, Boolean>>()

    private var order = ""
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
                val hide = annotations.any { descriptor.annotations.hasAnnotation(it) }
                fields[name] = Pair(desc, hide)
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
            object : MethodVisitor(Opcodes.ASM5) {}
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

        fields.onEachIndexed { index, entry ->
            val name = entry.key
            val desc = entry.value.first
            val needToHide = entry.value.second

            mv.visitLdcInsn((if (index == 0) "" else ", ") + "$name=")
            appendToStringBuilder(mv)

            if (needToHide) {
                mv.visitLdcInsn(mask)
                appendToStringBuilder(mv)
            } else {
                mv.visitVarInsn(Opcodes.ALOAD, 0)
                mv.visitFieldInsn(Opcodes.GETFIELD, thisName, name, desc)

                when {
                    // primitives or descriptors supported by StringBuilder
                    desc.length == 1 || STRING_BUILDER_SUPPORTED_DESCRIPTORS.contains(desc) -> {
                        appendToStringBuilder(mv, desc)
                    }
                    // arrays
                    desc[0] == '[' -> {
                        val isPrimitiveArray = desc.length == 2
                        mv.visitMethodInsn(
                            Opcodes.INVOKESTATIC,
                            "java/util/Arrays",
                            "toString",
                            "(${if (isPrimitiveArray) desc else "[Ljava/lang/Object;"})Ljava/lang/String;",
                            false,
                        )
                        appendToStringBuilder(mv)
                    }
                    // others goes as Object
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

    private fun appendToStringBuilder(methodVisitor: MethodVisitor, descriptor: String = "Ljava/lang/String;") {
        methodVisitor.visitMethodInsn(
            Opcodes.INVOKEVIRTUAL,
            STRING_BUILDER,
            "append",
            "($descriptor)Ljava/lang/StringBuilder;",
            false,
        )
    }

    private companion object {
        const val STRING_BUILDER = "java/lang/StringBuilder"
        val STRING_BUILDER_SUPPORTED_DESCRIPTORS =
            setOf("Ljava/lang/String;", "Ljava/lang/StringBuffer;", "Ljava/lang/CharSequence;")
    }

    private fun JvmDeclarationOrigin.classDescriptor() =
        descriptor?.containingDeclaration?.fqNameSafe?.asString()?.replace('.', '/')

    private fun isInDataClass(descriptor: DeclarationDescriptor?): Boolean {
        if (descriptor is IrBasedSimpleFunctionDescriptor) {
            return descriptor.owner.origin == IrDeclarationOrigin.GENERATED_DATA_CLASS_MEMBER
        }
        return false
    }

}
