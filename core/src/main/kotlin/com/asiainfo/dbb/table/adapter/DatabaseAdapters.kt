package com.asiainfo.dbb.table.adapter

import org.nutz.dao.DB
import java.util.*

object DatabaseAdapters {

    private val adapters = HashMap<DB, DatabaseAdapter>()

    init {
        register(MysqlAdapter)
        register(HsqldbAdapter)
    }

    fun register(adapter: DatabaseAdapter) {
        adapters[adapter.getDB()] = adapter
    }

    operator fun get(type: DB): DatabaseAdapter? {
        return adapters[type]
    }
}