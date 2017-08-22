package org.team4u.dbb.table

import org.apache.ddlutils.model.Column
import org.apache.ddlutils.model.Table

/**
 * @author Jay Wu
 */
class TableBuilder(val tables: List<Table>) {

    fun build() {

    }

    private fun convertToColModel(col: org.team4u.dbb.model.Column) {
        val column = Column().apply {

        }
    }
}