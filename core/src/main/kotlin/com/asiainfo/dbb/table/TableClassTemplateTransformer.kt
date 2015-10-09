package com.asiainfo.dbb.table

import org.nutz.lang.Strings
import org.rythmengine.extension.Transformer

@Transformer
object TableClassTemplateTransformer {
    @JvmStatic fun upperWord(cs: String): String {
        return Strings.upperWord(cs, '_')
    }

    @JvmStatic fun upperFirst(cs: String): String {
        return Strings.upperFirst(cs)
    }

    @JvmStatic fun format(list: List<String>): String {
        return "\"" + list.join("\",\"") + "\""
    }
}