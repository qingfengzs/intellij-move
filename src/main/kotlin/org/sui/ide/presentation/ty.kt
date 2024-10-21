package org.sui.ide.presentation

import org.sui.lang.core.psi.MvElement
import org.sui.lang.core.psi.MvModule
import org.sui.lang.core.psi.containingModule
import org.sui.lang.core.types.ty.*

// null -> builtin module
fun Ty.declaringModule(): MvModule? = when (this) {
    is TyReference -> this.referenced.declaringModule()
    is TyAdt -> this.item.containingModule
    else -> null
}

fun Ty.nameNoArgs(): String {
    return this.name().replace(Regex("<.*>"), "")
}

fun Ty.name(): String {
    return text(fq = false)
}

fun Ty.fullnameNoArgs(): String {
    return this.fullname().replace(Regex("<.*>"), "")
}

fun Ty.fullname(): String {
    return text(fq = true)
}

fun Ty.typeLabel(relativeTo: MvElement): String {
    val typeModule = this.declaringModule()
    if (typeModule != null && typeModule != relativeTo.containingModule) {
        return this.fullname()
    } else {
        return this.name()
    }
}

fun Ty.hintText(): String =
    render(this, level = 3, unknown = "?", tyVar = { "?" })

fun Ty.text(fq: Boolean = false): String =
    render(
        this,
        level = 3,
        fq = fq
    )

fun Ty.expectedTyText(): String {
    return render(
        this,
        level = 3,
        typeParam = {
            val name = it.origin.name ?: "_"
            val abilities =
                it.abilities()
                    .toList().sorted()
                    .joinToString(", ", "(", ")") { a -> a.label() }
            "$name$abilities"
        },
        tyVar = {
            val name = it.origin?.name ?: "_"
            val abilities =
                it.abilities()
                    .toList().sorted()
                    .joinToString(", ", "(", ")") { a -> a.label() }
            "?$name$abilities"
        },
        fq = true
    )
}

val Ty.insertionSafeText: String
    get() = render(
        this,
        level = Int.MAX_VALUE,
        unknown = "_",
        anonymous = "_",
        integer = "_"
    )

fun tyToString(ty: Ty) = render(ty, Int.MAX_VALUE)

private fun render(
    ty: Ty,
    level: Int,
    unknown: String = "<unknown>",
    anonymous: String = "<anonymous>",
    integer: String = "integer",
    typeParam: (TyTypeParameter) -> String = { it.name ?: anonymous },
    tyVar: (TyInfer.TyVar) -> String = { "?${it.origin?.name ?: "_"}" },
    fq: Boolean = false
): String {
    check(level >= 0)
    if (ty is TyUnknown) return unknown
    if (ty is TyPrimitive) {
        return when (ty) {
            is TyBool -> "bool"
            is TyAddress -> "address"
            is TySigner -> "signer"
            is TyUnit -> "()"
            is TyNum -> "num"
            is TySpecBv -> "bv"
            is TyInteger -> {
                if (ty.kind == TyInteger.DEFAULT_KIND) {
                    integer
                } else {
                    ty.kind.toString()
                }
            }
            is TyNever -> "<never>"
            else -> error("unreachable")
        }
    }

    if (level == 0) return "_"

    val r = { subTy: Ty -> render(subTy, level - 1, unknown, anonymous, integer, typeParam, tyVar, fq) }

    return when (ty) {
        is TyFunction -> {
            val params = ty.paramTypes.joinToString(", ", "fn(", ")", transform = r)
            var s = if (ty.returnType is TyUnit) params else "$params -> ${r(ty.returnType)}"
            if (ty.acquiresTypes.isNotEmpty()) {
                s += ty.acquiresTypes.joinToString(", ", " acquires ", transform = r)
            }
            s
        }
        is TyTuple -> ty.types.joinToString(", ", "(", ")", transform = r)
        is TyVector -> "vector<${r(ty.item)}>"
        is TyRange -> "range<${r(ty.item)}>"
        is TyReference -> {
            val prefix = if (ty.mutability.isMut) "&mut " else "&"
            "$prefix${r(ty.referenced)}"
        }
        is TyTypeParameter -> typeParam(ty)
//        is TyStruct -> {
//            val name = if (fq) ty.item.qualName?.editorText() ?: anonymous else (ty.item.name ?: anonymous)
//            val args =
//                if (ty.typeArguments.isEmpty()) ""
//                else ty.typeArguments.joinToString(", ", "<", ">", transform = r)
//            name + args
//        }
        is TyAdt -> {
            val name = if (fq) ty.item.qualName?.editorText() ?: anonymous else (ty.item.name ?: anonymous)
            val args =
                if (ty.typeArguments.isEmpty()) ""
                else ty.typeArguments.joinToString(", ", "<", ">", transform = r)
            name + args
        }
        is TyInfer -> when (ty) {
            is TyInfer.TyVar -> tyVar(ty)
            is TyInfer.IntVar -> integer
        }
        is TyLambda -> {
            val params = ty.paramTypes.joinToString(",", "|", "|", transform = r)
            val retType = if (ty.returnType is TyUnit)
                "()"
            else
                r(ty.returnType)
            "$params -> $retType"
        }
        is TySchema -> {
            val name = if (fq) ty.item.qualName?.editorText() ?: anonymous else (ty.item.name ?: anonymous)
            val args =
                if (ty.typeArguments.isEmpty()) ""
                else ty.typeArguments.joinToString(", ", "<", ">", transform = r)
            name + args
        }
        else -> error("unimplemented for type ${ty.javaClass.name}")
    }
}
