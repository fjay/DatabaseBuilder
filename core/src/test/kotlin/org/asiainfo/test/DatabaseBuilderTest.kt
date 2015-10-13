package org.asiainfo.test

import com.asiainfo.dbb.DatabaseBuilder
import org.junit.Test

class DatabaseBuilderTest : IocTest() {

    private fun databaseBuilder(): DatabaseBuilder {
        return DatabaseBuilder(dataSource())
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
        databaseBuilder().createTableClassesWithFilePath("tables.yml", "org.asiainfo.test.entity", "")
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