package org.asiainfo.test

class OtherTest {

    class ThreadBox<T>(v: T) {
        private val value = v
        @Synchronized fun <R> locked(f: T.() -> R): R = value.f()
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
}