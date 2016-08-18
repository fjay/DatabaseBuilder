package org.team4u.test

import org.team4u.dbb.record.transformer.DataTransformer
import org.team4u.dbb.record.transformer.DataTransformers
import org.junit.Assert
import org.junit.Test

class DataTransformersTest {

    @Test
    fun dates() {
        Assert.assertEquals(
                DataTransformers.DateTransformer.now(),
                DataTransformers.execute("Dates.now()"))

        Assert.assertEquals(
                DataTransformers.DateTransformer.now("yyyy-MM-dd"),
                DataTransformers.execute("Dates.now('yyyy-MM-dd')"))
    }

    @Test
    fun extend() {
        DataTransformers.register(A)
        Assert.assertEquals(A.one(), DataTransformers.execute("A.one()"))
    }

    object A : DataTransformer {
        override fun getKey(): String {
            return "A"
        }

        @JvmStatic
        fun one(): Int {
            return 1
        }
    }
}