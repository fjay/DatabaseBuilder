package org.team4u.test

import org.team4u.dbb.util.DynamicClassLoaderEngine
import org.junit.Test

class DynamicClassLoaderEngineTest : IocTest() {

    @Test
    fun load() {
        val packageName = "com.asiainfo.entity"
        val className = "DynaClass"
        val source = """
        package $packageName;
        public class $className {
            public String toString() {
                return "Hello, I am " + this.getClass().getSimpleName();
            }
        }
        """.trimIndent()
        println(source)

        val clazz = DynamicClassLoaderEngine.loadClassFromSource(packageName + "." + className, source)
        println(clazz)
    }
}