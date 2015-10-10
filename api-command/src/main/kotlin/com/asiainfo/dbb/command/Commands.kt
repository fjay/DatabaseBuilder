package com.asiainfo.dbb.command

import com.alibaba.druid.pool.DruidDataSource
import com.asiainfo.dbb.DatabaseBuilder
import org.apache.commons.cli.*
import org.nutz.ioc.impl.PropertiesProxy
import org.nutz.lang.Strings
import java.util.*
import kotlin.properties.get

object Commands {

    private val commandMap = LinkedHashMap<String, Command>()

    private val HELP_KEY = "h"

    private val options = Options()

    val formatter = HelpFormatter().apply {
        width = 110
    }

    init {
        register()
    }

    private fun register() {
        val add = { command: Command ->
            commandMap[command.key] = command
            options.addOption(command.option)
        }

        add(Help())
        add(CreateTableClass())
        add(CreateTable())
        add(FillData())
        add(CreateTableDocument())
    }

    fun execute(args: Array<String>) {
        val doHelp = {
            val help = commandMap[HELP_KEY]
            help!!.execute(help.option)
        }

        try {
            val commandLine = DefaultParser().parse(options, args)
            var hasOption = false

            commandMap.forEach { key, command ->
                if (commandLine.hasOption(key)) {
                    hasOption = true

                    val option = commandLine.options.find {
                        it.opt == key
                    }
                    command!!.execute(option!!)
                }
            }

            if (!hasOption) {
                doHelp()
            }
        } catch (e: ParseException) {
            doHelp()
        }
    }

    private abstract class Command(val key: String) {

        abstract fun execute(option: Option)

        abstract val option: Option

        protected var context = lazy { Context() }

        protected class Context {

            val map = PropertiesProxy("config/config.properties").let { p ->
                val map = HashMap<String, String>()

                p.keys.forEach {
                    var value = p.get(it)
                    if (!Strings.isBlank(value)) {
                        map[it] = value
                    }
                }

                map
            }

            val tableFilePath: String by map

            val tableClassPackage: String  by map

            val tableClassPath: String by map

            val tableClassTemplatePath = map["tableClassTemplatePath"]

            val recordFilePath: String  by map

            val tableDocumentPath: String by map

            val dataSource = lazy {
                DruidDataSource().apply {
                    url = map["jdbc.url"]
                    username = map["jdbc.username"]
                    password = map["jdbc.password"]
                }
            }
        }
    }

    private class Help : Command(HELP_KEY) {
        override val option = Option(key, "help", false, "帮助说明").apply {
            isRequired = false
        }

        override fun execute(option: Option) {
            formatter.printHelp("dbb", options, true)
        }
    }

    private class CreateTableClass : Command("ctc") {
        override val option = Option(key, "create-table-class", false, "生成表对应的实体类").apply {
            isRequired = false
        }

        override fun execute(option: Option) {
            DatabaseBuilder(context.value.dataSource.value).createTableClassesWithFilePath(
                    context.value.tableFilePath,
                    context.value.tableClassPackage,
                    context.value.tableClassPath,
                    context.value.tableClassTemplatePath
            )
        }
    }

    private class CreateTable : Command("ct") {
        override val option = Option(key, "create-table", false, "生成指定数据库的表结构").apply {
            isRequired = false
        }

        override fun execute(option: Option) {
            DatabaseBuilder(context.value.dataSource.value).createTablesWithFilePath(
                    context.value.tableFilePath, context.value.tableClassPackage
            )
        }
    }

    private class FillData : Command("fd") {
        override val option = Option(key, "fill-data", false, "生成指定表的数据记录").apply {
            isRequired = false
        }

        override fun execute(option: Option) {
            DatabaseBuilder(context.value.dataSource.value).fillDataWithFilePath(context.value.recordFilePath)
        }
    }

    private class CreateTableDocument : Command("ctd") {
        override val option = Option(key, "create-table-document", false, "反向生成DatabaseBuilder文本结构").apply {
            isRequired = false
        }

        override fun execute(option: Option) {
            DatabaseBuilder(context.value.dataSource.value).toTableDocument(context.value.tableDocumentPath)
        }
    }
}