package com.asiainfo.dbb.table.adapter

import org.nutz.dao.entity.Record
import org.nutz.dao.entity.annotation.ColType

abstract class AbstractDatabaseAdapter : DatabaseAdapter {

    override fun asColType(record: Record): ColType? {
        return when (getTypeNameWithoutUnsigned(record)) {
            "VARCHAR" -> ColType.VARCHAR

            "CHAR" -> ColType.CHAR

            "BOOLEAN" -> ColType.BOOLEAN

            "DATETIME" -> ColType.DATETIME

            "TIMESTAMP" -> ColType.TIMESTAMP

            "DATE" -> ColType.DATE

            "TIME" -> ColType.TIME

            "INT" -> ColType.INT

            "NUMERIC" -> ColType.FLOAT

            "CLOB" -> ColType.TEXT

            "BLOB" -> ColType.BINARY

            else -> null
        }
    }
}