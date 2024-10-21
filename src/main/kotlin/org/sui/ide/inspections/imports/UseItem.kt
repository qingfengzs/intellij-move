package org.sui.ide.inspections.imports

import org.sui.cli.settings.debugError
import org.sui.ide.inspections.imports.UseItemType.*
import org.sui.lang.core.psi.MvUseSpeck
import org.sui.lang.core.psi.MvUseStmt
import org.sui.lang.core.psi.NamedItemScope
import org.sui.lang.core.psi.ext.MvItemsOwner
import org.sui.lang.core.resolve2.PathKind
import org.sui.lang.core.resolve2.pathKind
import org.sui.lang.core.resolve2.util.forEachLeafSpeck

enum class UseItemType {
    MODULE, SELF_MODULE, ITEM;
}

data class UseItem(
    val useSpeck: MvUseSpeck,
    val nameOrAlias: String,
    val type: UseItemType,
    val scope: NamedItemScope
)

val MvItemsOwner.useItems: List<UseItem> get() = this.useStmtList.flatMap { it.useItems }

val MvUseStmt.useItems: List<UseItem>
    get() {
        val items = mutableListOf<UseItem>()
        val stmtItemScope = this.usageScope
        this.forEachLeafSpeck { speckPath, useAlias ->
            val useSpeck = speckPath.parent as MvUseSpeck
            val nameOrAlias = useAlias?.name ?: speckPath.referenceName ?: return@forEachLeafSpeck false
            val pathKind = speckPath.pathKind()
            when (pathKind) {
                is PathKind.QualifiedPath.Module ->
                    items.add(UseItem(useSpeck, nameOrAlias, MODULE, stmtItemScope))
                is PathKind.QualifiedPath.ModuleItem -> {
                    debugError("not reachable, must be a bug")
                    return@forEachLeafSpeck false
                }
                is PathKind.QualifiedPath -> {
                    if (pathKind.path.referenceName == "Self") {
                        val moduleName =
                            useAlias?.name ?: pathKind.qualifier.referenceName ?: return@forEachLeafSpeck false
                        items.add(UseItem(useSpeck, moduleName, SELF_MODULE, stmtItemScope))
                    } else {
                        items.add(UseItem(useSpeck, nameOrAlias, ITEM, stmtItemScope))
                    }
                }
                else -> return@forEachLeafSpeck false
            }
            false
        }

        return items
    }
