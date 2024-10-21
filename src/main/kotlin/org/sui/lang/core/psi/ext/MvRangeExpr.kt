package org.sui.lang.core.psi.ext

import org.sui.lang.core.psi.MvExpr
import org.sui.lang.core.psi.MvRangeExpr

val MvRangeExpr.fromExpr: MvExpr get() = exprList.first()
val MvRangeExpr.toExpr: MvExpr? get() = exprList.drop(1).firstOrNull()