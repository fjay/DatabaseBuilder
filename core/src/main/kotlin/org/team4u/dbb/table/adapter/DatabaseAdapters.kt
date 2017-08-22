package org.team4u.dbb.table.adapter

import org.nutz.dao.DB
import org.team4u.kit.core.lang.Registry

object DatabaseAdapters : Registry<DB, DatabaseAdapter>() {

    init {
        register(MysqlAdapter)
        register(HsqldbAdapter)
    }
}