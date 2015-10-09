package com.asiainfo.dbb.table

import com.asiainfo.dbb.model.Table
import com.asiainfo.dbb.util.DynamicClassLoaderEngine
import org.nutz.lang.Files
import org.nutz.lang.Strings
import org.nutz.log.Logs
import org.rythmengine.RythmEngine
import org.rythmengine.conf.RythmConfigurationKey
import java.io.File
import java.util.*

class TableClassBuilder(val tables: List<Table>) {

    private val log = Logs.get()

    private val rythm = RythmEngine(mapOf(RythmConfigurationKey.CODEGEN_COMPACT_ENABLED.key to false))

    private val defaultTemplate = Files.read("TableClassTemplate.txt")

    init {
        rythm.registerTransformer(TableClassTransformer::class.java)
    }

    fun buildClassesInMemory(tableClassPackage: String): List<Class<*>> {
        val classes = ArrayList<Class<*>>()

        tables.forEach {
            val clazz = buildClassInMemory(tableClassPackage, it)

            if (clazz != null) {
                classes.add(clazz)
            }
        }

        return classes
    }

    fun buildClassesInFile(tableClassPackage: String, path: String, template: String? = null) {
        Files.deleteDir(File(path))
        tables.forEach {
            buildClassInFile(tableClassPackage, it, path, template ?: defaultTemplate)
        }
    }

    private fun buildClassContent(tableClassPackage: String, table: Table, template: String): String {
        return rythm.render(template, mapOf(
                "table" to table,
                "packageName" to tableClassPackage
        ))
    }

    private fun getClassName(tableName: String): String {
        return Strings.upperFirst(Strings.upperWord(tableName, '_'))
    }

    private fun buildClassInMemory(tableClassPackage: String, table: Table): Class<*>? {
        val source = buildClassContent(tableClassPackage, table, defaultTemplate)

        val clazz = DynamicClassLoaderEngine.loadClassFromSource(tableClassPackage + "." + getClassName(table.name), source)
        if (clazz == null) {
            throw RuntimeException("LoadClassFromSource fail(source=$source)")
        } else {
            log.debug("LoadClassFromSource success(class=${clazz.name})")
        }

        return clazz
    }

    private fun buildClassInFile(tableClassPackage: String, table: Table, path: String, template: String) {
        val source = buildClassContent(tableClassPackage, table, template)

        val fileName = getClassName(table.name) + ".java"

        Files.write(
                path + fileName,
                source
        )
    }
}