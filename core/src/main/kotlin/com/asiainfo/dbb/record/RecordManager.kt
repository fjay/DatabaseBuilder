package com.asiainfo.dbb.record

import com.asiainfo.dbb.model.Record
import com.asiainfo.dbb.table.TableManager
import org.nutz.dao.Chain
import org.nutz.dao.Dao
import org.nutz.log.Logs
import java.io.Writer

class RecordManager(val dao: Dao, val tables: TableManager.Tables, val text: String) {

    private val records: List<Record>


    private val log = Logs.get()

    init {
        records = RecordDocumentParser.parse(text) {
            tables.getTable(it) ?: throw IllegalArgumentException("Invalid table(name=$it)")
        }
    }

    fun execute() {
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

    fun toDocument(writer: Writer, vararg tables: String) {
        val tableNames = if (tables.isEmpty()) {
            TableManager.createWithDB(dao).getTables().map {
                it.name
            }
        } else {
            tables.asList()
        }

        for (it in tableNames) {
            dao.each(it, null) { index, ele, length ->
                writer.write(RecordDocumentParser.toDocument(listOf(ele)))
            }
        }
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