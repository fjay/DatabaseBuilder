package org.team4u.dbb.model

import java.util.*

class Index(
        val type: Index.Type,
        val name: String? = null,
        val columns: List<String> = ArrayList()
) {
    enum class Type {
        PK, INDEX, UNIQUE
    }
}

