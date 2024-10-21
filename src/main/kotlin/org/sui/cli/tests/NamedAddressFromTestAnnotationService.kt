package org.sui.cli.tests

import org.sui.cli.MoveProject
import org.sui.lang.core.types.Address.Named

interface NamedAddressFromTestAnnotationService {
    fun getNamedAddress(moveProject: MoveProject, name: String): Named?
}

class NamedAddressServiceImpl: NamedAddressFromTestAnnotationService {
    override fun getNamedAddress(moveProject: MoveProject, name: String): Named? = null
}


class NamedAddressServiceTestImpl: NamedAddressFromTestAnnotationService {
    val namedAddresses: MutableMap<String, String> = mutableMapOf()

    override fun getNamedAddress(moveProject: MoveProject, name: String): Named? {
        val value = namedAddresses[name] ?: return null
        return Named(name, value)
    }
}