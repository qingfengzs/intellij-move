package org.sui.lang.core.psi

import com.intellij.ide.projectView.PresentationData
import com.intellij.openapi.editor.colors.TextAttributesKey
import org.sui.cli.settings.moveSettings
import org.sui.ide.MoveIcons
import org.sui.lang.MvElementTypes
import org.sui.lang.core.completion.MvCompletionContext
import org.sui.lang.core.psi.ext.*
import org.sui.lang.core.stubs.MvModuleStub
import org.sui.lang.core.types.infer.InferenceContext
import org.sui.lang.core.types.infer.deepFoldTyInferWith
import org.sui.lang.core.types.infer.loweredType
import org.sui.lang.core.types.infer.substitute
import org.sui.lang.core.types.ty.*

interface MvFunctionLike: MvNameIdentifierOwner,
                          MvGenericDeclaration,
                          MvDocAndAttributeOwner {

    val functionParameterList: MvFunctionParameterList?

    val returnType: MvReturnType?
}

val MvFunctionLike.functionItemPresentation: PresentationData?
    get() {
        val name = this.name ?: return null
        val signature = this.signatureText
        return PresentationData(
            "$name$signature",
            this.locationString(true),
            MoveIcons.FUNCTION,
            TextAttributesKey.createTextAttributesKey("public")
        )
    }

val MvFunctionLike.isNative get() = hasChild(MvElementTypes.NATIVE)

val MvFunctionLike.parameters get() = this.functionParameterList?.functionParameterList.orEmpty()

val MvFunctionLike.parametersAsBindings: List<MvPatBinding> get() = this.parameters.map { it.patBinding }

val MvFunctionLike.valueParamsAsBindings: List<MvPatBinding>
    get() {
        val msl = this.isMslOnlyItem
        val parameters = this.parameters
        return parameters
            .filter { it.type?.loweredType(msl) !is TyLambda }
            .map { it.patBinding }
    }

val MvFunctionLike.lambdaParamsAsBindings: List<MvPatBinding>
    get() {
        val msl = this.isMslOnlyItem
        val parameters = this.parameters
        return parameters
            .filter { it.type?.loweredType(msl) is TyLambda }
            .map { it.patBinding }
    }

val MvFunctionLike.acquiresPathTypes: List<MvPathType>
    get() =
        when (this) {
            is MvFunction -> this.acquiresType?.pathTypeList.orEmpty()
            else -> emptyList()
        }

val MvFunctionLike.anyBlock: AnyBlock?
    get() = when (this) {
        is MvFunction -> this.codeBlock
        is MvSpecFunction -> this.specCodeBlock
        is MvSpecInlineFunction -> this.specCodeBlock
        else -> null
    }

val MvFunctionLike.module: MvModule?
    get() =
        when (this) {
            is MvFunction -> {
                val moduleStub = greenStub?.parentStub as? MvModuleStub
                if (moduleStub != null) {
                    moduleStub.psi
                } else {
                    this.parent as? MvModule
                }
            }
            // TODO:
            else -> null
        }

val MvFunctionLike.script: MvScript? get() = this.parent as? MvScript

val MvFunctionLike.signatureText: String
    get() {
        val paramsText = this.parameters.joinToSignature()
        val retType = this.returnType?.type?.text ?: ""
        val retTypeSuffix = if (retType == "") "" else ": $retType"
        return "$paramsText$retTypeSuffix"
    }

val MvFunction.selfParam: MvFunctionParameter?
    get() {
        if (!project.moveSettings.enableReceiverStyleFunctions) return null
        return this.parameters.firstOrNull()?.takeIf { it.name == "self" }
    }

fun MvFunction.selfParamTy(msl: Boolean): Ty? = this.selfParam?.type?.loweredType(msl)

val MvFunction.isMethod get() = selfParam != null

val MvFunction.selfSignatureText: String
    get() {
        val paramsText = this.parameters.drop(1).joinToSignature()
        val retType = this.returnType?.type?.text ?: ""
        val retTypeSuffix = if (retType == "") "" else ": $retType"
        return "$paramsText$retTypeSuffix"
    }

fun MvFunctionLike.requiresExplicitlyProvidedTypeArguments(completionContext: MvCompletionContext?): Boolean {
    val msl = this.isMslOnlyItem
    val callTy = this.functionTy(msl).substitute(this.tyVarsSubst) as TyFunction

    val inferenceCtx = InferenceContext(msl)
    callTy.paramTypes.forEach {
        inferenceCtx.combineTypes(it, it.deepFoldTyInferWith { TyUnknown })
    }

    val expectedTy = completionContext?.expectedTy
    if (expectedTy != null && expectedTy !is TyUnknown) {
        inferenceCtx.combineTypes(callTy.returnType, expectedTy)
    }
    val resolvedCallTy = inferenceCtx.resolveTypeVarsIfPossible(callTy) as TyFunction

    return resolvedCallTy.needsTypeAnnotation()
}