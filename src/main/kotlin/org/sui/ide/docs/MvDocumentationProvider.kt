package org.sui.ide.docs

import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.lang.documentation.DocumentationMarkup
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.sui.ide.presentation.presentationInfo
import org.sui.ide.presentation.text
import org.sui.ide.presentation.typeLabel
import org.sui.lang.core.psi.*
import org.sui.lang.core.psi.ext.*
import org.sui.lang.core.types.infer.inference
import org.sui.lang.core.types.infer.loweredType
import org.sui.lang.core.types.ty.Ty
import org.sui.lang.core.types.ty.TyUnknown
import org.sui.lang.moveProject
import org.sui.stdext.joinToWithBuffer

class MvDocumentationProvider : AbstractDocumentationProvider() {
    override fun getCustomDocumentationElement(
        editor: Editor,
        file: PsiFile,
        contextElement: PsiElement?,
        targetOffset: Int
    ): PsiElement? {
        val namedAddress = contextElement?.ancestorOrSelf<MvNamedAddress>()
        if (namedAddress != null) return namedAddress
        return super.getCustomDocumentationElement(editor, file, contextElement, targetOffset)
    }

    override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
        val buffer = StringBuilder()
        var docElement = element
        if (
            docElement is MvBindingPat && docElement.owner is MvConst
        )
            docElement = docElement.owner

        when (docElement) {
            is MvNamedAddress -> {
                // TODO: add docs for both [addresses] and [dev-addresses]
                val moveProject = docElement.moveProject ?: return null
                val refName = docElement.referenceName
                val named = moveProject.getNamedAddress(refName) ?: return null
                val address =
                    named.addressLit()?.original ?: angleWrapped("unassigned")
                return "$refName = \"$address\""
            }
            is MvDocAndAttributeOwner -> generateOwnerDoc(docElement, buffer)
            is MvBindingPat -> {
                val presentationInfo = docElement.presentationInfo ?: return null
                val msl = docElement.isMslOnlyItem
                val inference = docElement.inference(msl) ?: return null
                val type = inference.getPatType(docElement).renderForDocs(true)
                buffer += presentationInfo.type
                buffer += " "
                buffer.b { it += presentationInfo.name }
                buffer += ": "
                buffer += type
            }

            is MvTypeParameter -> {
                val presentationInfo = docElement.presentationInfo ?: return null
                buffer += presentationInfo.type
                buffer += " "
                buffer.b { it += presentationInfo.name }
                val abilities = docElement.abilityBounds
                if (abilities.isNotEmpty()) {
                    abilities.joinToWithBuffer(buffer, " + ", ": ") { generateDocumentation(it) }
                }
            }
        }
        return if (buffer.isEmpty()) null else buffer.toString()
    }

    private fun generateOwnerDoc(element: MvDocAndAttributeOwner, buffer: StringBuilder) {
        definition(buffer) {
            element.signature(it)
        }
        val text = element.documentationAsHtml()
        buffer += "\n" // Just for more pretty html text representation
        content(buffer) { it += text }
    }
}

fun MvDocAndAttributeOwner.documentationAsHtml(): String {
    return docComments()
        .flatMap { it.text.split("\n") }
        .map { it.trimStart('/', ' ') }
        .map { "<p>$it</p>" }
        .joinToString("\n")
}

fun generateFunction(function: MvFunction, buffer: StringBuilder) {
    val module = function.module
    if (module != null) {
        buffer += module.qualName?.editorText() ?: "unknown"
        buffer += "\n"
    }
    if (function.isNative) buffer += "native "
    buffer += "fun "
    buffer.b { it += function.name }
    function.typeParameterList?.generateDocumentation(buffer)
    function.functionParameterList?.generateDocumentation(buffer)
    function.returnType?.generateDocumentation(buffer)
}

