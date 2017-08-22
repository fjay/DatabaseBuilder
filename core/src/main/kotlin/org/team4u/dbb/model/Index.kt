package org.team4u.dbb.model

import java.util.*

class Index(
        val type: IndexType,
        val name: String? = null,
        val columns: List<String> = ArrayList()
) {
}

