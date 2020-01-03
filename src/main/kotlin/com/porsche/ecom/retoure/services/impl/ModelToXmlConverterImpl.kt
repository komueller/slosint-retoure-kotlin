package com.porsche.ecom.retoure.services.impl

import com.porsche.ecom.retoure.models.ProductModel
import com.porsche.ecom.retoure.models.RetoureModel
import com.porsche.ecom.retoure.services.ModelToXmlConverter
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.StringWriter
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class ModelToXmlConverterImpl : ModelToXmlConverter {

    companion object {
        private const val NUMBER = "number"
        private const val PRODUCT = "product"
        private const val PRODUCTS = "products"
        private const val POSITION = "position"
        private const val AMOUNT = "amount"
        private const val RETURNS = "returns"
        private const val RETURN = "return"
        private const val CONSIGNMENT = "consignment"
        private const val DATE = "date"
        private const val VALID = "valid"
        private const val RESTOCK = "restock"
        private const val SCRAP = "scrap"
        private const val INVALID = "invalid"
        private const val INTERNAL = "internal"
        private const val REASON = "reason"
        private const val CHANNEL = "channel"
        private const val ZERO = "0"
    }

    override fun convert(retoureModels: MutableList<RetoureModel>): MutableList<String> =
        retoureModels
            .map { retoure: RetoureModel ->
                try {
                    convertModelToXml(retoure)
                } catch (e: java.lang.Exception) {
                    println("An error occurred while creating XML with returnNumber '${retoure.returnNumber}' and consignmentNumber '${retoure.consignmentNumber}': '${e.message}'")
                    e.printStackTrace()
                }
                ""
            }
            .filter { it.isNotEmpty() }.toMutableList()

    /**
     * Converts contents of a [RetoureModel] to xml.
     *
     * @param model is the instance of Retoure Model
     * @return xml as String
     * @throws Exception if products are not valid
     */
    @Throws(Exception::class)
    fun convertModelToXml(model: RetoureModel): String {
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()

        val root: Element = Xml(document, RETURNS).element
        document.appendChild(root)

        val returnNumber: Element = Xml(document, RETURN).run {
            parent(root)
            attribute(NUMBER to model.returnNumber)
        }

        Xml(document, DATE).run {
            parent(returnNumber)
            text(dateToString(model.date))
            attribute("format" to "YYYY-MM-DD")
        }

        Xml(document, CHANNEL).run {
            parent(returnNumber)
            text("B2C")
        }

        val consignmentNumber: Element = Xml(document, CONSIGNMENT).run {
            parent(returnNumber)
            attribute(NUMBER to model.consignmentNumber.toString())
        }

        val products: Element = Xml(document, PRODUCTS).run { parent(consignmentNumber) }
        addProductsXmlTo(products, document, model.products.values)

        val xml: String = convertDocumentToString(document)
        println("Parsed XML: '$xml'")
        return xml
    }

    /**
     * Converts the [ProductModel]s to xml and adds the [Element]s to the parent.
     *
     * @param parent The parent [Element] to which the product [Element]s are added
     * @param document The xml [Document]
     * @param products The products
     */
    private fun addProductsXmlTo(
        parent: Element,
        document: Document,
        products: Collection<ProductModel>
    ) {
        for (product in products) {
            val productEle: Element = Xml(document, PRODUCT).run {
                parent(parent)
                attribute(NUMBER to product.productNumber)
            }

            Xml(document, POSITION).run {
                parent(productEle)
                text(product.position.toString())
            }

            val amount: Element = Xml(document, AMOUNT).run { parent(productEle) }
            val valid: Element = Xml(document, VALID).run { parent(amount) }

            Xml(document, RESTOCK).run {
                parent(valid)
                text(product.amountValidRestock.toString())
            }

            Xml(document, SCRAP).run {
                parent(valid)
                text(ZERO)
            }

            Xml(document, INVALID).run {
                parent(amount)
                text(ZERO)
            }

            Xml(document, INTERNAL).run {
                parent(amount)
                text(ZERO)
            }

            Xml(document, REASON).run {
                parent(productEle)
                text(reasonToString(product))
            }
        }
    }

    /**
     * Converts [Document] to String.
     *
     * @param doc is the Document instance
     * @return document in String format
     */
    private fun convertDocumentToString(doc: Document): String {
        val transformer = TransformerFactory.newInstance().newTransformer()
        val writer = StringWriter()

        transformer.transform(DOMSource(doc), StreamResult(writer))
        return writer.buffer.toString()
    }

    /**
     * Converts Local Date to String.
     *
     * @param date is the local date
     * @return the date in String format
     */
    private fun dateToString(date: LocalDate): String = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

    /**
     * Converts Reason of return request to String.
     *
     * @param productModel is the instance of the product model
     * @return the reason with zero firstly in String format
     */
    private fun reasonToString(productModel: ProductModel): String = String.format("%02d", productModel.reason)
}

data class Xml(val document: Document, val tagName: String) {

    val element: Element = document.createElement(tagName)

    fun attribute(attr: Pair<String, String>): Element {
        element.setAttribute(attr.first, attr.second)
        return element
    }

    fun text(textContent: String): Element {
        element.appendChild(document.createTextNode(textContent))
        return element
    }

    fun parent(parentElement: Element): Element {
        parentElement.appendChild(element)
        return element
    }
}
