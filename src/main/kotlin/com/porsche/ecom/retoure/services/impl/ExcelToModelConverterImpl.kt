package com.porsche.ecom.retoure.services.impl

import com.porsche.ecom.retoure.models.ProductModel
import com.porsche.ecom.retoure.models.RetoureModel
import com.porsche.ecom.retoure.services.CellFormatter
import com.porsche.ecom.retoure.services.ExcelToModelConverter
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Row
import java.io.InputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ExcelToModelConverterImpl(
    private val cellFormatter: CellFormatter,
    private val columnInitializer: ColumnInitializer
) : ExcelToModelConverter {

    companion object {
        private const val CONVERT_ROWS_WITH_REPLACEMENT: String = "1-Gutschrift"
        private const val CONVERT_ROWS_WITH_STATUS: String = "019-Ware wurde in den Bestandgebucht"

        private val REASON_CELL_NUMBER_POSITION: IntRange = 1..2
    }

    override fun convert(file: InputStream): MutableList<RetoureModel> {
        println("Start converting xls file to RetoureModel")

        val result: MutableList<RetoureModel> = mutableListOf()
        try {
            val rows: Iterator<Row> = HSSFWorkbook(file).getSheetAt(0).rowIterator()

            if (rows.hasNext()) {
                columnInitializer.initializeColumns(rows.next(), cellFormatter)
            }

            while (rows.hasNext()) {
                try {
                    parseRow(rows.next(), result)
                } catch (e: Exception) {
                    println("Skipping current row because it could not be parsed: '${e.message}'")
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            println("An error occurred while reading the xls file: '${e.message}'")
            e.printStackTrace()
        }

        println("Finished converting excel to model")
        return result
    }

    private fun parseRow(row: Row, result: MutableList<RetoureModel>) {
        if (!shouldConvertRow(row)) {
            println("Skipping row ${row.rowNum}")
            return
        }

        val returnNumber: String = parseReturnNumber(row)
        val date: LocalDate = getDateFrom(row)
        val consignmentNumber: Int = getNumberFrom(row, ExcelColumn.CONSIGNMENT_NUMBER)
        val productNumber: String = getStringFrom(row, ExcelColumn.PRODUCT_NUMBER)
        val position: Int = getNumberFrom(row, ExcelColumn.POSITION)
        val amountValidRestock: Int = getNumberFrom(row, ExcelColumn.AMOUNT_VALID_RESTOCK)
        val reason: Int = parseReason(row)

        if (result.any { it.returnNumber == returnNumber }) {
            val model: RetoureModel = result[result.size - 1]

            if (model.containsProduct(productNumber)) {
                return
            }

            val product = ProductModel(productNumber, position, amountValidRestock, reason)
            model.addProduct(product)

            println("Model with returnNumber '$returnNumber' was already added. Added Product to existing model: '$product'")
        } else {
            val model = RetoureModel(returnNumber, date, consignmentNumber)
            val product = ProductModel(productNumber, position, amountValidRestock, reason)
            model.addProduct(product)
            result.add(model)

            println("New model added: '$model'")
        }
    }

    private fun parseReturnNumber(row: Row): String {
        val returnNumber: List<String> = getStringFrom(row, ExcelColumn.RETURN_NUMBER).split("-")
        return "${returnNumber[0]}-${returnNumber[1]}"
    }

    private fun parseReason(row: Row): Int =
        getStringFrom(row, ExcelColumn.REASON).substring(REASON_CELL_NUMBER_POSITION).toInt()

    private fun shouldConvertRow(row: Row): Boolean =
        getStringFrom(row, ExcelColumn.REPLACEMENT) == CONVERT_ROWS_WITH_REPLACEMENT
                && getStringFrom(row, ExcelColumn.STATUS) == CONVERT_ROWS_WITH_STATUS

    private fun getNumberFrom(row: Row, column: ExcelColumn): Int = getStringFrom(row, column).toInt()

    private fun getDateFrom(row: Row): LocalDate {
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        return LocalDate.parse(getStringFrom(row, ExcelColumn.DATE), formatter)
    }

    private fun getStringFrom(row: Row, column: ExcelColumn): String {
        val columnNumber: Int = columnInitializer.getColumns()[column.columnName]!!
        return cellFormatter.format(row.getCell(columnNumber))
    }
}
