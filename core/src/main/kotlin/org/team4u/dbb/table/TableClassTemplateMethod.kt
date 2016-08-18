package org.team4u.dbb.table

import org.team4u.dbb.model.Column
import org.team4u.dbb.model.Table
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

    @JvmStatic fun javaTypeName(column: Column): String {
        return column.javaType.name
    }

    @JvmStatic fun javaTypeSimpleName(column: Column): String {
        return column.javaType.simpleName
    }
}