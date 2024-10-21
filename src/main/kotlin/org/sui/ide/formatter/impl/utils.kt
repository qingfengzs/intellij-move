package org.sui.ide.formatter.impl

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.TokenType
import com.intellij.psi.tree.TokenSet.orSet
import org.sui.lang.MoveFile
import org.sui.lang.MvElementTypes.*
import org.sui.lang.core.psi.*
import org.sui.openapiext.document
import org.sui.openapiext.getOffsetPosition
import com.intellij.psi.tree.TokenSet.create as ts


//val BINARY_OPS = ts(
//    PLUS, MINUS, MUL, DIV, MODULO,
//    OR, AND, OR_OR, AND_AND,
//    EQ, EQ_EQ, NOT_EQ,
//)
val ONE_LINE_ITEMS = ts(USE_STMT, CONST)

val PAREN_DELIMITED_BLOCKS = ts(
    PARENS_EXPR, PAT_TUPLE, TUPLE_TYPE, TUPLE_LIT_EXPR,
    CONDITION, MATCH_ARGUMENT,
    FUNCTION_PARAMETER_LIST, VALUE_ARGUMENT_LIST, ATTR_ITEM_LIST,
    ITEM_SPEC_FUNCTION_PARAMETER_LIST
)
val PAREN_LISTS = orSet(PAREN_DELIMITED_BLOCKS /*ts(PAT_TUPLE_STRUCT)*/)

val ANGLE_DELIMITED_BLOCKS = ts(TYPE_PARAMETER_LIST, TYPE_ARGUMENT_LIST, ITEM_SPEC_TYPE_PARAMETER_LIST)
val ANGLE_LISTS = orSet(ANGLE_DELIMITED_BLOCKS)

val BRACK_DELIMITED_BLOCKS = ts(VECTOR_LIT_ITEMS)
val BRACK_LISTS = orSet(BRACK_DELIMITED_BLOCKS, ts(INDEX_EXPR))

val STRUCT_LITERAL_BLOCKS = ts(STRUCT_LIT_FIELDS_BLOCK)

//val STRUCT_LITERAL_BLOCKS = ts(STRUCT_PAT_FIELDS_BLOCK, STRUCT_LIT_FIELDS_BLOCK)
val DEF_BLOCKS = ts(
    /*SCRIPT_BLOCK, */ADDRESS_BLOCK, /*MODULE_BLOCK, */CODE_BLOCK,
    MODULE_SPEC_BLOCK, SPEC_CODE_BLOCK,
    BLOCK_FIELDS, SCHEMA_FIELDS_BLOCK
)

val BLOCK_LIKE = orSet(STRUCT_LITERAL_BLOCKS, DEF_BLOCKS, ts(ENUM_BODY, MATCH_BODY))
val BRACE_LISTS = ts(USE_GROUP)
val BRACE_DELIMITED_BLOCKS = orSet(BLOCK_LIKE, BRACE_LISTS)

val DELIMITED_BLOCKS = orSet(
    PAREN_DELIMITED_BLOCKS, ANGLE_DELIMITED_BLOCKS, BRACK_DELIMITED_BLOCKS,
    BLOCK_LIKE,
    ts(USE_GROUP)
)
val FLAT_BRACE_BLOCKS = ts(SCRIPT, MODULE, PAT_STRUCT)

fun ASTNode?.isWhitespaceOrEmpty() = this == null || textLength == 0 || elementType == TokenType.WHITE_SPACE

val PsiElement.isSpecStmt: Boolean
    get() = this is MvSchemaFieldStmt
            || this is MvGlobalVariableStmt
            || this is MvPragmaSpecStmt
            || this is MvUpdateSpecStmt
            || this is MvIncludeStmt
            || this is MvApplySchemaStmt

val PsiElement.isTopLevelItem: Boolean
    get() = (this is MvModule || this is MvAddressDef || this is MvScript || this is MvModuleSpec)
            && parent is MoveFile

val PsiElement.isModuleItem: Boolean
    get() = this is MvFunction || this is MvConst || this is MvStruct || this is MvUseStmt
            || this is MvSpecFunction || this is MvSchema

val PsiElement.isDeclarationItem: Boolean
    get() = (this is MvModule && parent is MvAddressBlock) || this.isModuleItem

val PsiElement.isStmt: Boolean
    get() = this is MvStmt && parent is MvCodeBlock

