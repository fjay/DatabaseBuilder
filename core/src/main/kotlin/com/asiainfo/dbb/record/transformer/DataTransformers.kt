package com.asiainfo.dbb.record.transformer

import com.asiainfo.dbb.util.Registrator
import org.nutz.lang.random.R

object DataTransformers : Registrator<String, DataTransformer> () {

    init {
        register(UUIDTransformer)
    }

    private object UUIDTransformer : DataTransformer {
        override fun getKey(): String {
            return "uuid()"
        }

        override fun execute(): String? {
            return R.UU32()
        }
    }
}