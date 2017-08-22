package org.team4u.dbb.record

import org.apache.ddlutils.model.Table
import org.nutz.dao.Chain
import org.nutz.dao.Dao
import org.nutz.log.Logs
import org.team4u.dbb.model.Column
import org.team4u.dbb.model.Record
import org.team4u.dbb.table.TableManager
import java.util.*

class RecordManager(val dao: Dao, val tables: TableManager.Tables) {

    private val log = Logs.get()

    fun fillData(text: String) {
        val records = RecordDocuments.parse(text) {
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

    fun toDocument(tableNames: List<String>? = null): String {
        val manager = TableManager.createWithDB(dao)
        val tables = manager.getTables(tableNames)
        val result = ArrayList<Record>()

        for (table in tables) {
            try {
                result.add(Record(table, Record.LoadMethod.CLEAR_AND_INSERT, loadData(table)))
            } catch(e: Exception) {
                throw RuntimeException("LoadData fail(table=${table.name})", e)
            }
        }

        return RecordDocuments.toDocument(result)
    }

    private fun loadData(table: Table): ArrayList<Record.Data> {
        val recordData = ArrayList<Record.Data>()
        dao.each(table.name, null) { index, ele, length ->
            val columnData = ArrayList<Record.ColumnData>()

            table.columns.forEach {
                columnData.add(Record.ColumnData(Column() it, ele.get(it.name)))
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
                record.table.name, record.data.size)
    }
}