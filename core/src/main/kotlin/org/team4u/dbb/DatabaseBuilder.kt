package org.team4u.dbb

import org.nutz.dao.impl.NutDao
import org.nutz.dao.util.Daos
import org.nutz.lang.Files
import org.nutz.log.Logs
import org.team4u.dbb.record.RecordManager
import org.team4u.dbb.table.TableClassBuilder
import org.team4u.dbb.table.TableDocuments
import org.team4u.dbb.table.TableManager
import java.io.File
import javax.sql.DataSource

class DatabaseBuilder(dataSource: DataSource) {

    private val log = Logs.get()

    private val dao = NutDao(dataSource)

    fun createTablesWithFile(file: File, packageName: String, tableNames: List<String>? = null) {
        createTablesWithContent(Files.read(file), packageName, tableNames)
    }

    fun createTablesWithFilePath(filePath: String, packageName: String, tableNames: List<String>? = null) {
        createTablesWithFile(Files.findFile(filePath), packageName, tableNames)
    }

    fun createTablesWithContent(content: String, packageName: String, tableNames: List<String>? = null) {
        val tables = TableManager.createWithDocument(content).getTables().filter {
            tableNames == null || tableNames.isEmpty() || tableNames.contains(it.name)
        }

        for (clazz in TableClassBuilder(tables).buildClassesInMemory(packageName)) {
            dao.create(clazz, true)
            log.debugf("Create table success(name=%s)", clazz.simpleName)
        }
    }

    fun createTableClassesWithFile(file: File,
                                   packageName: String,
                                   tableNames: List<String>? = null,
                                   tableClassPath: String,
                                   template: String? = null,
                                   fileExtension: String) {
        createTableClassesWithContent(Files.read(file), packageName, tableNames,
                tableClassPath, template, fileExtension)
    }

    fun createTableClassesWithFilePath(filePath: String,
                                       packageName: String,
                                       tableNames: List<String>? = null,
                                       tableClassPath: String,
                                       template: String? = null,
                                       fileExtension: String) {
        createTableClassesWithContent(Files.read(filePath), packageName, tableNames,
                tableClassPath, template, fileExtension)
    }

    fun createTableClassesWithContent(content: String,
                                      packageName: String,
                                      tableNames: List<String>? = null,
                                      tableClassPath: String,
                                      template: String? = null,
                                      fileExtension: String) {
        val tables = TableManager.createWithDocument(content).getTables().filter {
            tableNames == null || tableNames.isEmpty() || tableNames.contains(it.name)
        }
        TableClassBuilder(tables).buildJavaFile(packageName, tableClassPath, template, fileExtension)
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
        RecordManager(dao, TableManager.createWithDB(dao)).fillData(content)
    }

    fun toTableDocument(tableNames: List<String>? = null): String {
        return TableDocuments.toDocument(TableManager.createWithDB(dao).getTables(tableNames))
    }

    fun toTableDocument(tableNames: List<String>? = null, filePath: String) {
        val file = File(filePath)
        Files.deleteFile(file)
        Files.write(file, toTableDocument(tableNames))
        log.debugf("Write table document success(file=%s)", filePath)
    }

    fun toRecordDocument(tableNames: List<String>? = null): String {
        return RecordManager(dao, TableManager.createWithDB(dao)).toDocument(tableNames)
    }

    fun toRecordDocument(tableNames: List<String>? = null, filePath: String) {
        val file = File(filePath)
        Files.deleteFile(file)
        Files.write(file, toRecordDocument(tableNames))
        log.debugf("Write record document success(file=%s)", filePath)
    }
}