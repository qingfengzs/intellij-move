package org.move.ide.inspections

import org.move.utils.tests.annotation.InspectionTestBase

class MvUnusedImportInspectionTest: InspectionTestBase(MvUnusedImportInspection::class) {
    fun `test no error`() = checkWarnings("""
module 0x1::M {
    struct MyItem {}
    struct MyItem2 {}
    public fun call() {}
}        
module 0x1::M2 {
    use 0x1::M::MyItem;
    use 0x1::M::MyItem2;
    use 0x1::M::call;
    fun main(arg: MyItem2) {
        let a: MyItem = call();
    }
}        
    """)

    fun `test error unused item import`() = checkWarnings("""
module 0x1::M {
    struct MyItem {}
    struct MyItem2 {}
    public fun call() {}
}        
module 0x1::M2 {
    <warning descr="Unused use item">use 0x1::M::MyItem;</warning>
    fun main() {}
}
    """)

    fun `test error unused module import`() = checkWarnings("""
module 0x1::M {
    struct MyItem {}
    struct MyItem2 {}
    public fun call() {}
}        
module 0x1::M2 {
    <warning descr="Unused use item">use 0x1::M;</warning>
    fun main() {}
}
    """)

    fun `test unused item in use group`() = checkWarnings("""
module 0x1::M {
    struct MyItem {}
    struct MyItem2 {}
    public fun call() {}
}        
module 0x1::M2 {
    use 0x1::M::{MyItem, <warning descr="Unused use item">MyItem2</warning>};
    fun main(a: MyItem) {}
}
    """)

    fun `test no error if module imported and used as fq`() = checkWarnings("""
module 0x1::M {
    public fun call() {}
}
module 0x1::M2 {
    use 0x1::M;
    fun main() {
        M::call();
    }
}
    """)

    fun `test no unused import on Self`() = checkWarnings("""
module 0x1::M {
    struct S {}
    public fun call() {}
}        
module 0x1::Main {
    use 0x1::M::{Self, S};
    
    fun main(a: S) {
        M::call();
    }
}
    """)

    fun `test unused imports if unresolved module`() = checkWarnings("""
module 0x1::Main {
    <warning descr="Unused use item">use 0x1::M1;</warning>
}
    """)

    fun `test no unused import if unresolved module but used`() = checkWarnings("""
module 0x1::Main {
    use 0x1::M;
    fun call() {
        M::call();
    }
}        
    """)

    fun `test unused imports if unresolved item`() = checkWarnings("""
module 0x1::Main {
    <warning descr="Unused use item">use 0x1::M1::call;</warning>
}
    """)

    fun `test no unused import if unresolved item but used`() = checkWarnings("""
module 0x1::Main {
    use 0x1::M::call;
    fun call() {
        call();
    }
}        
    """)

    fun `test duplicate import`() = checkWarnings("""
module 0x1::M {
    public fun call() {}
}
module 0x1::M2 {
    use 0x1::M::call;
    <warning descr="Unused use item">use 0x1::M::call;</warning>

    fun main() {
        call();
    }
}
    """)

    fun `test duplicate import with item group`() = checkWarnings("""
module 0x1::M {
    struct S {}
    public fun call() {}
}
module 0x1::M2 {
    use 0x1::M::{S, call};
    <warning descr="Unused use item">use 0x1::M::call;</warning>

    fun main(s: S) {
        call();
    }
}
    """)

    fun `test no unused import for type with the same name as module`() = checkWarnings("""
    module 0x1::Coin {
        struct Coin {}
        public fun get_coin(): Coin {}
    }    
    module 0x1::Main {
        use 0x1::Coin::{Self, Coin};
        
        fun call(): Coin {
            Coin::get_coin()
        }
    }
    """)

    fun `test unused Self import`() = checkWarnings("""
    module 0x1::Coin {
        struct Coin {}
        public fun get_coin(): Coin {}
    }    
    module 0x1::Main {
        use 0x1::Coin;
        <warning descr="Unused use item">use 0x1::Coin::Self;</warning>
        
        fun call(): Coin {
            Coin::get_coin()
        }
    }
    """)

    fun `test unused Self in group`() = checkWarnings("""
    module 0x1::Coin {
        struct Coin {}
        public fun get_coin(): Coin {}
    }    
    module 0x1::Main {
        use 0x1::Coin;
        use 0x1::Coin::{<warning descr="Unused use item">Self</warning>, Coin};
        
        fun call(): Coin {
            Coin::get_coin()
        }
    }
    """)

    fun `test empty item group`() = checkWarnings("""
    module 0x1::Coin {
        struct C {}
    }    
    module 0x1::Main {
        <warning descr="Unused use item">use 0x1::Coin::{};</warning>
    }
    """)
}