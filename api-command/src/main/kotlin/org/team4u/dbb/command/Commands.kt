package org.team4u.dbb.command

import com.alibaba.druid.pool.DruidDataSource
import com.asiainfo.common.util.Registrar
import com.asiainfo.common.util.StringUtil
import com.asiainfo.common.util.config.Configs
import org.apache.commons.cli.*
import org.nutz.lang.Files
import org.team4u.dbb.DatabaseBuilder

object Commands : Registrar<String, Commands.Command>() {

    private val HELP_KEY = "h"

    private val options = Options()

    init {
        register(Help())
        register(WithConfig())
        register(CreateTableClass())
        register(CreateTable())
        register(FillData())
        register(CreateTableDocument())
        register(CreateRecordDocument())
    }

    override fun register(value: Command): Registrar<String, Command>? {
        options.addOption(value.option)
        return super.register(value)
    }

    fun execute(args: Array<String>) {
        val context = CommandContext()

        val doHelp = {
            val help = get(HELP_KEY)
            help!!.execute(help.option, context)
        }

        try {
            val commandLine = DefaultParser().parse(options, args)
            context.commandLine = commandLine

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

    abstract class Command(val keyForCommand: String) : Registrar.Applicant<String> {

        abstract val option: Option

        override fun getKey(): String {
            return keyForCommand
        }

        abstract fun execute(option: Option, context: CommandContext)

        fun getOptionValues(context: CommandContext): List<String>? = getOptionValue(context)?.split(",")?.map {
            it.trim()
        }

        fun getOptionValue(context: CommandContext): String? = context.commandLine.getOptionValue(option.longOpt)
    }

    class CommandContext {

        var configPath = "config/config.properties"

        lateinit var commandLine: CommandLine

        val config: Config by lazy {
            Configs.getInstance().getOrLoadWithFilePath(
                    Config::class.java,
                    configPath
            )
        }

        val builder by lazy {
            val dataSource = DruidDataSource().apply {
                url = config.jdbc.url
                username = config.jdbc.username
                password = config.jdbc.password
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
        override val option: Option = Option.builder(keyForCommand)
                .argName("table,table2,...")
                .hasArg()
                .longOpt("create-table-class")
                .optionalArg(true)
                .desc("生成表对应的实体类")
                .build()

        override fun execute(option: Option, context: CommandContext) {
            context.builder.createTableClassesWithFilePath(
                    context.config.tableFilePath,
                    context.config.tableClassPackage,
                    getOptionValues(context),
                    context.config.tableClassPath,
                    if (StringUtil.isEmpty(context.config.tableClassTemplatePath)) {
                        null
                    } else {
                        Files.read(context.config.tableClassTemplatePath)
                    },
                    context.config.tableClassFileExtension
            )
        }
    }

    private class CreateTable : Command("ct") {
        override val option: Option = Option.builder(keyForCommand)
                .argName("table,table2,...")
                .hasArg()
                .longOpt("create-table")
                .optionalArg(true)
                .desc("生成指定数据库的表结构")
                .build()

        override fun execute(option: Option, context: CommandContext) {
            context.builder.createTablesWithFilePath(
                    context.config.tableFilePath,
                    context.config.tableClassPackage,
                    getOptionValues(context)
            )
        }
    }

    private class FillData : Command("fd") {
        override val option: Option = Option.builder(keyForCommand)
                .longOpt("fill-data")
                .desc("生成指定表的数据记录")
                .build()

        override fun execute(option: Option, context: CommandContext) {
            context.builder.fillDataWithFilePath(context.config.recordFilePath)
        }
    }

    private class CreateTableDocument : Command("ctd") {
        override val option: Option = Option.builder(keyForCommand)
                .argName("table,table2,...")
                .hasArg()
                .longOpt("create-table-document")
                .optionalArg(true)
                .desc("反向生成表结构文本结构")
                .build()

        override fun execute(option: Option, context: CommandContext) {
            context.builder.toTableDocument(getOptionValues(context), context.config.tableDocumentPath)
        }
    }

    private class CreateRecordDocument : Command("crd") {
        override val option: Option = Option.builder(keyForCommand)
                .argName("table,table2,...")
                .hasArg()
                .longOpt("create-record-document")
                .optionalArg(true)
                .desc("反向生成数据文本结构")
                .build()

        override fun execute(option: Option, context: CommandContext) {
            context.builder.toRecordDocument(getOptionValues(context), context.config.recordDocumentPath)
        }
    }

    private class WithConfig : Command("c") {
        override val option: Option = Option.builder(keyForCommand)
                .argName("file")
                .hasArg()
                .longOpt("config")
                .desc("指定配置文件")
                .build()

        override fun execute(option: Option, context: CommandContext) {
            context.configPath = getOptionValue(context)!!
        }
    }
}