package com.asiainfo.dbb.table.adapter

import com.asiainfo.dbb.util.Registrator
import org.nutz.dao.DB
import org.nutz.dao.entity.Record
import org.nutz.dao.entity.annotation.ColType

interface DatabaseAdapter : Registrator.Applicant<DB> {

    fun getTypeName(record: Record) = record.getString("type_name")

    fun getTypeNameWithoutUnsigned(record: Record) = getTypeName(record).replace(" UNSIGNED", "")

    fun asColType(record: Record): ColType?
}