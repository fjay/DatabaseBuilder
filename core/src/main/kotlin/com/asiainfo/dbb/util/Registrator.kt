package com.asiainfo.dbb.util

import java.util.*

open class Registrator<K, V : Registrator.Applicant<K>> {

    private val map = HashMap<K, V>()

    open fun register(value: V) {
        map[value.getKey()] = value
    }

    operator fun get(key: K): V? {
        return map[key]
    }

    interface Applicant<K> {

        fun getKey(): K
    }
}