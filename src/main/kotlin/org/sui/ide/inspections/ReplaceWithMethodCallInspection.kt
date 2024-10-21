package org.sui.ide.inspections

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import org.sui.ide.inspections.fixes.ReplaceWithMethodCallFix
import org.sui.lang.core.psi.*
import org.sui.lang.core.psi.ext.isMsl
import org.sui.lang.core.psi.ext.itemModule
import org.sui.lang.core.psi.ext.valueArguments
import org.sui.lang.core.types.infer.deepFoldTyTypeParameterWith
import org.sui.lang.core.types.infer.inference
import org.sui.lang.core.types.ty.TyInfer
import org.sui.lang.core.types.ty.TyReference.Companion.isCompatibleWithAutoborrow
import org.sui.lang.core.types.ty.hasTyUnknown
import org.sui.lang.moveProject

class ReplaceWithMethodCallInspection: MvLocalInspectionTool() {
    override fun buildMvVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): MvVisitor {
        return object: MvVisitor() {
            override fun visitCallExpr(callExpr: MvCallExpr) {
                val function = callExpr.path.reference?.resolveFollowingAliases() as? MvFunction ?: return
                val msl = callExpr.isMsl()
                val inference = callExpr.inference(msl) ?: return

                val firstArgExpr = callExpr.valueArguments.firstOrNull()?.expr ?: return
                val firstArgExprTy = inference.getExprType(firstArgExpr)
                if (firstArgExprTy.hasTyUnknown) return

                val moveProject = callExpr.moveProject ?: return
                val methodModule = function.module ?: return
                val itemModule = firstArgExprTy.itemModule(moveProject) ?: return
                if (methodModule != itemModule) return

                val selfTy = function.selfParamTy(msl)
                    ?.deepFoldTyTypeParameterWith { TyInfer.TyVar(it) } ?: return
                if (selfTy.hasTyUnknown) return

                if (isCompatibleWithAutoborrow(firstArgExprTy, selfTy, msl)) {
                    // can be converted
                    holder.registerProblem(
                        callExpr,
                        "Can be replaced with method call",
                        ProblemHighlightType.WEAK_WARNING,
                        ReplaceWithMethodCallFix(callExpr)
                    )
                }
            }
        }
    }

}