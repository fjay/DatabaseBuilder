package org.asiainfo.test

import com.asiainfo.dbb.command.Client
import org.junit.Test

class ClientTest : IocTest() {

    @Test
    fun main() {
        Client.main(arrayOf("-ctd", "my_stock", "my_transaction", "-crd"))
    }
}