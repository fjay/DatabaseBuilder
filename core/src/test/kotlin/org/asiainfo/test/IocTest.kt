package org.asiainfo.test

import org.junit.runner.RunWith
import org.nutz.dao.impl.NutDao
import org.nutz.ioc.Ioc
import org.nutz.mvc.annotation.IocBy
import org.nutz.mvc.ioc.provider.ComboIocProvider
import org.nutz.test.NutTestContext
import org.nutz.test.junit48.NutJunit48Runner
import javax.sql.DataSource

@RunWith(NutJunit48Runner::class)
@IocBy(type = ComboIocProvider::class, args = arrayOf("*org.nutz.ioc.loader.json.JsonLoader", "/ioc"))
open class IocTest {

    private val ioc = lazy { NutTestContext.me().ioc }

    private val dao = lazy { ioc().get(NutDao::class.java, "dao") }

    fun ioc(): Ioc {
        return ioc.value
    }

    fun dao(): NutDao {
        return dao.value
    }

    fun dataSource(): DataSource {
        return ioc().get(DataSource::class.java)
    }
}