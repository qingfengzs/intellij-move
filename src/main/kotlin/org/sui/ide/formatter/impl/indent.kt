package org.sui.ide.formatter.impl

import com.intellij.formatting.Indent
import com.intellij.lang.ASTNode
import org.sui.ide.formatter.MoveFmtBlock
import org.sui.ide.formatter.MvFmtContext
import org.sui.lang.MvElementTypes.*
import org.sui.lang.core.psi.*

fun MoveFmtBlock.computeIndent(child: ASTNode, childCtx: MvFmtContext): Indent? {
    val parentNode = node
    val parentPsi = node.psi
    val parentType = node.elementType
    return when {
        // do not indent contents of an address block
        // address {
        // module M {
        // }
        // }
        parentType == ADDRESS_BLOCK -> Indent.getNoneIndent()

        // indent inline block in else block
        // if (true)
        // else
        //     2 + 2;
        parentType == ELSE_BLOCK
                && child.elementType == INLINE_BLOCK -> Indent.getContinuationIndent()

        // do not indent else block
        child.elementType == ELSE_BLOCK -> Indent.getNoneIndent()

        // indent every child of the block except for braces
        // module M {
        //    struct S {}
        // }
        parentType in DELIMITED_BLOCKS -> getIndentIfNotDelim(child, parentNode)

        // Indent flat block contents, excluding closing brace
        node.isFlatBlock ->
            if (childCtx.metLBrace) {
                getIndentIfNotDelim(child, node)
            } else {
                Indent.getNoneIndent()
            }

//        //     let a =
//        //     92;
//        // =>
//        //     let a =
//        //         92;
        parentType == INITIALIZER -> Indent.getNormalIndent()

        // in expressions, we need to indent any part of it except for the first one
        // - binary expressions
        // 10000
        //     + 2
        //     - 3
        // - field chain calls
        // get_s()
        //     .myfield
        //     .myotherfield
        parentPsi is MvExpr -> Indent.getContinuationWithoutFirstIndent()
        parentPsi is MvMatchBody -> Indent.getNoneIndent()
        parentPsi is MvMatchArm -> Indent.getNormalIndent()
        parentPsi is MvEnumBody -> Indent.getNoneIndent()
        parentPsi is MvEnumVariant -> Indent.getContinuationIndent()
        // same thing as previous one, but for spec statements
        parentPsi.isSpecStmt -> Indent.getContinuationWithoutFirstIndent()

        else -> Indent.getNoneIndent()
    }
}

//fun getNormalIndentIfNotCurrentBlockDelimiter(child: ASTNode, parent: ASTNode): Indent =
//    if (child.isDelimiterOfCurrentBlock(parent)) {
//        Indent.getNoneIndent()
//    } else {
//        Indent.getNormalIndent()
//    }

private fun getIndentIfNotDelim(child: ASTNode, parent: ASTNode): Indent =
    if (child.isBlockDelim(parent)) {
        Indent.getNoneIndent()
    } else {
        Indent.getNormalIndent()
    }
