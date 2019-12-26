package com.porsche.ecom.retoure.services

import com.porsche.ecom.retoure.models.RetoureModel
import java.io.InputStream

interface ExcelToModelConverter {
    fun convert(file: InputStream): MutableList<RetoureModel>
}
