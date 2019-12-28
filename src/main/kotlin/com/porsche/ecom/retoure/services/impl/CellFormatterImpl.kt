package com.porsche.ecom.retoure.services.impl

import com.porsche.ecom.retoure.services.CellFormatter
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.DataFormatter
import java.util.*

class CellFormatterImpl : CellFormatter {

    private val formatter: DataFormatter = DataFormatter(Locale.GERMANY);

    override fun format(cell: Cell): String = formatter.formatCellValue(cell)
}
