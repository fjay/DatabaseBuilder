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
        register(CreateRecordDocument())
    }

    override fun register(value: Command) {
        super.register(value)

        options.addOption(value.option)
    }

    fun execute(args: Array<String>) {
        val context = CommandContext()

        val doHelp = {
            val help = get(HELP_KEY)
            help!!.execute(help.option, context)
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
                    entry.value.execute(option!!, context)
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

        abstract val option: Option

        override fun getKey(): String {
            return keyForCommand
        }

        abstract fun execute(option: Option, context: CommandContext)
    }

    private class CommandContext {

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

        val recordDocumentPath: String by map

        var builder = lazy {
            val dataSource = DruidDataSource().apply {
                url = map["jdbc.url"]
                username = map["jdbc.username"]
                password = map["jdbc.password"]
                isTestWhileIdle = false
            }

            DatabaseBuilder(dataSource)
        }
    }

    private class Help : Command(HELP_KEY) {

        val formatter = HelpFormatter().apply {
            width = 110
        }

        override val option = Option(keyForCommand, "help", false, "帮助说明").apply {
            isRequired = false
        }

        override fun execute(option: Option, context: CommandContext) {
            formatter.printHelp("dbb", options, true)
        }
    }

    private class CreateTableClass : Command("ctc") {
        override val option = Option(keyForCommand, "create-table-class",
                false, "生成表对应的实体类").apply {
            isRequired = false
        }

        override fun execute(option: Option, context: CommandContext) {
            context.builder.value.createTableClassesWithFilePath(
                    context.tableFilePath,
                    context.tableClassPackage,
                    context.tableClassPath,
                    context.tableClassTemplatePath
            )
        }
    }

    private class CreateTable : Command("ct") {
        override val option = Option(keyForCommand, "create-table",
                false, "生成指定数据库的表结构").apply {
            isRequired = false
        }

        override fun execute(option: Option, context: CommandContext) {
            context.builder.value.createTablesWithFilePath(context.tableFilePath, context.tableClassPackage)
        }
    }

    private class FillData : Command("fd") {
        override val option = Option(keyForCommand, "fill-data",
                false, "生成指定表的数据记录").apply {
            isRequired = false
        }

        override fun execute(option: Option, context: CommandContext) {
            context.builder.value.fillDataWithFilePath(context.recordFilePath)
        }
    }

    private class CreateTableDocument : Command("ctd") {
        override val option = Option(keyForCommand, "create-table-document",
                false, "反向生成表结构文本结构").apply {
            isRequired = false
        }

        override fun execute(option: Option, context: CommandContext) {
            context.builder.value.toTableDocument(context.tableDocumentPath)
        }
    }

    private class CreateRecordDocument : Command("crd") {
        override val option = Option(keyForCommand, "create-record-document",
                false, "反向生成数据文本结构").apply {
            isRequired = false
        }

        override fun execute(option: Option, context: CommandContext) {
            context.builder.value.toRecordDocument(context.recordDocumentPath)
        }
    }
}