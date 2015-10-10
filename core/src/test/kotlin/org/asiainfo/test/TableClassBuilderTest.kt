package org.asiainfo.test

import com.asiainfo.dbb.table.TableClassBuilder
import com.asiainfo.dbb.table.TableDocumentParser
import com.asiainfo.dbb.table.TableManager
import org.junit.Test
import org.nutz.lang.Files

class TableClassBuilderTest : IocTest() {

    @Test
    fun buildJavaFile() {
        TableClassBuilder(TableManager.createWithDocument(dao(), Files.read("tables.yml")).getTables())
                .buildJavaFile("com.asiainfo.test.entity", "~/tmp/entity")
    }

    @Test
    fun toTableDocument() {
        println(TableDocumentParser.toDocument(TableManager.createWithDB(dao()).getTables()))
    }
}