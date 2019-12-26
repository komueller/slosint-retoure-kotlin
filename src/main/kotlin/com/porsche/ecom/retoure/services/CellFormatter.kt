package com.porsche.ecom.retoure.services

import org.apache.poi.ss.usermodel.Cell

interface CellFormatter {
    fun format(cell: Cell): String
}
