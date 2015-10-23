package com.asiainfo.dbb.util

import java.util.*

open class Registrar<K, V : Registrar.Applicant<K>> {

    protected val applicants = LinkedHashMap<K, V>()

    open fun register(value: V) {
        applicants[value.getKey()] = value
    }

    open fun unregister(key: K) {
        applicants.remove(key)
    }

    open fun unregister(value: V) {
        unregister(value.getKey())
    }

    operator fun get(key: K): V? {
        return applicants[key]
    }

    interface Applicant<K> {

        fun getKey(): K
    }
}