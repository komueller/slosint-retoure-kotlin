package com.porsche.ecom.retoure.services

import com.porsche.ecom.retoure.models.RetoureModel

interface ModelToXmlConverter {
    fun convert(retoureModels: MutableList<RetoureModel>): MutableList<String>
}
