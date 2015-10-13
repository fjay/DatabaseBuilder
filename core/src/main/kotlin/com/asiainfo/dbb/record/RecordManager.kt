package com.asiainfo.dbb.record

import com.asiainfo.dbb.model.Record
import com.asiainfo.dbb.model.Table
import com.asiainfo.dbb.table.TableManager
import org.nutz.dao.Chain
import org.nutz.dao.Dao
import org.nutz.log.Logs
import java.util.*

class RecordManager(val dao: Dao, val tables: TableManager.Tables) {

    private val log = Logs.get()

    fun fillData(text: String) {
        val records = RecordDocumentParser.parse(text) {
            tables.getTable(it) ?: throw IllegalArgumentException("Invalid table(name=$it)")
        }

        records.forEach {
            when (it.loadMethod) {
                Record.LoadMethod.CLEAR_AND_INSERT -> {
                    val count = dao.clear(it.table.name)

                    log.debugf("Clear data success(table=%s,count=%d)",
                            it.table.name, count)

                    insert(it)
                }

                Record.LoadMethod.INSERT -> {
                    insert(it)
                }
            }
        }
    }

    fun toDocument(vararg tables: String): String {
        val tables = if (tables.isEmpty()) {
            TableManager.createWithDB(dao).getTables()
        } else {
            val t = TableManager.createWithDB(dao)
            tables.map {
                t.getTable(it)!!
            }
        }

        val result = ArrayList<Record>()

        for (table in tables) {
            result.add(Record(table, Record.LoadMethod.CLEAR_AND_INSERT, loadRecords(table)))
        }

        return RecordDocumentParser.toDocument(result)
    }

    private fun loadRecords(table: Table): ArrayList<Record.Data> {
        val recordData = ArrayList<Record.Data>()
        dao.each(table.name, null) { index, ele, length ->
            val columnData = ArrayList<Record.ColumnData>()

            table.columns.forEach {
                columnData.add(Record.ColumnData(it, ele.get(it.name)))
            }

            recordData.add(Record.Data(columnData))
        }

        return recordData
    }

    private fun insert(record: Record) {
        record.data.forEach {
            var chain: Chain? = null

            it.columnData.forEach {
                if (chain == null) {
                    chain = Chain.make(it.column.name, it.value)
                } else {
                    chain!!.add(it.column.name, it.value)
                }
            }

            dao.insert(record.table.name, chain)
        }

        log.debugf("Insert data success(table=%s,count=%d)",
                record.table.name, record.data.size())
    }
}