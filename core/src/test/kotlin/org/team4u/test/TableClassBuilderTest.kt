package org.team4u.test

import org.junit.Test
import org.nutz.lang.Files
import org.team4u.dbb.table.TableClassBuilder
import org.team4u.dbb.table.TableDocuments
import org.team4u.dbb.table.TableManager

class TableClassBuilderTest {

    @Test
    fun buildJavaFile() {
        TableClassBuilder(TableManager.Companion.createWithDocument(Files.read("tables.yml")).getTables())
                .buildJavaFile(
                        tableClassPackage = "com.asiainfo.test.entity",
                        path = "~/tmp/entity",
                        fileExtension = "java"
                )
    }

    @Test
    fun toTableDocument() {
        println(TableDocuments.toDocument(TableManager.createWithDB(TestUtil.dao).getTables()))
    }
}