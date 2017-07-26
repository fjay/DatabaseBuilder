package org.team4u.test

import org.junit.Test
import org.team4u.dbb.DatabaseBuilder

class DatabaseBuilderTest {

    private fun databaseBuilder(): DatabaseBuilder {
        return DatabaseBuilder(TestUtil.datasource)
    }

    @Test
    fun createTablesWithFilePath() {
        val b = databaseBuilder()
        b.createTablesWithFilePath("tables.yml", "org.asiainfo.test.entity")
        println(b.toTableDocument())
        b.fillDataWithFilePath("records.yml")
    }

    @Test
    fun createTableClassesWithFilePath() {
        databaseBuilder().createTableClassesWithFilePath(
                filePath = "tables.yml",
                packageName = "org.asiainfo.test.entity",
                tableClassPath = "./tmp/entity",
                fileExtension = "java")
    }

    @Test
    fun createTablesInPackage() {
        databaseBuilder().createTablesInPackage("com.asiainfo.entity", true)
    }

    @Test
    fun fillDataWithFilePath() {
        databaseBuilder().fillDataWithFilePath("records.yml")
    }

    @Test
    fun toTableDocument() {
        println(databaseBuilder().toTableDocument())
    }

    @Test
    fun toRecordDocument() {
        val b = databaseBuilder()
        b.createTablesWithFilePath("tables.yml", "org.asiainfo.test.entity")
        b.fillDataWithFilePath("records.yml")
        println(b.toRecordDocument())
    }
}