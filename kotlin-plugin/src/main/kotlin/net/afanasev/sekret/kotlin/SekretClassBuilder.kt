package net.afanasev.sekret.kotlin

import org.jetbrains.kotlin.codegen.ClassBuilder
import org.jetbrains.kotlin.codegen.DelegatingClassBuilder
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.resolve.jvm.diagnostics.JvmDeclarationOrigin
import org.jetbrains.org.objectweb.asm.FieldVisitor
import org.jetbrains.org.objectweb.asm.Label
import org.jetbrains.org.objectweb.asm.MethodVisitor
import org.jetbrains.org.objectweb.asm.Opcodes
import org.jetbrains.org.objectweb.asm.commons.InstructionAdapter

class SekretClassBuilder(
        internal val classBuilder: ClassBuilder,
        annotations: List<String>,
        private val mask: String,
        private val maskNulls: Boolean
) : DelegatingClassBuilder() {

    private val annotations: List<FqName> = annotations.map { FqName(it) }
    private val secretFields = mutableSetOf<String>()
    private val secretArrayFields = mutableSetOf<String>()

    override fun getDelegate(): ClassBuilder = classBuilder

    override fun newField(
            origin: JvmDeclarationOrigin,
            access: Int,
            name: String,
            desc: String,
            signature: String?,
            value: Any?
    ): FieldVisitor {
        (origin.descriptor as? PropertyDescriptor)?.backingField?.let { descriptor ->
            if (annotations.any { descriptor.annotations.hasAnnotation(it) }) {
                secretFields.add(name)
                if (desc.first() == '[') {
                    secretArrayFields.add(name)
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
        val original = super.newMethod(origin, access, name, desc, signature, exceptions)

        if (name != "toString") {
            return original
        }

        return object : MethodVisitor(Opcodes.ASM5, original) {

            private var appendLabel: Label? = null
            private var skipNextInstruction: Boolean = false
            private var replaceAppendDescriptor: Boolean = false

            override fun visitFieldInsn(opcode: Int, owner: String?, name: String?, descriptor: String?) {
                if (opcode == Opcodes.GETFIELD && secretFields.contains(name)) {
                    skipNextInstruction = secretArrayFields.contains(name)
                    replaceAppendDescriptor = true

                    if (maskNulls) {
                        InstructionAdapter(this).apply {
                            pop()
                            visitLdcInsn(mask)
                        }
                    } else {
                        super.visitFieldInsn(opcode, owner, name, descriptor)
                        addInstructionsToPrintNullsAsIs()
                    }
                } else {
                    super.visitFieldInsn(opcode, owner, name, descriptor)
                }
            }

            override fun visitMethodInsn(opcode: Int, owner: String?, name: String?, descriptor: String?, isInterface: Boolean) {
                val field = getFieldFromGetter(opcode, owner, name)
                if (field != null && secretFields.contains(field)) {
                    skipNextInstruction = secretArrayFields.contains(field)
                    replaceAppendDescriptor = true

                    if (maskNulls) {
                        InstructionAdapter(this).apply {
                            pop()
                            visitLdcInsn(mask)
                        }
                    } else {
                        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
                        addInstructionsToPrintNullsAsIs()
                    }

                    return
                }

                // skip Array.toString() method
                if (skipNextInstruction) {
                    skipNextInstruction = false
                    return
                }

                val newDescriptor = if (
                        opcode == Opcodes.INVOKEVIRTUAL
                        && owner == STRING_BUILDER
                        && name == APPEND_METHOD
                        && replaceAppendDescriptor
                ) {
                    replaceAppendDescriptor = false

                    appendLabel?.let {
                        visitLabel(appendLabel)
                        appendLabel = null
                    }

                    APPEND_DESCRIPTOR
                } else {
                    descriptor
                }

                super.visitMethodInsn(opcode, owner, name, newDescriptor, isInterface)
            }

            private fun getFieldFromGetter(opcode: Int, owner: String?, name: String?): String? {
                if (
                        opcode != Opcodes.INVOKEVIRTUAL
                        || owner != origin.classDescriptor()
                        || name?.startsWith("get") != true
                ) {
                    return null
                }

                return name.substring(3).decapitalize()
            }

            private fun addInstructionsToPrintNullsAsIs() {
                appendLabel = Label()
                val ifNullLabel = Label()

                InstructionAdapter(this).apply {
                    ifnull(ifNullLabel)

                    visitLabel(Label())
                    visitLdcInsn(mask)
                    goTo(appendLabel)

                    visitLabel(ifNullLabel)
                    visitLdcInsn("null")
                    goTo(appendLabel)
                }
            }
        }
    }

    private companion object {
        const val STRING_BUILDER = "java/lang/StringBuilder"
        const val APPEND_METHOD = "append"
        const val APPEND_DESCRIPTOR = "(Ljava/lang/String;)Ljava/lang/StringBuilder;"
    }

    private fun JvmDeclarationOrigin.classDescriptor() =
            descriptor?.containingDeclaration?.fqNameSafe?.asString()?.replace('.', '/')

}
