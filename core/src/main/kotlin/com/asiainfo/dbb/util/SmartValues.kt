package com.asiainfo.dbb.util

import org.nutz.castor.Castors
import org.nutz.lang.Strings

object SmartValues {

    inline fun <reified T : Any> value(value: String, defaultValue: T? = null): T? {
        if (Strings.isBlank(value)) {
            return defaultValue
        }

        return Castors.create().castTo(value, T::class.java)
    }
}
