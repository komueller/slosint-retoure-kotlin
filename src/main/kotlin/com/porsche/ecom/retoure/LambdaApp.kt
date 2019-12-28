package com.porsche.ecom.retoure

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestStreamHandler
import com.porsche.ecom.retoure.aws.impl.AwsS3Impl
import com.porsche.ecom.retoure.aws.impl.AwsSnsImpl
import com.porsche.ecom.retoure.models.RetoureModel
import com.porsche.ecom.retoure.services.impl.*
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets

class LambdaApp : RequestStreamHandler {

    override fun handleRequest(input: InputStream, output: OutputStream, context: Context) {
        println("Starting program")

        try {
            val messageId: String = JsonParserServiceImpl().parseMessageIdFrom(input)
            startConvert(messageId)
        } catch (e: Exception) {
            val error = "The following error occurred: ${e.message}"

            println(error)
            e.printStackTrace()
            output.write(error.toByteArray(StandardCharsets.UTF_8))
            return
        }

        val successMessage = "Execution was successful"
        println(successMessage)
        output.write(successMessage.toByteArray(StandardCharsets.UTF_8))
    }

    fun startConvert(messageId: String) {
        val file: InputStream = MailServiceImpl(AwsS3Impl()).getFirstAttachmentFromS3Mail(messageId)
        val retoureModels: MutableList<RetoureModel> =
            ExcelToModelConverterImpl(CellFormatterImpl(), ColumnInitializer()).convert(file)

        val awsSns = AwsSnsImpl()
        val xml: MutableList<String> = ModelToXmlConverterImpl().convert(retoureModels)
        println("Finished generating xml")
        xml.forEach { awsSns.publish(it) }
    }
}

fun main(args: Array<String>) = try {
    LambdaApp().startConvert(args[0])
} catch (e: Exception) {
    println("An error occurred: ${e.message}")
    e.printStackTrace()
}
