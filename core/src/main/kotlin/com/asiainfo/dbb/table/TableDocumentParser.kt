package com.asiainfo.dbb.table

import com.asiainfo.dbb.model.Column
import com.asiainfo.dbb.model.Index
import com.asiainfo.dbb.model.Table
import com.asiainfo.dbb.util.TableFormatter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import org.nutz.dao.entity.annotation.ColType
import org.nutz.lang.Lang
import org.nutz.lang.Strings
import org.nutz.lang.segment.CharSegment
import org.nutz.lang.stream.StringWriter
import java.math.BigDecimal
import java.util.*

object TableDocumentParser {

    fun parse(text: String): List<Table> {
        val result = ArrayList<Table>()
        val mapper = ObjectMapper(YAMLFactory())
        val doc = mapper.readValue(text, TableDocument::class.java)

        for (it in doc.tables) {
            val name = it.name ?:
                    throw IllegalArgumentException("Invalid name")
            val columns = it.columns ?:
                    throw IllegalArgumentException("Invalid name")
            val indexes = parseIndexes(it.indexes)

            val table = Table(
                    name = name,
                    columns = parseColumns(columns),
                    primaryKey = indexes.find { it.type == Index.Type.PK },
                    indexes = indexes.filter { it.type != Index.Type.PK },
                    comment = it.comment)
            result.add(table)
        }

        return result
    }

    fun toDocument(tables: List<Table>): String {
        val doc = TableDocument()
        tables.forEach { t ->
            val table = TableDocument.Table().apply {
                name = t.name
                comment = t.comment

                columns = "\${$name}"

                val indexes = t.indexes.toArrayList()
                if ( t.primaryKey != null) {
                    indexes.add(t.primaryKey)
                }
                this.indexes = indexes.map { i ->
                    TableDocument.Index().apply {
                        name = i.name
                        type = i.type.name()
                        columns = i.columns
                    }
                }
            }

            doc.tables.add(table)
        }

        return format(doc, tables)
    }

    private fun format(doc: TableDocument, tables: List<Table>): String {
        val sb = StringBuilder()
        ObjectMapper(YAMLFactory()).writeValue(StringWriter(sb), doc)
        val seg = CharSegment(sb.toString().replace("\"", ""));

        tables.forEach { t ->
            seg.set(t.name, "|\n" + TableFormatter.format(t.columns.map {
                linkedMapOf(
                        "\t" + TableDocument.ColumnKey.NAME.name() to "\t" + it.name,
                        TableDocument.ColumnKey.TYPE.name() to it.type.name(),
                        TableDocument.ColumnKey.LENGTH.name() to it.width,
                        TableDocument.ColumnKey.NULLABLE.name() to it.nullable,
                        TableDocument.ColumnKey.COMMENT.name() to it.comment
                )
            }))
        }

        return seg.toString()
    }

    private fun parseColumns(text: String): List<Column> {
        val result = ArrayList<Column>()
        TableFormatter.each(text) { map ->
            val type = ColType.valueOf(map.getNotNullColumnHeader(TableDocument.ColumnKey.TYPE))

            val name = map.getNotNullColumnHeader(TableDocument.ColumnKey.NAME)
            val nullable = Lang.parseBoolean(map.getColumnHeader(TableDocument.ColumnKey.NULLABLE) ?: "1")
            val pk = Lang.parseBoolean(map.getColumnHeader(TableDocument.ColumnKey.PK) ?: "0")
            val (width, precision) = getColumnLength(map)

            result.add(Column(
                    name = name,
                    width = width,
                    precision = precision,
                    type = type,
                    javaType = asJavaType(type),
                    pk = pk,
                    nullable = nullable,
                    comment = map[TableDocument.ColumnKey.COMMENT.name()]
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
        val lengthString = map.getColumnHeader(TableDocument.ColumnKey.LENGTH)

        if (lengthString == null) {
            return Pair(null, null)
        }

        val lengthPair = lengthString.split(",")
        val width = lengthPair[0].toInt()
        val precision = if (lengthPair.size() == 2) {
            lengthPair[1].toInt()
        } else {
            0
        }
        return Pair(width, precision)
    }

    private fun Map<String, String>.getColumnHeader(key: TableDocument.ColumnKey): String? {
        val value = this[key.name()]

        if (Strings.isBlank(value)) {
            return null
        }

        return value
    }

    private fun Map<String, String>.getNotNullColumnHeader(key: TableDocument.ColumnKey) =
            this.getColumnHeader(key) ?: throw IllegalArgumentException("Invalid ColumnHeader:$key")

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
            NULLABLE, PK, UNSIGNED, AUTO
        }
    }
}