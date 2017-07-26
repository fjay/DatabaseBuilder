package org.team4u.test

import org.junit.Test
import org.nutz.lang.Files
import org.team4u.dbb.record.RecordDocuments
import org.team4u.dbb.record.RecordManager
import org.team4u.dbb.table.TableManager
import org.team4u.dbb.table.TableMetaDataLoader

class RecordManagerTest {

    @Test
    fun insert() {
        RecordManager(TestUtil.dao, TableManager.createWithDB(TestUtil.dao)).fillData(Files.read("records.yml"))
    }

    @Test
    fun loadMetaData() {
        println(TableMetaDataLoader(TestUtil.dao).load())
    }

    @Test
    fun toDocument() {
        val tables = TableManager.createWithDocument(Files.read("tables.yml"))
        println(RecordDocuments.toDocument(RecordDocuments.parse(Files.read("records.yml")) {
            tables.getTable(it)!!
        }))
    }
}