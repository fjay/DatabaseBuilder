package org.team4u.dbb.record

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import org.apache.ddlutils.model.Table
import org.nutz.castor.Castors
import org.nutz.lang.Strings
import org.nutz.lang.segment.CharSegment
import org.nutz.lang.stream.StringWriter
import org.team4u.dbb.model.Record
import org.team4u.dbb.record.transformer.DataTransformers
import org.team4u.dbb.util.DataTableUtil
import java.util.*

object RecordDocuments {

    fun parse(text: String, tableProvider: (String) -> Table): List<Record> {
        val result = ArrayList<Record>()
        val mapper = ObjectMapper(YAMLFactory());
        val doc = mapper.readValue(text, RecordDocument::class.java);

        for (it in doc.records) {
            val tableName = it.table!!
            val table = tableProvider(tableName)
            val data = it.data!!

            val loadMethod = Record.LoadMethod.valueOf(it.loadMethod!!)

            val record = Record(table, loadMethod, parseData(table, data))
            result.add(record)
        }

        return result
    }

    fun toDocument(records: List<Record>): String {
        val doc = RecordDocument()
        for (record in records) {
            if (record.data.isEmpty()) {
                continue
            }

            val recordInDoc = RecordDocument.Record().apply {
                table = record.table.name
                loadMethod = record.loadMethod.name
                data = "\${$table}"
            }

            doc.records.add(recordInDoc)
        }

        return format(doc, records)
    }

    private fun format(doc: RecordDocument, records: List<Record>): String {
        val sb = StringBuilder()
        ObjectMapper(YAMLFactory()).writeValue(StringWriter(sb), doc)
        val seg = CharSegment(sb.toString().replace("\"", ""));

        records.forEach { record ->
            val value = "|\n" + record.data.map {
                val map = LinkedHashMap<String, String?>()
                it.columnData.forEachIndexed { i, columnData ->
                    val indent = if (i == 0) "    " else ""
                    map[indent + columnData.column.name] = indent + columnData.value.castToString()
                }

                map
            }
            seg.set(record.table.name, value)
        }

        return seg.toString()
    }

    private fun parseData(table: Table, text: String): List<Record.Data> {
        val result = ArrayList<Record.Data>()
        DataTableUtil.each(text) { map ->
            val columnData = ArrayList<Record.ColumnData>()
            map.forEach {
                val columnName = it.key
                val column = table.columns.find {
                    it.name == columnName
                } ?: throw  IllegalArgumentException("Invalid column(name=$columnName)")


                val value = it.value.castTo(column.javaType)
                columnData.add(Record.ColumnData(column, value))
            }

            result.add(Record.Data(columnData))
        }

        return result
    }

    private fun <T : Any> String?.castTo(toClass: Class<T>): T? {
        if (Strings.isBlank(this)) {
            return null
        }

        val value: String = this!!.trim()
        if (value == "null") {
            return null
        }

        val result = DataTransformers.execute(value) ?: return null
        return Castors.me().castTo(result, toClass)
    }

    private fun Any?.castToString(): String? {
        if (this == null) {
            return ""
        }

        if (this is Boolean) {
            return if (this) "1" else "0"
        }

        return Castors.me().castToString(this)
    }

    private class RecordDocument {

        var records = ArrayList<Record>()

        class Record {
            var table: String? = null
            var loadMethod: String? = null
            var data: String? = null
        }
    }
}