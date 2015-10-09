package com.asiainfo.dbb.record

import com.asiainfo.dbb.model.Record
import com.asiainfo.dbb.model.Table
import com.asiainfo.dbb.record.transformer.DataTransformers
import com.asiainfo.dbb.util.DataTableUtil
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import org.nutz.castor.Castors
import org.nutz.lang.Strings
import java.util.*

object RecordDocumentParser {

    fun parse(tables: List<Table>, text: String): List<Record> {
        val result = ArrayList<Record>()
        val mapper = ObjectMapper(YAMLFactory());
        val doc = mapper.readValue(text, RecordDocument::class.java);

        for (it in doc.records) {
            val tableName = it.table ?: throw IllegalArgumentException("Invalid table")

            val table = tables.find { it.name == tableName } ?:
                    throw IllegalArgumentException("Invalid table")

            val data = it.data ?: throw IllegalArgumentException("Invalid data")

            val loadMethod = Record.LoadMethod.valueOf(it.loadMethod!!)

            val record = Record(table, loadMethod, parseData(table, data))
            result.add(record)
        }

        return result
    }

    private fun parseData(table: Table, text: String): List<Record.Data> {
        val result = ArrayList<Record.Data>()
        DataTableUtil.each(text) { map ->
            val columnData = ArrayList<Record.ColumnData>()
            map.forEach {
                val columnName = it.key
                val column = table.columns.find {
                    it.name == columnName
                } ?: throw  IllegalArgumentException("Invalid column")

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

        val dt = DataTransformers[this!!]
        val value = (if (dt != null) dt.execute() else this) ?: return null

        return Castors.create().castTo(value.trim(), toClass)
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