package org.asiainfo.test

import com.asiainfo.dbb.record.RecordManager
import com.asiainfo.dbb.table.TableManager
import com.asiainfo.dbb.table.TableMetaDataLoader
import org.junit.Test
import org.nutz.castor.Castors
import org.nutz.lang.Files
import java.util.*

class RecordManagerTest : IocTest() {

    private fun getTableManager(): TableManager.Tables {
        return TableManager.createWithDB(dao())
    }

    @Test
    fun cast() {
        println(Castors.create().castTo("2015-10-01", Date::class.java))
    }

    @Test
    fun insert() {
        RecordManager(getTableManager(), Files.read("records.yml")).execute()
    }

    @Test
    fun loadMetaData() {
        println(TableMetaDataLoader(dao()).load())
    }
}