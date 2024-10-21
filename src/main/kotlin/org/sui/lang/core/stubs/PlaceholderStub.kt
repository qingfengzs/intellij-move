package org.sui.lang.core.stubs

import com.intellij.lang.ASTNode
import com.intellij.psi.stubs.*
import org.sui.lang.core.psi.MvElement

open class PlaceholderStub(parent: StubElement<*>?, elementType: IStubElementType<*, *>):
    StubBase<MvElement>(parent, elementType) {

    open class Type<PsiT: MvElement>(
        debugName: String,
        private val psiContructor: (PlaceholderStub, IStubElementType<*, *>) -> PsiT,
    ): MvStubElementType<PlaceholderStub, PsiT>(debugName) {

        override fun shouldCreateStub(node: ASTNode): Boolean = createStubIfParentIsStub(node)

        override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?) =
            PlaceholderStub(parentStub, this)

        override fun serialize(stub: PlaceholderStub, dataStream: StubOutputStream) {}

        override fun createPsi(stub: PlaceholderStub) = psiContructor(stub, this)

        override fun createStub(psi: PsiT, parentStub: StubElement<*>?) =
            PlaceholderStub(parentStub, this)
    }
}
