package com.porsche.ecom.retoure.services

import java.io.InputStream

interface JsonParserService {
    fun parseMessageIdFrom(sesEvent: InputStream): String
}
