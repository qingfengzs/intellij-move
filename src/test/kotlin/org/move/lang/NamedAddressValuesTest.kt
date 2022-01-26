package org.move.lang

import org.move.cli.moveProjects
import org.move.lang.core.psi.MvNamedAddress
import org.move.utils.tests.FileTreeBuilder
import org.move.utils.tests.MvProjectTestCase
import org.move.utils.tests.base.findElementAndDataInEditor

class NamedAddressValuesTest : MvProjectTestCase() {
    fun `test named address`() = checkByFileTree {
        moveToml("""
        [addresses]
        Std = "0x1"
        """)
        sources {
            move("main.move", """
            module Std::Module {}
                   //^ 0x1
            """)
        }
    }

    fun `test placeholder address`() = checkByFileTree {
        moveToml("""
        [addresses]
        Std = "_"
        """)
        sources {
            move("main.move", """
            module Std::Module {}
                   //^ _
            """)
        }
    }

    fun `test dependency address`() = checkByFileTree {
        moveToml("""
        [dependencies]
        Stdlib = { local = "./stdlib" }    
        """)
        dir("stdlib") {
            moveToml("""
            [addresses]
            Std = "0x1"    
            """)
        }
        sources {
            move("main.move", """
            module Std::Module {}
                   //^ 0x1
            """)
        }
    }

    fun `test subst address value`() = checkByFileTree {
        moveToml(
            """
    [package]
    name = "rmrk"
    version = "0.0.0"
    
    [dependencies]
    Stdlib = { local = "./stdlib", addr_subst = { "Std" = "0xC0FFEE" }}
        """
        )
        sources {
            move(
                "main.move", """
        module Std::Module {}       
             //^ 0xC0FFEE                  
            """
            )
        }
        dir("stdlib") {
            moveToml(
                """
        [package]
        name = "Stdlib"
        version = "0.0.0"
        [addresses]
        Std = "_"        
            """
            )
        }
    }

    fun `test cannot subst non placeholder`() = checkByFileTree {
        moveToml(
            """
    [package]
    name = "rmrk"
    version = "0.0.0"
    
    [dependencies]
    Stdlib = { local = "./stdlib", addr_subst = { "Std" = "0xC0FFEE" }}
        """
        )
        sources {
            move(
                "main.move", """
        module Std::Module {}       
             //^ 0x1                 
            """
            )
        }
        dir("stdlib") {
            moveToml(
                """
        [package]
        name = "Stdlib"
        version = "0.0.0"
        [addresses]
        Std = "0x1"        
            """
            )
        }
    }

    fun `test named address of transitive dependency with subst`() = checkByFileTree {
        moveToml(
            """
        [dependencies]
        PontStdlib = { local = "./pont-stdlib", addr_subst = { "Std" = "0x2" }}
        """
        )
        sources {
            move(
                "main.move", """
            module 0x1::M {
                use Std::Reflect;
                    //^ 0x2
            }     
            """
            )
        }
        dir("pont-stdlib", {
            moveToml(
                """
            [dependencies]
            MoveStdlib = { local = "./move-stdlib" }    
            """
            )
            dir("move-stdlib", {
                moveToml(
                    """
                [addresses]
                Std = "_"
                """
                )
            })
        })
    }

    private fun checkByFileTree(
        fileTree: FileTreeBuilder.() -> Unit,
    ) {
        val testProject = testProjectFromFileTree(fileTree)
        myFixture.configureFromFileWithCaret(testProject)

        val (address, data) = myFixture.findElementAndDataInEditor<MvNamedAddress>()
        val expectedValue = data.trim()

        val moveProject = project.moveProjects.findProjectForPsiElement(address)!!
        val actualValue = moveProject.getAddressValue(address.referenceName)

        check(actualValue == expectedValue) {
            "Value mismatch. Expected $expectedValue, found: $actualValue"
        }
    }
}
