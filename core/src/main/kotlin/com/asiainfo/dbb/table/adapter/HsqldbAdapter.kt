package com.asiainfo.dbb.table.adapter

import org.nutz.dao.DB
import org.nutz.dao.entity.Record
import org.nutz.dao.entity.annotation.ColType

object HsqldbAdapter : AbstractDatabaseAdapter() {

    override fun getDB(): DB {
        return DB.HSQL
    }

    override fun asColType(record: Record): ColType? {
        return when (getTypeNameWithoutUnsigned(record)) {
            "FLOAT" -> ColType.FLOAT

            else -> super.asColType(record)
        }
    }
}