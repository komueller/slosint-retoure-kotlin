package com.porsche.ecom.retoure.services.impl

import com.porsche.ecom.retoure.services.CellFormatter
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.springframework.stereotype.Component

@Component
class ColumnInitializer {

    private lateinit var columns: Map<String, Int>

    fun initializeColumns(headers: Row, cellFormatter: CellFormatter) {
        println("Initializing columns")

        val cells: MutableList<Cell> = headers.cellIterator().asSequence().toMutableList()
        println("All headers: '$cells'")

        columns =
            ExcelColumn.values().map { cells.first { cell -> cellFormatter.format(cell) == it.columnName } }
                .map { cellFormatter.format(it) to it.columnIndex }.toMap()

        if (columns.size != ExcelColumn.values().size) {
            throw Exception("Unable to read headers of Excel file. Expected headers: '${ExcelColumn.values().map { it.columnName }.toList()}', but got '$columns'")
        }
        println("Mapping of columns successfully initialized: '$columns'")
    }

    fun getColumns(): MutableMap<String, Int> =
        if (this::columns.isInitialized) {
            columns.toMutableMap()
        } else {
            println("Columns were not initialized")
            hashMapOf()
        }
}
