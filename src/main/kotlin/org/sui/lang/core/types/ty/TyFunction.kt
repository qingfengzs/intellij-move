package org.sui.lang.core.types.ty

import com.intellij.openapi.project.Project
import org.sui.ide.presentation.tyToString
import org.sui.lang.core.psi.MvFunctionLike
import org.sui.lang.core.psi.MvGenericDeclaration
import org.sui.lang.core.psi.acquiresPathTypes
import org.sui.lang.core.psi.ext.returnTypeTy
import org.sui.lang.core.psi.parameters
import org.sui.lang.core.psi.psiFactory
import org.sui.lang.core.psi.typeParamsToTypeParamsSubst
import org.sui.lang.core.types.infer.*
import org.sui.lang.core.types.infer.loweredType

data class TyFunction(
    override val item: MvGenericDeclaration,
    override val substitution: Substitution,
    override val paramTypes: List<Ty>,
    override val returnType: Ty,
    val acquiresTypes: List<Ty>,
): TyCallable, GenericTy(
    item,
    substitution,
    mergeFlags(paramTypes) or mergeFlags(acquiresTypes) or returnType.flags
) {

    fun needsTypeAnnotation(): Boolean = this.substitution.hasTyInfer

    override fun innerFoldWith(folder: TypeFolder): Ty {
        return TyFunction(
            item,
            substitution.foldValues(folder),
            paramTypes.map { it.foldWith(folder) },
            returnType.foldWith(folder),
            acquiresTypes.map { it.foldWith(folder) },
        )
    }

    override fun innerVisitWith(visitor: TypeVisitor): Boolean =
        substitution.visitValues(visitor)
                || paramTypes.any { it.visitWith(visitor) }
                || returnType.visitWith(visitor)
                || acquiresTypes.any { it.visitWith(visitor) }

    override fun abilities(): Set<Ability> = Ability.all()

    override fun toString(): String = tyToString(this)

    companion object {
        fun unknownTyFunction(project: Project, numParams: Int): TyFunction {
            val fakeFunction = project.psiFactory.function("fun __fake()")
            return TyFunction(
                fakeFunction,
                emptySubstitution,
                generateSequence { TyUnknown }.take(numParams).toList(),
                TyUnknown,
                acquiresTypes = emptyList(),
            )
        }
    }
}

// do not account for explicit type arguments
fun MvFunctionLike.functionTy(msl: Boolean): TyFunction = rawFunctionTy(this, msl)

private fun rawFunctionTy(item: MvFunctionLike, msl: Boolean): TyFunction {
    val typeParamsSubst = item.typeParamsToTypeParamsSubst
    val paramTypes = item.parameters.map { it.type?.loweredType(msl) ?: TyUnknown }
    val acquiredTypes = item.acquiresPathTypes.map { it.loweredType(msl) }
    val retType = item.returnTypeTy(msl)
    return TyFunction(
        item,
        typeParamsSubst,
        paramTypes,
        retType,
        acquiredTypes,
    )
}
