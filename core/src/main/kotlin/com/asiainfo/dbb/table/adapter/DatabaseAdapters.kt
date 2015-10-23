package com.asiainfo.dbb.table.adapter

import com.asiainfo.dbb.util.Registrar
import org.nutz.dao.DB

object DatabaseAdapters : Registrar<DB, DatabaseAdapter> () {

    init {
        register(MysqlAdapter)
        register(HsqldbAdapter)
    }
}