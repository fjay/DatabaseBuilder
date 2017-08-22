package org.team4u.dbb.table.adapter

import org.nutz.dao.DB
import org.nutz.dao.entity.Record
import org.nutz.dao.entity.annotation.ColType
import org.team4u.kit.core.lang.Registry

interface DatabaseAdapter : Registry.Applicant<DB> {

    fun getTypeName(record: Record): String = record.getString("type_name")

    fun getTypeNameWithoutUnsigned(record: Record) = getTypeName(record).replace(" UNSIGNED", "")

    fun asColType(record: Record): ColType?
}