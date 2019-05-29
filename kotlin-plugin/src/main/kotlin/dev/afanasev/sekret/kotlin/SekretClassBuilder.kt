package dev.afanasev.sekret.kotlin

import org.jetbrains.kotlin.codegen.ClassBuilder
import org.jetbrains.kotlin.codegen.DelegatingClassBuilder
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

        if (name != "toString") {
            return original
        }

        return object : MethodVisitor(Opcodes.ASM5, original) {

            override fun visitFieldInsn(opcode: Int, owner: String?, name: String?, descriptor: String?) {
                if (opcode == Opcodes.GETFIELD && name == "password") {
                    InstructionAdapter(this).apply {
                        pop()
                        visitLdcInsn("■■■")
                    }
                } else {
                    super.visitFieldInsn(opcode, owner, name, descriptor)
                }
            }

        }
    }

}