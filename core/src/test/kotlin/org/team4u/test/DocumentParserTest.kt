package org.team4u.test

import org.team4u.dbb.record.RecordDocuments
import org.team4u.dbb.table.TableDocuments
import org.team4u.dbb.util.DataTableUtil
import org.junit.Test
import org.nutz.lang.Files

class DocumentParserTest {

    @Test
    fun parseTables() {
        TableDocuments.parse(Files.read("tables.yml"))
    }

    @Test
    fun parseRecords() {
        val tables = TableDocuments.parse(Files.read("tables.yml"))
        val records = RecordDocuments.parse(Files.read("records.yml")) { name ->
            tables.find { it.name == name }!!
        }
        println(records)
    }

    @Test
    fun toTableDocument() {
        println(TableDocuments.toDocument(TableDocuments.parse(Files.read("tables.yml"))))
    }

    @Test
    fun length() {
        println(DataTableUtil.Formatter.length("按固定值提成"))
        println(DataTableUtil.Formatter.length("Always Include"))
    }
}