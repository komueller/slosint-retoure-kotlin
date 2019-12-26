package com.porsche.ecom.retoure.models

import java.time.LocalDate

data class RetoureModel(
    val returnNumber: String,
    val date: LocalDate,
    val consignmentNumber: Int,
    val products: MutableMap<String, ProductModel> = HashMap()
) {
    fun addProduct(product: ProductModel) = products.put(product.productNumber, product)

    fun containsProduct(productNumber: String) = products.containsKey(productNumber)
}
