package org.team4u.dbb.table

import org.nutz.dao.Dao
import org.team4u.dbb.model.Table

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

        fun getTables(tableNames: List<String>? = null): List<Table>
    }

    private class TablesInDocument(val document: String) : Tables {

        private val tables: List<Table>

        init {
            tables = TableDocuments.parse(document)
        }

        override fun getTable(name: String): Table? {
            return tables.find { it.name == name }
        }

        override fun getTables(tableNames: List<String>?): List<Table> {
            return if (tableNames != null && tableNames.isNotEmpty()) {
                tables.filter { tableNames.contains(it.name) }
            } else {
                tables
            }
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

        override fun getTables(tableNames: List<String>?): List<Table> {
            return if (tableNames != null && tableNames.isNotEmpty()) {
                TableMetaDataLoader(dao).load(tableNames.joinToString("|"))
            } else {
                TableMetaDataLoader(dao).load()
            }
        }
    }
}