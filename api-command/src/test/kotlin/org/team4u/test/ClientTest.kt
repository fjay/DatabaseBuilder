package org.team4u.test

import org.junit.Test
import org.team4u.dbb.command.Client

class ClientTest {

    @Test
    fun main() {
        Client.main(arrayOf("-ct", "TEST, TEST2", "-ctc", "TEST"))
    }
}