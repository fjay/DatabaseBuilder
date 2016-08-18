package org.team4u.test

import org.team4u.dbb.record.RecordDocuments
import org.team4u.dbb.record.RecordManager
import org.team4u.dbb.table.TableManager
import org.team4u.dbb.table.TableMetaDataLoader
import org.junit.Test
import org.nutz.lang.Files

class RecordManagerTest : IocTest() {

    @Test
    fun insert() {
        RecordManager(dao(), TableManager.Companion.createWithDB(dao())).fillData(Files.read("records.yml"))
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