package com.asiainfo.dbb.table

import com.asiainfo.dbb.model.Column
import com.asiainfo.dbb.model.Index
import com.asiainfo.dbb.model.Table
import com.asiainfo.dbb.table.adapter.DatabaseAdapters
import org.nutz.dao.Dao
import org.nutz.dao.util.Daos
import org.nutz.lang.Lang
import java.sql.Connection
import java.sql.DatabaseMetaData
import java.sql.ResultSet
import java.util.*

class TableMetaDataLoader(val dao: Dao) {

    private val adapter = DatabaseAdapters[dao.meta().type] ?: throw UnsupportedOperationException("暂不支持该数据库")

    fun load(tableNamePattern: String = "%"): List<Table> {
        val tables = ArrayList<Table>()
        dao.run { conn: Connection ->
            var rs: ResultSet? = null
            try {
                rs = conn.metaData.getTables(null, "%", tableNamePattern, arrayOf("TABLE"))
                while (rs.next()) {
                    val record = org.nutz.dao.entity.Record.create(rs)

                    val tableName = record.getString("table_name")
                    val columns = parseColumns(conn.metaData, tableName)
                    val pk = parsePrimaryKey(conn.metaData, tableName)
                    val indexes = parseIndexes(conn.metaData, tableName)

                    tables.add(Table(
                            name = tableName.toLowerCase(),
                            columns = columns,
                            primaryKey = pk,
                            indexes = indexes.filter { it.name != pk?.name },
                            comment = record.getString("remarks")))
                }
            } finally {
                Daos.safeClose(rs)
            }
        }

        return tables
    }

    private fun parseColumns(metaData: DatabaseMetaData, tableName: String): List<Column> {
        var rs: ResultSet? = null
        val columns = ArrayList<Column>()

        try {
            rs = metaData.getColumns(null, "%", tableName, "%")
            while (rs.next()) {
                columns.add(parseColumn(org.nutz.dao.entity.Record.create(rs)))
            }

            return columns
        } finally {
            Daos.safeClose(rs)
        }
    }

    private fun parseColumn(record: org.nutz.dao.entity.Record): Column {
        val type = adapter.asColType(record) ?:
                throw RuntimeException("Unknown column type:" + adapter.getTypeName(record))

        return Column(
                name = record.getString("column_name").toLowerCase(),
                type = type,
                width = record.getInt("column_size"),
                precision = record.getInt("decimal_digits"),
                javaType = TableDocumentParser.asJavaType(type),
                pk = false,
                nullable = Lang.parseBoolean(record.getString("nullable")),
                comment = record.getString("remarks")
        )
    }

    private fun parsePrimaryKey(metaData: DatabaseMetaData, tableName: String): Index? {
        var rs: ResultSet? = null
        try {
            rs = metaData.getPrimaryKeys(null, null, tableName)
            val indexes = parseIndexes(rs, true)

            if (indexes.isEmpty()) {
                return null
            } else {
                return indexes.first()
            }
        } finally {
            Daos.safeClose(rs)
        }
    }

    private fun parseIndexes(metaData: DatabaseMetaData, tableName: String): List<Index> {
        var rs: ResultSet? = null
        try {
            rs = metaData.getIndexInfo(null, null, tableName, false, false)
            return parseIndexes(rs, false)
        } finally {
            Daos.safeClose(rs)
        }
    }

    private fun parseIndexes(rs: ResultSet, isPk: Boolean): List<Index> {
        val indexes = ArrayList<Index>()
        val indexMap = HashMap<String, MutableList<String>>()
        while (rs.next()) {
            val record = org.nutz.dao.entity.Record.create(rs)
            val name = if (isPk) record.getString("pk_name") else record.getString("index_name")
            val unique: Boolean = !Lang.parseBoolean(record.getString("non_unique"))

            var columns = indexMap.get(name)

            if (columns == null) {
                columns = ArrayList<String>()
                indexMap.put(name, columns)
                indexes.add(Index(parseIndexType(unique, isPk), name, columns))
            }

            columns.add(record.getString("column_name").toLowerCase())
        }

        return indexes
    }

    private fun parseIndexType(unique: Boolean, isPk: Boolean): Index.Type {
        if (isPk) {
            return Index.Type.PK
        }

        if (unique) {
            return Index.Type.UNIQUE
        }

        return Index.Type.INDEX
    }
}