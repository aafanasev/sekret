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
    private val maskNulls: Boolean
) : DelegatingClassBuilder() {

    private val annotations: List<FqName> = annotations.map { FqName(it) }
    private val secretFields = mutableSetOf<String>()
    private val secretArrayFields = mutableSetOf<String>()

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
            // TODO: reverse the logic
            (origin.descriptor as? PropertyDescriptor)?.let { descriptor ->
                if (annotations.any { descriptor.annotations.hasAnnotation(it) }) {
                    secretFields.add(name)
                    if (desc.first() == '[') {
                        secretArrayFields.add(name)
                    }
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
        return if (name == "toString" && origin.descriptor?.isInDataClass() == true) {
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
//        mv.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder")
//        mv.visitInsn(Opcodes.DUP)
//        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false)
        mv.visitLdcInsn("hello")
//        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false)
//        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false)
        mv.visitInsn(Opcodes.ARETURN)
        //mv.visitMaxs(1, 1)
        mv.visitEnd()
    }

    private companion object {
        const val STRING_BUILDER = "java/lang/StringBuilder"
        const val APPEND_METHOD = "append"
        const val APPEND_DESCRIPTOR = "(Ljava/lang/String;)Ljava/lang/StringBuilder;"
    }

    private fun JvmDeclarationOrigin.classDescriptor() =
        descriptor?.containingDeclaration?.fqNameSafe?.asString()?.replace('.', '/')

    private fun DeclarationDescriptor.isInDataClass(): Boolean {
        if (this is IrBasedSimpleFunctionDescriptor) {
            return owner.origin == IrDeclarationOrigin.GENERATED_DATA_CLASS_MEMBER
        }
        return false
    }

}
