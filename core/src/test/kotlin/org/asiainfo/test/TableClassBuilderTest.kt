package org.asiainfo.test

import com.asiainfo.dbb.table.TableClassBuilder
import com.asiainfo.dbb.table.TableDocumentParser
import com.asiainfo.dbb.table.TableManager
import org.junit.Test
import org.nutz.lang.Files

class TableClassBuilderTest : IocTest() {

    @Test
    fun buildClassesInMemory() {
        TableClassBuilder(TableManager(dao(), Files.read("tables.yml")).tables)
                .buildClassesInMemory("com.asiainfo.test.entity")
    }

    @Test
    fun toTableDocument() {
        println(TableDocumentParser.toDocument(TableManager(dao()).tables))
    }
}