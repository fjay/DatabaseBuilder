package com.asiainfo.dbb.table.adapter

import com.asiainfo.dbb.util.Registrator
import org.nutz.dao.DB

object DatabaseAdapters : Registrator<DB, DatabaseAdapter> () {

    init {
        register(MysqlAdapter)
        register(HsqldbAdapter)
    }
}