package com.asiainfo.dbb.table

import com.asiainfo.dbb.model.Table
import org.nutz.dao.Dao

class TableManager {

    companion object {

        fun createWithDocument(text: String): Tables {
            return TablesInDocument(text)
        }

        fun createWithDB(dao: Dao): Tables {
            return TablesInDB(dao)
        }
    }

    interface Tables {

        fun getTable(name: String): Table?

        fun getTables(): List<Table>

    }

    private class TablesInDocument(val document: String) : Tables {

        private val tables: List<Table>

        init {
            tables = TableDocuments.parse(document)
        }

        override fun getTable(name: String): Table? {
            return tables.find { it.name == name }
        }

        override fun getTables(): List<Table> {
            return tables
        }
    }

    private class TablesInDB(val dao: Dao) : Tables {

        override fun getTable(name: String): Table? {
            val tables = TableMetaDataLoader(dao).load(name)
            if (tables.isEmpty()) {
                return null
            } else {
                return tables.first()
            }
        }

        override fun getTables(): List<Table> {
            return TableMetaDataLoader(dao).load()
        }
    }
}