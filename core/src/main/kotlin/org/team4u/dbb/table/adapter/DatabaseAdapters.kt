package org.team4u.dbb.table.adapter

import org.team4u.dbb.util.Registrar
import org.nutz.dao.DB

object DatabaseAdapters : Registrar<DB, DatabaseAdapter> () {

    init {
        register(MysqlAdapter)
        register(HsqldbAdapter)
    }
}