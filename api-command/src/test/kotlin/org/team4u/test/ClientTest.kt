package org.team4u.test

import org.team4u.dbb.command.Client
import org.junit.Test

class ClientTest : IocTest() {

    @Test
    fun main() {
        Client.main(arrayOf("-ctc","-h"))
    }
}