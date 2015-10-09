package org.asiainfo.test

import com.asiainfo.dbb.record.RecordManager
import com.asiainfo.dbb.table.TableManager
import com.asiainfo.dbb.table.TableMetaDataLoader
import org.junit.Test
import org.nutz.lang.Files

class RecordManagerTest : IocTest() {

    private fun getTableManager(): TableManager {
        return TableManager(dao())
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