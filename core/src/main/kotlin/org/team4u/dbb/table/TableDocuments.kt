package org.team4u.dbb.table

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.xiaoleilu.hutool.convert.Convert
import org.apache.ddlutils.model.*
import org.nutz.dao.entity.annotation.ColType
import org.nutz.lang.Strings
import org.nutz.lang.segment.CharSegment
import org.nutz.lang.stream.StringWriter
import org.team4u.dbb.model.Column
import org.team4u.dbb.model.IndexType
import org.team4u.dbb.util.DataTableUtil
import java.math.BigDecimal
import java.util.*

object TableDocuments {

    fun parse(text: String): List<Table> {
        val result = ArrayList<Table>()
        val mapper = ObjectMapper(YAMLFactory())
        val doc = mapper.readValue(text, TableDocument::class.java)

        for (tableDoc in doc.tables) {
            val table = Table().apply {
                name = tableDoc.name
                description = tableDoc.comment
                addColumns(parseColumns(tableDoc.columns!!))
                addIndices(parseIndexes(this, tableDoc.indexes))
            }
            result.add(table)
        }

        return result
    }

    fun toDocument(tables: List<Table>): String {
        val doc = TableDocument()
        for (t in tables) {
            val tableDoc = TableDocument.Table().apply {
                name = t.name
                comment = t.description

                columns = "\${$name}"

                val indexes = t.indices.toMutableList()

                this.indexes = indexes.map { i ->
                    TableDocument.Index().apply {
                        name = i.name
                        type = when (i) {
                            is NonUniqueIndex ->
                                IndexType.INDEX.name

                            is UniqueIndex ->
                                IndexType.UNIQUE.name

                            else ->
                                IndexType.UNKNOWN.name
                        }
                        columns = i.columns.map {
                            it.name
                        }
                    }
                }
            }

            doc.tables.add(tableDoc)
        }

        return format(doc, tables)
    }

    private fun format(doc: TableDocument, tables: List<Table>): String {
        val sb = StringBuilder()
        ObjectMapper(YAMLFactory()).writeValue(StringWriter(sb), doc)
        val seg = CharSegment(sb.toString().replace("\"", ""));

        tables.forEach { table ->
            seg.set(table.name, "|\n" + table.columns.map {
                val indent = "    "
                linkedMapOf(
                        indent + TableDocument.ColumnKey.NAME.name to indent + it.name,
                        TableDocument.ColumnKey.TYPE.name to it.type,
                        TableDocument.ColumnKey.LENGTH.name to it.size,
                        TableDocument.ColumnKey.NULLABLE.name to !it.isRequired,
                        TableDocument.ColumnKey.COMMENT.name to it.description
                )
            })
        }

        return seg.toString()
    }

    private fun parseColumns(text: String): List<Column> {
        val result = ArrayList<Column>()
        DataTableUtil.each(text) { map ->
            val type = ColType.valueOf(map.getNotNullColumn(TableDocument.ColumnKey.TYPE))

            result.add(Column(asJavaType(type)).apply {
                name = map.getNotNullColumn(TableDocument.ColumnKey.NAME)
                isRequired = !Convert.toBool(map.getProperty(TableDocument.ColumnKey.NULLABLE) ?: "0")
                isPrimaryKey = Convert.toBool(map.getProperty(TableDocument.ColumnKey.PK) ?: "0")
                description = map.getProperty(TableDocument.ColumnKey.COMMENT)
                defaultValue = map.getProperty(TableDocument.ColumnKey.DEFAULT)
                isAutoIncrement = Convert.toBool(map.getProperty(TableDocument.ColumnKey.AUTO) ?: "0")
                size = map.getProperty(TableDocument.ColumnKey.LENGTH)
            })
        }

        return result
    }

    private fun parseIndexes(table: Table, indexes: List<TableDocument.Index>): List<Index> {
        val result = ArrayList<Index>()
        indexes.forEach { i ->
            val index = when (IndexType.valueOf(i.type!!)) {
                IndexType.INDEX ->
                    NonUniqueIndex()
                IndexType.UNIQUE ->
                    UniqueIndex()
                else ->
                    throw IllegalArgumentException("Invalid index type: ${i.type}")
            }

            index.apply {
                name = i.name
                i.columns.forEach {
                    addColumn(IndexColumn(table.findColumn(it)))
                }
            }

            result.add(index)
        }

        return result
    }

    fun asJavaType(type: ColType): Class<*> {
        return when (type) {
            ColType.CHAR ->
                Char::class.java

            ColType.BOOLEAN ->
                Boolean::class.java

            ColType.VARCHAR ->
                String::class.java

            ColType.TEXT ->
                String::class.java

            ColType.BINARY ->
                ByteArray::class.java

            ColType.DATETIME ->
                Date::class.java

            ColType.TIMESTAMP ->
                Date::class.java

            ColType.DATE ->
                Date::class.java

            ColType.TIME ->
                Date::class.java

            ColType.INT ->
                Int::class.java

            ColType.FLOAT ->
                BigDecimal::class.java

            else -> {
                throw IllegalArgumentException("Invalid ColType:$type")
            }
        }
    }

    private fun Map<String, String>.getProperty(key: TableDocument.ColumnKey): String? {
        val value = this[key.name]

        if (Strings.isBlank(value)) {
            return null
        }

        return value
    }

    private fun Map<String, String>.getNotNullColumn(key: TableDocument.ColumnKey) =
            this.getProperty(key) ?: throw IllegalArgumentException("Invalid column property:$key")

    private class TableDocument {

        var tables = ArrayList<Table>()

        class Table {
            var name: String? = null
            var comment: String? = null
            var columns: String? = null
            var indexes: List<Index> = ArrayList()
        }

        class Index {
            var type: String? = null
            var name: String? = null
            var columns: List<String> = ArrayList()
        }

        enum class ColumnKey {
            NAME, TYPE, LENGTH, COMMENT, PK,
            NULLABLE, UNSIGNED, AUTO, DEFAULT
        }
    }
}