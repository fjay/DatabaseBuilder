package org.asiainfo.test

import com.asiainfo.dbb.table.TableClassBuilder
import com.asiainfo.dbb.table.TableDocuments
import com.asiainfo.dbb.table.TableManager
import org.junit.Test
import org.nutz.lang.Files

class TableClassBuilderTest : IocTest() {

    @Test
    fun buildJavaFile() {
        TableClassBuilder(TableManager.createWithDocument(Files.read("tables.yml")).getTables())
                .buildJavaFile(
                        tableClassPackage = "com.asiainfo.test.entity",
                        path = "~/tmp/entity",
                        fileExtension = "java"
                )
    }

    @Test
    fun toTableDocument() {
        println(TableDocuments.toDocument(TableManager.createWithDB(dao()).getTables()))
    }
}