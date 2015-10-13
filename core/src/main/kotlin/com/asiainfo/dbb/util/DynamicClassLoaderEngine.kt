package com.asiainfo.dbb.util

import org.nutz.log.Logs
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.OutputStream
import java.net.URI
import java.net.URL
import java.net.URLClassLoader
import java.util.*
import javax.tools.*


object DynamicClassLoaderEngine {

    private val log = Logs.get()

    private val parentClassLoader: URLClassLoader
    private lateinit var classpath: String

    init {
        parentClassLoader = this.javaClass.classLoader as URLClassLoader
        buildClassPath()
    }

    private fun buildClassPath() {
        val sb = StringBuilder()
        for (url in parentClassLoader.urLs) {
            sb.append(url.file).append(File.pathSeparator)
        }
        classpath = sb.toString()
    }

    fun loadClassFromSource(fullClassName: String, javaCode: String): Class<*>? {
        val compiler = ToolProvider.getSystemJavaCompiler()
        if (compiler == null) {
            log.info(System.getProperty("java.home"))
            throw RuntimeException("Please set JAVA_HOME first")
        }

        val diagnostics = DiagnosticCollector<JavaFileObject>()
        val fileManager = ClassFileManager(compiler.getStandardFileManager(diagnostics, null, null))
        val files = arrayListOf(CharSequenceJavaFileObject(fullClassName, javaCode))

        val options = ArrayList<String>()
        options.add("-encoding")
        options.add("UTF-8")
        options.add("-classpath")
        options.add(classpath)

        val task = compiler.getTask(null, fileManager, diagnostics, options, null, files)
        if (task.call()) {
            val dynamicClassLoader = DynamicClassLoader(this.parentClassLoader)
            return dynamicClassLoader.loadClass(fullClassName, fileManager.javaClassObject)
        } else {
            var error = ""
            for (diagnostic in diagnostics.diagnostics) {
                error += compileMessage(diagnostic)
            }

            log.error(error)
            return null
        }
    }

    private fun compileMessage(diagnostic: Diagnostic<*>): String {
        val res = StringBuffer()
        res.append("Code:[" + diagnostic.code + "]\n")
        res.append("Kind:[" + diagnostic.kind + "]\n")
        res.append("Position:[" + diagnostic.position + "]\n")
        res.append("Start Position:[" + diagnostic.startPosition + "]\n")
        res.append("End Position:[" + diagnostic.endPosition + "]\n")
        res.append("Source:[" + diagnostic.source + "]\n")
        res.append("Message:[" + diagnostic.getMessage(null) + "]\n")
        res.append("LineNumber:[" + diagnostic.lineNumber + "]\n")
        res.append("ColumnNumber:[" + diagnostic.columnNumber + "]\n")
        return res.toString()
    }

    class CharSequenceJavaFileObject(className: String, content: CharSequence) : SimpleJavaFileObject(
            URI.create("string:///" + className.replace('.', '/') + JavaFileObject.Kind.SOURCE.extension),
            JavaFileObject.Kind.SOURCE) {

        private val content: CharSequence = content

        override fun getCharContent(ignoreEncodingErrors: Boolean): CharSequence {
            return content
        }
    }

    class ClassFileManager(standardManager: StandardJavaFileManager) :
            ForwardingJavaFileManager<StandardJavaFileManager>(standardManager) {
        lateinit var javaClassObject: JavaClassObject

        override fun getJavaFileForOutput(location: JavaFileManager.Location,
                                          className: String,
                                          kind: JavaFileObject.Kind,
                                          sibling: FileObject): JavaFileObject {
            javaClassObject = JavaClassObject(className, kind)
            return javaClassObject
        }
    }

    class JavaClassObject(name: String, kind: JavaFileObject.Kind) :
            SimpleJavaFileObject(URI.create("string:///" + name.replace('.', '/') + kind.extension), kind) {
        protected val bos = ByteArrayOutputStream()
        val bytes: ByteArray
            get() {
                return bos.toByteArray()
            }

        override fun openOutputStream(): OutputStream {
            return bos
        }
    }

    class DynamicClassLoader(parent: ClassLoader) : URLClassLoader(arrayOfNulls<URL>(0), parent) {
        fun findClassByClassName(className: String): Class<*> {
            return this.findClass(className)
        }

        fun loadClass(fullName: String, jco: JavaClassObject): Class<*> {
            val classData = jco.bytes
            return this.defineClass(fullName, classData, 0, classData.size())
        }
    }
}