package net.afanasev.sekret.kotlin

import org.jetbrains.kotlin.codegen.ClassBuilder
import org.jetbrains.kotlin.codegen.ClassBuilderFactory
import org.jetbrains.kotlin.codegen.ClassBuilderMode
import org.jetbrains.kotlin.codegen.extensions.ClassBuilderInterceptorExtension
import org.jetbrains.kotlin.diagnostics.DiagnosticSink
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.jvm.diagnostics.JvmDeclarationOrigin

class SekretClassGenerationInterceptor(
    private val annotations: List<String>,
    private val mask: String,
    private val maskNulls: Boolean,
) : ClassBuilderInterceptorExtension {

    override fun interceptClassBuilderFactory(
        interceptedFactory: ClassBuilderFactory,
        bindingContext: BindingContext,
        diagnostics: DiagnosticSink,
    ) = object : ClassBuilderFactory {

        override fun newClassBuilder(origin: JvmDeclarationOrigin): ClassBuilder {
            return SekretClassBuilder(interceptedFactory.newClassBuilder(origin), annotations, mask, maskNulls)
        }

        override fun getClassBuilderMode(): ClassBuilderMode {
            return interceptedFactory.classBuilderMode
        }

        override fun asText(builder: ClassBuilder?): String? {
            return interceptedFactory.asText((builder as SekretClassBuilder).classBuilder)
        }

        override fun asBytes(builder: ClassBuilder?): ByteArray? {
            return interceptedFactory.asBytes((builder as SekretClassBuilder).classBuilder)
        }

        override fun close() {
            interceptedFactory.close()
        }
    }
}
