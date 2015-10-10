package com.asiainfo.dbb.record.transformer

import org.nutz.el.El
import org.nutz.lang.Lang
import org.nutz.lang.Times
import org.nutz.lang.random.R
import org.nutz.lang.util.Context
import java.util.*

object DataTransformers {

    private val context: Context = Lang.context();

    init {
        register(StringTransformer)
        register(DateTransformer)
    }

    fun register(value: DataTransformer) {
        context.set(value.getKey(), value)
    }

    fun execute(value: String): Any? {
        return El.eval(context, value)
    }

    object StringTransformer : DataTransformer {
        override fun getKey(): String {
            return "Strings"
        }

        @JvmStatic
        fun uuid(): String {
            return R.UU32().toUpperCase()
        }
    }

    object DateTransformer : DataTransformer {
        override fun getKey(): String {
            return "Dates"
        }

        @JvmStatic
        @JvmOverloads
        fun now(fmt: String = "yyyy-MM-dd HH:mm:ss"): String {
            return Times.format(fmt, Date())
        }
    }
}