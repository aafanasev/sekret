package dev.afanasev.sekret.kotlin

import jdk.internal.org.objectweb.asm.Opcodes.GETFIELD
import org.jetbrains.kotlin.codegen.ClassBuilder
import org.jetbrains.kotlin.codegen.DelegatingClassBuilder
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.resolve.jvm.diagnostics.JvmDeclarationOrigin
import org.jetbrains.org.objectweb.asm.MethodVisitor
import org.jetbrains.org.objectweb.asm.Opcodes
import org.jetbrains.org.objectweb.asm.commons.InstructionAdapter

class SekretClassBuilder(private val classBuilder: ClassBuilder) : DelegatingClassBuilder() {

    override fun getDelegate(): ClassBuilder = classBuilder

    override fun newMethod(
            origin: JvmDeclarationOrigin,
            access: Int,
            name: String,
            desc: String,
            signature: String?,
            exceptions: Array<out String>?
    ): MethodVisitor {
        val original = super.newMethod(origin, access, name, desc, signature, exceptions)
        val function = origin.descriptor as? FunctionDescriptor ?: return original

        if (name != "toString") {
            return original
        }

        return object : MethodVisitor(Opcodes.ASM5, original) {
            override fun visitInsn(opcode: Int) {
                // TODO: get secret field
                if (opcode == GETFIELD) {
                    InstructionAdapter(this).apply {
                        visitLdcInsn("sekret")
                    }
                } else {
                    super.visitInsn(opcode)
                }
            }
        }
    }

}