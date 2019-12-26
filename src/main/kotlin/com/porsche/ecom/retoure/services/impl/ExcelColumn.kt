package com.porsche.ecom.retoure.services.impl

enum class ExcelColumn(val columnName: String) {
    RETURN_NUMBER("Vorgang"),
    DATE("Dt.REingang"),
    CONSIGNMENT_NUMBER("Auftrag (E)"),
    PRODUCT_NUMBER("Artikel (E)"),
    POSITION("Pos."),
    AMOUNT_VALID_RESTOCK("RE-Menge"),
    REASON("RE-Grund"),
    REPLACEMENT("GS / Umtausch"),
    STATUS("RE-Status")
}
