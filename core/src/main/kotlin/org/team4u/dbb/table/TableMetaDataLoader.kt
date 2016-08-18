package org.team4u.dbb.table

import org.team4u.dbb.model.Column
import org.team4u.dbb.model.Index
import org.team4u.dbb.model.Table
import org.team4u.dbb.table.adapter.DatabaseAdapters
import org.team4u.dbb.util.DaoUtil
import org.nutz.dao.Dao
import org.nutz.lang.Lang
import java.sql.Connection
import java.sql.DatabaseMetaData
import java.sql.ResultSet
import java.util.*

class TableMetaDataLoader(val dao: Dao) {

    private val adapter = DatabaseAdapters[dao.meta().type] ?: throw RuntimeException("Unsupported database")

    fun load(tableNamePattern: String = "%"): List<Table> {
        val tables = ArrayList<Table>()
        dao.run { conn: Connection ->
            val rs = conn.metaData.getTables(null, "%", tableNamePattern, arrayOf("TABLE"))
            DaoUtil.each(rs) { record ->
                val tableName = record.getString("table_name")
                val pk = parsePrimaryKey(conn.metaData, tableName)
                val columns = parseColumns(conn.metaData, tableName, pk)
                val indexes = parseIndexes(conn.metaData, tableName)

                tables.add(Table(
                        name = tableName,
                        columns = columns,
                        primaryKey = pk,
                        indexes = indexes.filter { it.name != pk?.name },
                        comment = record.getString("remarks")))
            }
        }

        return tables
    }

    private fun parseColumns(metaData: DatabaseMetaData, tableName: String, pk: Index?): List<Column> {
        val columns = ArrayList<Column>()
        val rs = metaData.getColumns(null, "%", tableName, "%")

        DaoUtil.each(rs) {
            columns.add(parseColumn(it, pk))
        }

        return columns
    }

    private fun parseColumn(record: org.nutz.dao.entity.Record, pk: Index?): Column {
        val type = adapter.asColType(record) ?:
                throw IllegalArgumentException("Unknown column type:" + adapter.getTypeName(record))

        val name = record.getString("column_name")
        return Column(
                name = name,
                type = type,
                width = record.getInt("column_size"),
                precision = record.getInt("decimal_digits"),
                javaType = TableDocuments.asJavaType(type),
                pk = pk?.columns?.contains(name) == true,
                nullable = Lang.parseBoolean(record.getString("nullable")),
                comment = record.getString("remarks")
        )
    }

    private fun parsePrimaryKey(metaData: DatabaseMetaData, tableName: String): Index? {
        val rs = metaData.getPrimaryKeys(null, null, tableName)
        val indexes = parseIndexes(rs, true)

        if (indexes.isEmpty()) {
            return null
        } else {
            return indexes.first()
        }
    }

    private fun parseIndexes(metaData: DatabaseMetaData, tableName: String): List<Index> {
        val rs = metaData.getIndexInfo(null, null, tableName, false, false)
        return parseIndexes(rs, false)
    }

    private fun parseIndexes(rs: ResultSet?, isPk: Boolean): List<Index> {
        val indexes = ArrayList<Index>()
        val indexMap = HashMap<String, MutableList<String>>()

        DaoUtil.each(rs) { record ->
            val name = if (isPk) record.getString("pk_name") else record.getString("index_name")
            val unique: Boolean = !Lang.parseBoolean(record.getString("non_unique"))

            var columns = indexMap.get(name)

            if (columns == null) {
                columns = ArrayList<String>()
                indexMap.put(name, columns)
                indexes.add(Index(parseIndexType(unique, isPk), name, columns))
            }

            columns.add(record.getString("column_name"))
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