package org.team4u.test

import com.alibaba.druid.pool.DruidDataSource
import org.apache.ddlutils.PlatformFactory
import org.apache.ddlutils.io.DatabaseIO
import org.junit.Test
import javax.sql.DataSource


class OtherTest {

    class ThreadBox<T>(v: T) {
        private val value = v
        @Synchronized
        fun <R> locked(f: T.() -> R): R = value.f()
    }

    class ThreadBox2

    fun locked1() {
        val bank = ThreadBox(object {
            val accounts = intArrayOf(10, 0, 0, 0)
        })

        bank.locked {
            accounts[1]
        }
    }

    private fun createDataSource(): DataSource {
        val ds = DruidDataSource()
        ds.url = "jdbc:mysql://127.0.0.1:3306/test?useUnicode=true&characterEncoding=UTF-8"
        ds.username = "root"
        ds.password = "1234"
        ds.initialSize = 1
        return ds
    }

    @Test
    fun x() {
        val platform = PlatformFactory.createNewPlatformInstance(createDataSource());
        val db = platform.readModelFromDatabase("model");
        DatabaseIO().write(db, "../Test.xml")
    }
}