package com.asiainfo.dbb.util

import org.nutz.lang.Strings
import java.util.*

object DataTableUtil {

    inline fun each(text: String, ele: (Map<String, String>) -> Unit) {
        val result = ArrayList<Map<String, String>>()
        var headers: List<String>? = null

        text.lines().forEachIndexed { i, line ->
            if (Strings.isBlank(line)) {
                return@forEachIndexed
            }

            val values = line.split("|")
            if (i == 0) {
                headers = values
            } else {
                val map = HashMap<String, String>()
                headers?.forEachIndexed { k, header ->
                    map[header.trim()] = values[k].trim()
                }

                result.add(map)
            }
        }

        result.forEach {
            ele(it)
        }
    }

    fun <T : MutableMap<String, *>> format(data: List<T>?): String? {
        return Formatter.format(data)
    }

    object Formatter {

        private val SUFFIX_MAX_LENGTH = "MaxLength"

        fun <T : Map<String, *>> convert(list: List<T>): List<MutableMap<String, String>> {
            val result = ArrayList<MutableMap<String, String>>()

            for (map in list) {
                val newMap = LinkedHashMap<String, String>()
                for (entry in map.entries) {
                    newMap.put(entry.key, if (entry.value == null) "null" else entry.value.toString())
                }
                result.add(newMap)
            }

            return result
        }

        fun initMaxLength(list: List<MutableMap<String, String>>) {
            val keys = ArrayList(list.get(0).keys)

            for (key in keys) {
                var maxValueLength = length(key)

                for (map in list) {
                    maxValueLength = Math.max(maxValueLength, length(map.get(key)))
                }

                for (map in list) {
                    map.put(key + SUFFIX_MAX_LENGTH, maxValueLength.toString())
                }
            }
        }

        fun <T : MutableMap<String, *>> format(source: List<T>?): String? {
            if (source == null || source.isEmpty()) {
                return null
            }

            val list = convert(source)

            initMaxLength(list)

            val builder = StringBuilder()
            var first = true

            for (it in list) {
                val br = if (first) "" else "\n"
                builder.append(br)

                if (first) {
                    builder.append(formatHeader(it))
                }

                builder.append(formatBody(it))
                first = false
            }

            return builder.toString()
        }

        fun format(map: MutableMap<String, *>): String? {
            val list = ArrayList<MutableMap<String, *>>()
            list.add(map)
            return format(list)
        }

        private fun formatHeader(map: Map<String, String>): String {
            val builder = StringBuilder()

            var first = true
            var totalLength = 0

            for (it in map.keys) {
                if (it.endsWith(SUFFIX_MAX_LENGTH)) {
                    continue
                }

                val spilt = if (first) "" else "|"
                val maxLength = Integer.valueOf(map.get(it + SUFFIX_MAX_LENGTH))!!
                totalLength += (maxLength + spilt.length)

                builder.append(spilt).append(padRight(it, maxLength))
                first = false
            }

            val line = ""//multiply("-", totalLength)
            builder.append("\n").append(line)

            return builder.toString()
        }


        private fun formatBody(map: Map<String, String>): String {
            val builder = StringBuilder()

            var first = true
            for (it in map.keys) {
                if (it.endsWith(SUFFIX_MAX_LENGTH)) {
                    continue
                }

                val spilt = if (first) "" else "|"
                builder.append(spilt).append(padRight(map.get(it)!!, Integer.valueOf(map.get(it + SUFFIX_MAX_LENGTH))))
                first = false
            }

            return builder.toString()
        }

        @JvmOverloads fun padRight(self: String, numberOfChars: Number, padding: Char = ' '): String {
            val s = self.toString()
            val numChars = numberOfChars.toInt()
            val selfLength = length(s)
            if (numChars <= selfLength) {
                return s
            } else {
                return s + getPadding(padding.toString(), numChars - selfLength)
            }
        }

        private fun getPadding(padding: String, length: Int): String {
            if (padding.length < length) {
                return multiply(padding, length / padding.length + 1).substring(0, length)
            } else {
                return padding.substring(0, length)
            }
        }

        fun multiply(self: String, factor: Number): String {
            return multiply(self as CharSequence, factor)
        }

        fun multiply(self: CharSequence, factor: Number): String {
            val s = self.toString()
            val size = factor.toInt()
            if (size == 0)
                return ""
            else if (size < 0) {
                throw IllegalArgumentException("multiply() should be called with a number of 0 or greater not: " + size)
            }
            val answer = StringBuilder(s)
            for (i in 1..size - 1) {
                answer.append(s)
            }
            return answer.toString()
        }

        fun isLetter(c: Char): Boolean {
            val k = 128
            return c.toInt() / k == 0
        }

        /**
         * 得到一个字符串的长度,显示的长度,一个汉字或日韩文长度为2,英文字符长度为1
         */
        fun length(s: String?): Int {
            if (s == null)
                return 0
            val c = s.toCharArray()
            var len = 0
            for (aC in c) {
                len++
                if (!isLetter(aC)) {
                    len++
                }
            }
            return len
        }
    }
}