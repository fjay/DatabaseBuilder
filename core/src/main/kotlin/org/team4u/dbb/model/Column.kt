package org.team4u.dbb.model

import org.nutz.dao.entity.annotation.ColType

data class Column(
        val name: String,
        val type: ColType,
        val width: Int? = null,
        val precision: Int? = null,
        val javaType: Class<*>,
        val unsigned: Boolean = false,
        val auto: Boolean = false,
        var pk: Boolean = false,
        val nullable: Boolean = true,
        val comment: String? = null
)