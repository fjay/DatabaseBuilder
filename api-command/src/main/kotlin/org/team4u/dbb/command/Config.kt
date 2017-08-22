package org.team4u.dbb.command

import org.team4u.kit.core.config.Configurable


/**
 *
 *
 * @author Jay Wu
 */
class Config : Configurable {

    lateinit var tableFilePath: String

    lateinit var tableClassPackage: String

    lateinit var tableClassPath: String

    lateinit var tableClassFileExtension: String

    var tableClassTemplatePath: String? = null

    lateinit var recordFilePath: String

    lateinit var tableDocumentPath: String

    lateinit var recordDocumentPath: String

    lateinit var jdbc: Jdbc

    class Jdbc {
        lateinit var url: String
        lateinit var username: String
        lateinit var password: String
    }

    override fun getKey(): String {
        return this.javaClass.name
    }
}