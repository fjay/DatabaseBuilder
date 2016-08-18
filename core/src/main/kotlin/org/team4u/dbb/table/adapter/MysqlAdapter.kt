package org.team4u.dbb.table.adapter

import org.nutz.dao.DB
import org.nutz.dao.entity.Record
import org.nutz.dao.entity.annotation.ColType

object MysqlAdapter : AbstractDatabaseAdapter() {

    override fun getKey(): DB {
        return DB.MYSQL
    }

    override fun asColType(record: Record): ColType? {
        return when (getTypeNameWithoutUnsigned(record)) {
            "TEXT" -> ColType.TEXT
            "MEDIUMTEXT" -> ColType.TEXT
            "LONGTEXT" -> ColType.TEXT

            "TINYINT" -> ColType.INT
            "BIT" -> ColType.INT
            "BIGINT" -> ColType.INT
            "MEDIUMINT" -> ColType.INT

            "MediumBlob" -> ColType.BINARY
            "LONGBLOB" -> ColType.BINARY

            "DECIMAL" -> ColType.FLOAT
            "DOUBLE" -> ColType.FLOAT

            else -> super.asColType(record)
        }
    }
}