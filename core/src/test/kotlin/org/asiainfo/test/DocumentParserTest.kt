package org.asiainfo.test

import com.asiainfo.dbb.record.RecordDocumentParser
import com.asiainfo.dbb.table.TableDocumentParser
import org.junit.Test
import org.nutz.lang.Files

class DocumentParserTest {

    @Test
    fun parseTables() {
        TableDocumentParser.parse(Files.read("tables.yml"))
    }

    @Test
    fun parseRecords() {
        val tables = TableDocumentParser.parse(Files.read("tables.yml"))
        val records = RecordDocumentParser.parse(Files.read("records.yml")) { name ->
            tables.find { it.name == name }!!
        }
        println(records)
    }

    @Test
    fun toTableDocument() {
        println(TableDocumentParser.toDocument(TableDocumentParser.parse(Files.read("tables.yml"))))
    }
}