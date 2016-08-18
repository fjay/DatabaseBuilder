package org.team4u.dbb.table

import jetbrick.template.JetEngine
import org.nutz.lang.Files
import org.nutz.lang.Strings
import org.nutz.log.Logs
import org.team4u.dbb.model.Table
import org.team4u.dbb.util.DynamicClassLoaderEngine
import java.io.File
import java.io.StringWriter
import java.util.*

class TableClassBuilder(val tables: List<Table>) {

    private val log = Logs.get()

    private val engine = JetEngine.create()

    init {
        registerTemplateMethod(TableClassTemplateMethod::class.java)
    }

    fun registerTemplateMethod(method: Class<*>) {
        engine.globalResolver.registerMethods(method)
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

    fun buildJavaFile(tableClassPackage: String, path: String,
                      template: String? = null, fileExtension: String) {
        Files.deleteDir(File(path))
        tables.forEach {
            buildJavaFile(tableClassPackage, it, path, template ?: Companion.TABLE_CLASS_TEMPLATE, fileExtension)
        }
    }

    private fun buildJavaSource(tableClassPackage: String, table: Table, templateContent: String): String {
        val template = engine.createTemplate(templateContent)

        val sw = StringWriter()
        template.render(mapOf(
                "table" to table,
                "packageName" to tableClassPackage
        ), sw)

        return sw.toString()
    }

    private fun getClassName(tableName: String): String {
        return Strings.upperFirst(Strings.upperWord(tableName.toLowerCase(), '_'))
    }

    private fun buildClassInMemory(tableClassPackage: String, table: Table): Class<*>? {
        val source = buildJavaSource(tableClassPackage, table, Companion.TABLE_CLASS_TEMPLATE)

        val clazz = DynamicClassLoaderEngine.loadClassFromSource(tableClassPackage + "." + getClassName(table.name), source)
        if (clazz == null) {
            throw RuntimeException("LoadClassFromSource fail(source=$source)")
        } else {
            log.debug("LoadClassFromSource success(class=${clazz.name})")
        }

        return clazz
    }

    private fun buildJavaFile(tableClassPackage: String, table: Table,
                              path: String, template: String,
                              fileExtension: String) {
        val source = buildJavaSource(tableClassPackage, table, template)

        val fileName = getClassName(table.name) + "." + fileExtension
        val file = File(path + File.separator + fileName)
        Files.write(file, source)

        log.info("BuildJavaFile:${file.absolutePath}");
    }

    companion object {
        private val TABLE_CLASS_TEMPLATE = Files.read("TableClassTemplate.txt")
    }
}