package org.sui.lang.core.psi.ext

import com.intellij.ide.projectView.PresentationData
import com.intellij.lang.ASTNode
import com.intellij.navigation.ItemPresentation
import org.sui.ide.MoveIcons
import org.sui.lang.core.psi.MvBlockFields
import org.sui.lang.core.psi.MvNamedFieldDecl
import org.sui.lang.core.psi.impl.MvMandatoryNameIdentifierOwnerImpl
import javax.swing.Icon

val MvNamedFieldDecl.blockFields: MvBlockFields? get() = parent as? MvBlockFields

val MvNamedFieldDecl.fieldOwner: MvFieldsOwner get() = blockFields?.parent as MvFieldsOwner

abstract class MvNamedFieldDeclMixin(node: ASTNode) : MvMandatoryNameIdentifierOwnerImpl(node),
                                                      MvNamedFieldDecl {

    override fun getIcon(flags: Int): Icon = MoveIcons.STRUCT_FIELD

    override fun getPresentation(): ItemPresentation {
        val type = this.type?.let { ": ${it.text}" } ?: ""
        return PresentationData(
            "${this.name}$type",
            this.locationString(true),
            MoveIcons.STRUCT_FIELD,
            null
        )
    }
}