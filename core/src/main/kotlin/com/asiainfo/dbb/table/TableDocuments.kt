package com.asiainfo.dbb.table

import com.asiainfo.dbb.model.Column
import com.asiainfo.dbb.model.Index
import com.asiainfo.dbb.model.Table
import com.asiainfo.dbb.util.DataTableUtil
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import org.nutz.dao.entity.annotation.ColType
import org.nutz.lang.Lang
import org.nutz.lang.Strings
import org.nutz.lang.segment.CharSegment
import org.nutz.lang.stream.StringWriter
import java.math.BigDecimal
import java.util.*

object TableDocuments {

    fun parse(text: String): List<Table> {
        val result = ArrayList<Table>()
        val mapper = ObjectMapper(YAMLFactory())
        val doc = mapper.readValue(text, TableDocument::class.java)

        for (it in doc.tables) {
            val name = it.name!!
            val columns = it.columns!!
            val indexes = parseIndexes(it.indexes)
            val pk = indexes.find { it.type == Index.Type.PK }

            val table = Table(
                    name = name,
                    columns = parseColumns(columns, pk),
                    primaryKey = pk,
                    indexes = indexes.filter { it.type != Index.Type.PK },
                    comment = it.comment)
            result.add(table)
        }

        return result
    }

    fun toDocument(tables: List<Table>): String {
        val doc = TableDocument()
        for (t in tables) {
            val tableInDoc = TableDocument.Table().apply {
                name = t.name
                comment = t.comment

                columns = "\${$name}"

                val indexes = t.indexes.toMutableList()
                if (t.primaryKey != null) {
                    indexes.add(t.primaryKey)
                }
                this.indexes = indexes.map { i ->
                    TableDocument.Index().apply {
                        name = i.name
                        type = i.type.name
                        columns = i.columns
                    }
                }
            }

            doc.tables.add(tableInDoc)
        }

        return format(doc, tables)
    }

    private fun format(doc: TableDocument, tables: List<Table>): String {
        val sb = StringBuilder()
        ObjectMapper(YAMLFactory()).writeValue(StringWriter(sb), doc)
        val seg = CharSegment(sb.toString().replace("\"", ""));

        tables.forEach { table ->
            seg.set(table.name, "|\n" + DataTableUtil.format(table.columns.map {
                val length = if (it.precision == null || it.precision <= 0) {
                    it.width.toString()
                } else {
                    it.width.toString() + "," + it.precision
                }

                val indent = "    "
                linkedMapOf(
                        indent + TableDocument.ColumnKey.NAME.name to indent + it.name,
                        TableDocument.ColumnKey.TYPE.name to it.type.name,
                        TableDocument.ColumnKey.LENGTH.name to length,
                        TableDocument.ColumnKey.NULLABLE.name to it.nullable,
                        TableDocument.ColumnKey.COMMENT.name to it.comment
                )
            }))
        }

        return seg.toString()
    }

    private fun parseColumns(text: String, pk: Index?): List<Column> {
        val result = ArrayList<Column>()
        DataTableUtil.each(text) { map ->
            val type = ColType.valueOf(map.getNotNullColumn(TableDocument.ColumnKey.TYPE))

            val name = map.getNotNullColumn(TableDocument.ColumnKey.NAME)
            val nullable = Lang.parseBoolean(map.getProperty(TableDocument.ColumnKey.NULLABLE) ?: "1")
            val (width, precision) = getColumnLength(map)

            result.add(Column(
                    name = name,
                    width = width,
                    precision = precision,
                    type = type,
                    javaType = asJavaType(type),
                    pk = pk?.columns?.contains(name) == true,
                    nullable = nullable,
                    comment = map.getProperty(TableDocument.ColumnKey.COMMENT)
            ))
        }

        return result
    }

    private fun parseIndexes(indexes: List<TableDocument.Index>): List<Index> {
        val result = ArrayList<Index>()
        indexes.forEach {
            result.add(Index(
                    name = it.name,
                    type = Index.Type.valueOf(it.type!!),
                    columns = it.columns
            ))
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

    private fun getColumnLength(map: Map<String, String>): Pair<Int?, Int?> {
        val lengthString = map.getProperty(TableDocument.ColumnKey.LENGTH) ?: return Pair(null, null)

        val lengthPair = lengthString.split(",")
        val width = lengthPair[0].toInt()
        val precision = if (lengthPair.size == 2) lengthPair[1].toInt() else null
        return Pair(width, precision)
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
            NAME, TYPE, LENGTH, COMMENT,
            NULLABLE, UNSIGNED, AUTO
        }
    }
}