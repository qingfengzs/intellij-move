package org.move.ide.inspections

import org.move.ide.inspections.FieldInitShorthandInspection
import org.move.ide.inspections.lints.MoveConstNamingInspection
import org.move.ide.inspections.lints.MoveLocalBindingNamingInspection
import org.move.ide.inspections.lints.MoveStructNamingInspection
import org.move.utils.tests.annotation.InspectionsTestCase

class MoveConstNamingInspectionTest: InspectionsTestCase(MoveConstNamingInspection::class) {
    fun `test constants`() = checkByText("""
module 0x1::M {
    const CONST_OK: u8 = 1;
    const <warning descr="Invalid constant name `const_foo`. Constant names must start with 'A'..'Z'">const_foo</warning>: u8 = 2;
}
    """)
}

class MoveStructNamingInspectionTest: InspectionsTestCase(MoveStructNamingInspection::class) {
    fun `test structs`() = checkByText("""
module 0x1::M {
    struct S {}
    struct <warning descr="Invalid struct name `collection`. Struct names must start with 'A'..'Z'">collection</warning> {}
}
    """)
}

class MoveLocalBindingNamingInspectionTest: InspectionsTestCase(MoveLocalBindingNamingInspection::class) {
    fun `test function parameter`() = checkByText("""
module 0x1::M {
    fun m(<warning descr="Invalid local variable name `COLL`. Local variable names must start with 'a'..'z'">COLL</warning>: u8) {}
}
    """)

    fun `test let variables`() = checkByText("""
module 0x1::M {
    fun m() {
        let <warning descr="Invalid local variable name `COLL`. Local variable names must start with 'a'..'z'">COLL</warning> = 1;
    }
}
    """)

    fun `test let variables inside pattern`() = checkByText("""
module 0x1::M {
    struct S { s: u8 }
    fun m() {
        let S { s: <warning descr="Invalid local variable name `COLL`. Local variable names must start with 'a'..'z'">COLL</warning> } = S { s: 1 };
    }
}
    """)
}