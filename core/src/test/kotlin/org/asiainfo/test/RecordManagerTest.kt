package org.asiainfo.test

import com.asiainfo.dbb.record.RecordDocuments
import com.asiainfo.dbb.record.RecordManager
import com.asiainfo.dbb.table.TableManager
import com.asiainfo.dbb.table.TableMetaDataLoader
import org.junit.Test
import org.nutz.lang.Files

class RecordManagerTest : IocTest() {

    @Test
    fun insert() {
        RecordManager(dao(), TableManager.createWithDB(dao())).fillData(Files.read("records.yml"))
    }

    @Test
    fun loadMetaData() {
        println(TableMetaDataLoader(dao()).load())
    }

    @Test
    fun toDocument() {
        val tables = TableManager.createWithDocument(Files.read("tables.yml"))
        println(RecordDocuments.toDocument(RecordDocuments.parse(Files.read("records.yml")) {
            tables.getTable(it)!!
        }))
    }
}