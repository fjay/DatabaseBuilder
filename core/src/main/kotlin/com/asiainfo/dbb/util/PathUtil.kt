package com.asiainfo.dbb.util

import java.io.File

object PathUtil {

    var root: String = "config"

    fun normalize(path: String): String {
        return root + File.separator + path
    }
}