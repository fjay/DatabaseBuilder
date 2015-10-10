package com.asiainfo.dbb.table

import com.asiainfo.dbb.model.Table
import org.nutz.dao.Dao

class TableManager {

    companion object {

        fun createWithDocument(dao: Dao, text: String): Tables {
            return TablesInDocument(dao, text)
        }

        fun createWithDB(dao: Dao): Tables {
            return TablesInDB(dao)
        }
    }

    interface Tables {

        val dao: Dao

        fun getTable(name: String): Table?

        fun getTables(): List<Table>

    }

    private class TablesInDocument(override val dao: Dao, val document: String) : Tables {

        private val tables: List<Table>

        init {
            tables = TableDocumentParser.parse(document)
        }

        override fun getTable(name: String): Table? {
            return tables.find { it.name == name }
        }

        override fun getTables(): List<Table> {
            return tables
        }
    }

    private class TablesInDB(override val dao: Dao) : Tables {

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