package com.asiainfo.dbb

import com.asiainfo.dbb.record.RecordManager
import com.asiainfo.dbb.table.TableClassBuilder
import com.asiainfo.dbb.table.TableDocumentParser
import com.asiainfo.dbb.table.TableManager
import org.nutz.dao.impl.NutDao
import org.nutz.dao.util.Daos
import org.nutz.lang.Files
import org.nutz.log.Logs
import java.io.File
import javax.sql.DataSource

class DatabaseBuilder(val dataSource: DataSource) {

    private val log = Logs.get()

    private val dao = NutDao(dataSource)

    fun createTablesWithFile(file: File, packageName: String) {
        createTablesWithContent(Files.read(file), packageName)
    }

    fun createTablesWithFilePath(filePath: String, packageName: String) {
        createTablesWithFile(Files.findFile(filePath), packageName)
    }

    fun createTablesWithContent(content: String, packageName: String) {
        val tables = TableManager.createWithDocument(dao, content).getTables()

        for (clazz in TableClassBuilder(tables).buildClassesInMemory(packageName)) {
            dao.create(clazz, true)
            log.debugf("Create table success(name=%s)", clazz.simpleName)
        }
    }

    fun createTableClassesWithFile(file: File,
                                   packageName: String,
                                   tableClassPath: String,
                                   template: String? = null) {
        createTableClassesWithContent(Files.read(file), packageName, tableClassPath, template)
    }

    fun createTableClassesWithFilePath(filePath: String,
                                       packageName: String,
                                       tableClassPath: String,
                                       template: String? = null) {
        createTableClassesWithContent(Files.read(filePath), packageName, tableClassPath, template)
    }

    fun createTableClassesWithContent(content: String,
                                      packageName: String,
                                      tableClassPath: String,
                                      template: String? = null) {
        val tables = TableManager.createWithDocument(dao, content).getTables()
        TableClassBuilder(tables).buildJavaFile(packageName, tableClassPath, template)
    }

    fun createTablesInPackage(packageName: String, dropIfExist: Boolean) {
        Daos.createTablesInPackage(dao, packageName, dropIfExist)
    }

    fun fillDataWithFile(file: File) {
        fillDataWithContent(Files.read(file))
    }

    fun fillDataWithFilePath(filePath: String) {
        fillDataWithContent(Files.read(filePath))
    }

    fun fillDataWithContent(content: String) {
        RecordManager(TableManager.createWithDB(dao), content).execute()
    }

    fun toTableDocument(): String {
        return TableDocumentParser.toDocument(TableManager.createWithDB(dao).getTables())
    }

    fun toTableDocument(filePath: String) {
        val file = File(filePath)
        Files.deleteFile(file)
        Files.write(file, toTableDocument())
        log.debugf("Write table document success(file=%s)", filePath)
    }
}