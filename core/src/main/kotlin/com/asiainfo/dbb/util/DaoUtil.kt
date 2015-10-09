package com.asiainfo.dbb.util

import org.nutz.dao.entity.Record
import org.nutz.dao.util.Daos
import java.sql.ResultSet

object DaoUtil {

    inline fun each(rs: ResultSet?, callback: (Record) -> Unit) {
        if (rs == null) {
            return
        }

        try {
            while (rs.next()) {
                callback(Record.create(rs))
            }
        } finally {
            Daos.safeClose(rs)
        }
    }
}