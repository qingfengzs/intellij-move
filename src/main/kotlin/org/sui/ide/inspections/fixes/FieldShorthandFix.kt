package org.sui.ide.inspections.fixes

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import org.sui.ide.inspections.DiagnosticFix
import org.sui.lang.core.psi.*

sealed class FieldShorthandFix<T : MvElement>(field: T) : DiagnosticFix<T>(field) {

    class StructLit(field: MvStructLitField) : FieldShorthandFix<MvStructLitField>(field) {
        override fun getText(): String = "Use initialization shorthand"

        override fun invoke(project: Project, file: PsiFile, element: MvStructLitField) {
            element.colon?.delete()
            element.expr?.delete()
        }
    }

    class StructPat(fieldPatFull: MvPatFieldFull) : FieldShorthandFix<MvPatFieldFull>(fieldPatFull) {
        override fun getText(): String = "Use pattern shorthand"

        override fun invoke(project: Project, file: PsiFile, element: MvPatFieldFull) {
            val fieldName = element.referenceName
            val newBindingPat = project.psiFactory.bindingPat(fieldName)
            element.replace(newBindingPat)
//            element.replace(project.psiFactory.bindingPat(fieldIdent.text))
//            element.fieldPatBinding?.delete()
//            fieldIdent.replace(project.psiFactory.bindingPat(fieldIdent.text))
        }
    }

    class Schema(field: MvSchemaLitField) : FieldShorthandFix<MvSchemaLitField>(field) {
        override fun getText(): String = "Use initialization shorthand"

        override fun invoke(project: Project, file: PsiFile, element: MvSchemaLitField) {
            element.colon?.delete()
            element.expr?.delete()
        }
    }
}
