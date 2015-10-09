package com.asiainfo.dbb.model

import java.util.*

data class Record(
        val table: Table,
        val loadMethod: Record.LoadMethod = Record.LoadMethod.INSERT,
        val data: List<Record.Data> = ArrayList()) {

    enum class LoadMethod {
        CLEAR_AND_INSERT, INSERT
    }

    data class Data(val columnData: List<ColumnData>)

    data class ColumnData(val column: Column, val value: Any?)
}