val PsiElement.isStmtOrExpr: Boolean
    get() = this is MvStmt || this is MvExpr && parent is MvCodeBlock

fun ASTNode.isDelimiterOfCurrentBlock(parent: ASTNode?): Boolean {
    if (parent == null) return false
    val parentType = parent.elementType
    return when (elementType) {
        L_BRACE, R_BRACE -> parentType in BLOCK_LIKE || parentType == USE_GROUP
        L_BRACK, R_BRACK -> parentType in BRACK_DELIMITED_BLOCKS
        L_PAREN, R_PAREN -> parentType in PAREN_DELIMITED_BLOCKS
        LT, GT -> parentType in ANGLE_DELIMITED_BLOCKS
        else -> false
    }
}

data class PsiLocation(val line: Int, val column: Int) {
    override fun toString(): String {
        return "line=$line, column=$column"
    }
}

val PsiElement.fileWithLocation: Pair<PsiFile, PsiLocation>?
    get() {
        val elementOffset = this.textOffset
        val file = this.containingFile ?: return null
        val location = file.document?.getOffsetPosition(elementOffset) ?: return null
        return file to PsiLocation(location.first, location.second)
    }

/// Returns null if element does not belong to any file
val PsiElement.location: PsiLocation?
    get() {
        val elementOffset = this.textOffset
        val file = this.containingFile ?: return null
        val location = file.document?.getOffsetPosition(elementOffset) ?: return null
        return PsiLocation(location.first, location.second)
    }

fun PsiFile.elementLocation(psiElement: PsiElement): PsiLocation {
    val (line, col) = document?.getOffsetPosition(psiElement.textOffset) ?: (-1 to -1)
    return PsiLocation(line, col)
}


val ASTNode.isFlatBraceBlock: Boolean
    get() = elementType in FLAT_BRACE_BLOCKS

/**
 * A flat block is a Rust PSI element which does not denote separate PSI
 * element for its _block_ part (e.g. `{...}`), for example [MODULE].
 */
val ASTNode.isFlatBlock: Boolean
    get() = isFlatBraceBlock
//            || elementType == PAT_TUPLE_STRUCT

fun ASTNode.isBlockDelim(parent: ASTNode?): Boolean {
    if (parent == null) return false
    val parentType = parent.elementType
    return when (elementType) {
        L_BRACE, R_BRACE -> parentType in BRACE_DELIMITED_BLOCKS || parent.isFlatBraceBlock
        L_BRACK, R_BRACK -> parentType in BRACK_LISTS
        L_PAREN, R_PAREN -> parentType in PAREN_LISTS /*|| parentType == PAT_TUPLE_STRUCT*/
        LT, GT -> parentType in ANGLE_LISTS
        OR -> parentType == FUNCTION_PARAMETER_LIST && parent.treeParent?.elementType == LAMBDA_EXPR
        else -> false
    }
}

//class CommaList(
//    val list: IElementType,
//    val openingBrace: IElementType,
//    val closingBrace: IElementType,
//    val isElement: (PsiElement) -> Boolean
//) {
//    val needsSpaceBeforeClosingBrace: Boolean get() = closingBrace == R_BRACE // && list != USE_GROUP
//
//    override fun toString(): String = "CommaList($list)"
//
//    companion object {
//        fun forElement(elementType: IElementType): CommaList? {
//            return ALL.find { it.list == elementType }
//        }
//
//        private val ALL = listOf(
//            CommaList(BLOCK_FIELDS, LBRACE, RBRACE) { it.elementType == NAMED_FIELD_DECL },
//            CommaList(STRUCT_LITERAL_BODY, LBRACE, RBRACE) { it.elementType == STRUCT_LIT_FIELD },
//            CommaList(ENUM_BODY, LBRACE, RBRACE) { it.elementType == ENUM_VARIANT },
//            CommaList(USE_GROUP, LBRACE, RBRACE) { it.elementType == USE_SPECK },
//
//            CommaList(TUPLE_FIELDS, LPAREN, RPAREN) { it.elementType == TUPLE_FIELD_DECL },
//            CommaList(VALUE_PARAMETER_LIST, LPAREN, RPAREN) { it.elementType == VALUE_PARAMETER },
//            CommaList(VALUE_ARGUMENT_LIST, LPAREN, RPAREN) { it is RsExpr }
//        )
//    }
//}
