package com.asiainfo.dbb.command

import com.alibaba.druid.pool.DruidDataSource
import com.asiainfo.dbb.DatabaseBuilder
import com.asiainfo.dbb.util.Registrator
import org.apache.commons.cli.*
import org.nutz.ioc.impl.PropertiesProxy
import org.nutz.lang.Strings
import java.util.*
import kotlin.properties.get

object Commands : Registrator<String, Commands.Command>() {

    private val HELP_KEY = "h"

    private val options = Options()

    init {
        register(Help())
        register(CreateTableClass())
        register(CreateTable())
        register(FillData())
        register(CreateTableDocument())
    }

    override fun register(value: Command) {
        super.register(value)

        options.addOption(value.option)
    }

    fun execute(args: Array<String>) {
        val doHelp = {
            val help = get(HELP_KEY)
            help!!.execute(help.option)
        }

        try {
            val commandLine = DefaultParser().parse(options, args)
            var hasOption = false

            applicants.forEach { entry ->
                if (commandLine.hasOption(entry.key)) {
                    hasOption = true

                    val option = commandLine.options.find {
                        it.opt == entry.key
                    }
                    entry.value.execute(option!!)
                }
            }

            if (!hasOption) {
                doHelp()
            }
        } catch (e: ParseException) {
            doHelp()
        }
    }

    private abstract class Command(val keyForCommand: String) : Registrator.Applicant<String> {

        protected var context = lazy { Context() }

        abstract val option: Option

        override fun getKey(): String {
            return keyForCommand
        }

        abstract fun execute(option: Option)

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
                    isTestWhileIdle = false
                }
            }
        }
    }

    private class Help : Command(HELP_KEY) {

        val formatter = HelpFormatter().apply {
            width = 110
        }

        override val option = Option(keyForCommand, "help", false, "帮助说明").apply {
            isRequired = false
        }

        override fun execute(option: Option) {
            formatter.printHelp("dbb", options, true)
        }
    }

    private class CreateTableClass : Command("ctc") {
        override val option = Option(keyForCommand, "create-table-class",
                false, "生成表对应的实体类").apply {
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
        override val option = Option(keyForCommand, "create-table",
                false, "生成指定数据库的表结构").apply {
            isRequired = false
        }

        override fun execute(option: Option) {
            DatabaseBuilder(context.value.dataSource.value).createTablesWithFilePath(
                    context.value.tableFilePath, context.value.tableClassPackage
            )
        }
    }

    private class FillData : Command("fd") {
        override val option = Option(keyForCommand, "fill-data",
                false, "生成指定表的数据记录").apply {
            isRequired = false
        }

        override fun execute(option: Option) {
            DatabaseBuilder(context.value.dataSource.value)
                    .fillDataWithFilePath(context.value.recordFilePath)
        }
    }

    private class CreateTableDocument : Command("ctd") {
        override val option = Option(keyForCommand, "create-table-document",
                false, "反向生成DatabaseBuilder文本结构").apply {
            isRequired = false
        }

        override fun execute(option: Option) {
            DatabaseBuilder(context.value.dataSource.value)
                    .toTableDocument(context.value.tableDocumentPath)
        }
    }
}