package org.sui.lang.core.psi.ext

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import org.sui.ide.MoveIcons
import org.sui.lang.core.psi.MvMixUseSpeck
import org.sui.lang.core.psi.impl.MvNamedElementImpl
import javax.swing.Icon

abstract class MvMixUseSpeckMixin(node: ASTNode) : MvNamedElementImpl(node),
    MvMixUseSpeck {
    override val nameElement: PsiElement?
        get() = this.addressRef.namedAddress?.identifier

    override fun getIcon(flags: Int): Icon = MoveIcons.MODULE

//    override fun getReference(): MvReference {
//        return MvModuleReferenceImpl(moduleRef)
//    }

}
