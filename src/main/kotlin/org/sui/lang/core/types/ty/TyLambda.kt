package org.sui.lang.core.types.ty

import org.sui.ide.presentation.tyToString
import org.sui.lang.core.types.infer.TypeFolder
import org.sui.lang.core.types.infer.TypeVisitor
import org.sui.lang.core.types.infer.mergeFlags

// TODO: inherit from GenericTy ?
interface TyCallable {
    val paramTypes: List<Ty>
    val returnType: Ty
}

data class TyLambda(
    override val paramTypes: List<Ty>,
    override val returnType: Ty
) : Ty(mergeFlags(paramTypes) or returnType.flags), TyCallable {

    override fun abilities(): Set<Ability> = emptySet()

    override fun toString(): String = tyToString(this)

    override fun innerFoldWith(folder: TypeFolder): Ty {
        return TyLambda(
            paramTypes.map { it.foldWith(folder) },
            returnType.foldWith(folder),
        )
    }

    override fun innerVisitWith(visitor: TypeVisitor): Boolean =
        paramTypes.any { it.visitWith(visitor) } || returnType.visitWith(visitor)

    companion object {
        fun unknown(numParams: Int): TyLambda {
            return TyLambda(generateSequence { TyUnknown }.take(numParams).toList(), TyUnknown)
        }
    }
}
