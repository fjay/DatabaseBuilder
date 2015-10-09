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
        val records = RecordDocumentParser.parse(
                TableDocumentParser.parse(Files.read("tables.yml")),
                Files.read("records.yml"))
        println(records)
    }

    @Test
    fun toTableDocument() {
        println(TableDocumentParser.toDocument(TableDocumentParser.parse(Files.read("tables.yml"))))
    }
}