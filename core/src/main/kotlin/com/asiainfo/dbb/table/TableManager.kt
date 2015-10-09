package com.asiainfo.dbb.table

import com.asiainfo.dbb.model.Table
import org.nutz.dao.Dao

class TableManager {

    val tables: List<Table>

    val dao: Dao

    constructor(dao: Dao, text: String) {
        this.dao = dao
        tables = TableDocumentParser.parse(text)
    }

    constructor(dao: Dao) {
        this.dao = dao
        tables = TableMetaDataLoader(dao).load()
    }

    fun getTable(name: String): Table? {
        return tables.find { it.name == name }
    }
}