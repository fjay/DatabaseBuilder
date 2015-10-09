package com.asiainfo.dbb.model

data class Table(
        val name: String,
        val columns: List<Column>,
        val primaryKey: Index?,
        val indexes: List<Index>,
        val comment: String? = null
)