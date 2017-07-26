package org.team4u.test

import com.alibaba.druid.pool.DruidDataSource
import org.nutz.dao.Dao
import org.nutz.dao.impl.NutDao
import javax.sql.DataSource

/**
 * @author Jay Wu
 */
object TestUtil {

    val datasource by lazy {
        createDataSource()
    }

    val dao by lazy {
        createDao()
    }

    fun createDataSource(): DataSource {
        val ds = DruidDataSource()
        ds.url = "jdbc:hsqldb:mem:db"
        ds.username = "sa"
        ds.password = ""
        ds.initialSize = 1
        return ds
    }

    fun createDao(): Dao {
        return NutDao(createDataSource())
    }
}