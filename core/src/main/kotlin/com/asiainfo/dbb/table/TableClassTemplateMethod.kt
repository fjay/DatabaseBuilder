package com.asiainfo.dbb.table

import com.asiainfo.dbb.model.Column
import com.asiainfo.dbb.model.Table
import org.nutz.lang.Lang
import org.nutz.lang.Strings

object TableClassTemplateMethod {

    @JvmStatic fun fieldName(cs: String): String {
        return upperWord(cs.toLowerCase())
    }

    @JvmStatic fun methodName(cs: String): String {
        return upperFirst(upperWord(cs.toLowerCase()))
    }

    @JvmStatic fun upperWord(cs: String): String {
        return Strings.upperWord(cs, '_')
    }

    @JvmStatic fun upperFirst(cs: String): String {
        return Strings.upperFirst(cs)
    }

    @JvmStatic fun format(list: List<String>): String {
        return "\"" + list.joinToString("\",\"") + "\""
    }

    @JvmStatic fun isSinglePKColumn(column: Column, table: Table): Boolean {
        val pks = table.primaryKey?.columns
        return Lang.length(pks) == 1 && pks?.first() == column.name
    }
}