fun MvElement.signature(builder: StringBuilder) {
    val buffer = StringBuilder()
    // no need for msl type conversion in docs
    val msl = false
//    val msl = this.isMslLegacy()
    when (this) {
        is MvFunction -> generateFunction(this, buffer)
        is MvModule -> {
            buffer += "module "
            buffer += this.qualName?.editorText() ?: "unknown"
        }

        is MvStruct -> {
            buffer += this.module.qualName?.editorText() ?: "unknown"
            buffer += "\n"

            buffer += "struct "
            buffer.b { it += this.name }
            this.typeParameterList?.generateDocumentation(buffer)
            this.abilitiesList?.abilityList
                ?.joinToWithBuffer(buffer, ", ", " has ") { generateDocumentation(it) }
        }

        is MvNamedFieldDecl -> {
            val module = this.structItem?.module
//            val itemContext = this.structItem.outerItemContext(msl)
            buffer += module?.qualName?.editorText() ?: "unknown"
            buffer += "::"
            buffer += this.structItem?.name ?: angleWrapped("anonymous")
            buffer += "\n"
            buffer.b { it += this.name }
            buffer += ": ${(this.type?.loweredType(msl) ?: TyUnknown).renderForDocs(true)}"
//            buffer += ": ${itemContext.getStructFieldItemTy(this).renderForDocs(true)}"
        }

        is MvConst -> {
//            val itemContext = this.outerItemContext(msl)
            buffer += this.module?.qualName?.editorText() ?: angleWrapped("unknown")
            buffer += "\n"
            buffer += "const "
            buffer.b { it += this.name ?: angleWrapped("unknown") }
            buffer += ": ${(this.type?.loweredType(msl) ?: TyUnknown).renderForDocs(false)}"
//            buffer += ": ${itemContext.getConstTy(this).renderForDocs(false)}"
            this.initializer?.let { buffer += " ${it.text}" }
        }

        else -> return
    } ?: return
    listOf(buffer.toString()).joinTo(builder, "<br>")
}

private fun PsiElement.generateDocumentation(
    buffer: StringBuilder,
    prefix: String = "",
    suffix: String = ""
) {
    buffer += prefix
    when (this) {
        is MvType -> {
            val msl = this.isMsl()
            buffer += this.loweredType(msl)
                .typeLabel(this)
                .replace("<", "&lt;")
                .replace(">", "&gt;")
        }

        is MvFunctionParameterList ->
            this.functionParameterList
                .joinToWithBuffer(buffer, ", ", "(", ")") { generateDocumentation(it) }

        is MvFunctionParameter -> {
            buffer += this.patBinding.identifier.text
            this.typeAnnotation?.type?.generateDocumentation(buffer, ": ")
        }

        is MvTypeParameterList ->
            this.typeParameterList
                .joinToWithBuffer(buffer, ", ", "&lt;", "&gt;") { generateDocumentation(it) }

        is MvTypeParameter -> {
            if (this.isPhantom) {
                buffer += "phantom"
                buffer += " "
            }
            buffer += this.identifier?.text
            val bound = this.typeParamBound
            if (bound != null) {
                abilityBounds.joinToWithBuffer(buffer, " + ", ": ") { generateDocumentation(it) }
            }
        }

        is MvAbility -> {
            buffer += this.text
        }

        is MvReturnType -> this.type?.generateDocumentation(buffer, ": ")
    }
    buffer += suffix
}

private inline fun definition(buffer: StringBuilder, block: (StringBuilder) -> Unit) {
    buffer += DocumentationMarkup.DEFINITION_START
    block(buffer)
    buffer += DocumentationMarkup.DEFINITION_END
}

private inline fun content(buffer: StringBuilder, block: (StringBuilder) -> Unit) {
    buffer += DocumentationMarkup.CONTENT_START
    block(buffer)
    buffer += DocumentationMarkup.CONTENT_END
}

private fun angleWrapped(text: String): String = "&lt;$text&gt;"

private fun Ty.renderForDocs(fq: Boolean): String {
    val original = this.text(fq)
    return original
        .replace("<", "&lt;")
        .replace(">", "&gt;")
}

private operator fun StringBuilder.plusAssign(value: String?) {
    if (value != null) {
        append(value)
    }
}

private inline fun StringBuilder.b(action: (StringBuilder) -> Unit) {
    append("<b>")
    action(this)
    append("</b>")
}
