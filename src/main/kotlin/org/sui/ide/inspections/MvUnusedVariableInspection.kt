package org.sui.ide.inspections

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.util.descendantsOfType
import org.sui.ide.inspections.fixes.RemoveParameterFix
import org.sui.ide.inspections.fixes.RenameFix
import org.sui.lang.core.psi.*
import org.sui.lang.core.psi.ext.isMsl
import org.sui.lang.core.psi.ext.owner

class MvUnusedVariableInspection : MvLocalInspectionTool() {
    override fun buildMvVisitor(holder: ProblemsHolder, isOnTheFly: Boolean) =
        object : MvVisitor() {
            override fun visitLetStmt(o: MvLetStmt) {
                val bindings = o.pat?.descendantsOfType<MvPatBinding>().orEmpty()
                for (binding in bindings) {
                    checkUnused(binding, "Unused variable")
                }
            }

            override fun visitFunctionParameter(o: MvFunctionParameter) {
                val functionLike = o.containingFunctionLike ?: return
                if (functionLike.anyBlock == null) return

                val binding = o.patBinding
                checkUnused(binding, "Unused function parameter")
            }

            private fun checkUnused(binding: MvPatBinding, description: String) {
                if (binding.isMsl()) return

                val bindingName = binding.name
                if (bindingName.startsWith("_")) return

                val references = binding.searchReferences()
                    // filter out #[test] attributes
                    .filter { it.element !is MvAttrItem }
                if (references.none()) {
                    val fixes = when (binding.owner) {
                        is MvFunctionParameter -> arrayOf(
                            RenameFix(binding, "_$bindingName"),
                            RemoveParameterFix(binding, bindingName)
                        )
                        else -> arrayOf(RenameFix(binding, "_$bindingName"))
                    }
                    holder.registerProblem(
                        binding,
                        description,
                        ProblemHighlightType.LIKE_UNUSED_SYMBOL,
                        *fixes
                    )
                }
            }
        }
}
