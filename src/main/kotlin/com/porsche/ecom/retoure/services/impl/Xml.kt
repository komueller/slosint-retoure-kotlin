package com.porsche.ecom.retoure.services.impl

import org.w3c.dom.Document
import org.w3c.dom.Element

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
