package com.porsche.ecom.retoure.services.impl

import com.fasterxml.jackson.databind.ObjectMapper
import com.porsche.ecom.retoure.services.JsonParserService
import java.io.InputStream

class JsonParserServiceImpl : JsonParserService {
    override fun parseMessageIdFrom(sesEvent: InputStream): String {
        println("Parsing JSON from InputStream SES event")

        val mapper = ObjectMapper()
        val event = mapper.readTree(sesEvent)

        val messageId = event.get("Records").get(0).get("ses").get("mail").get("messageId").textValue()

        println("Successfully parsed messageId '$messageId' from JSON")
        return messageId
    }
}
