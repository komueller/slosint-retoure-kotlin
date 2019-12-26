package com.porsche.ecom.retoure

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestStreamHandler
import com.porsche.ecom.retoure.aws.AwsSns
import com.porsche.ecom.retoure.models.RetoureModel
import com.porsche.ecom.retoure.services.ExcelToModelConverter
import com.porsche.ecom.retoure.services.JsonParserService
import com.porsche.ecom.retoure.services.MailService
import com.porsche.ecom.retoure.services.ModelToXmlConverter
import org.springframework.context.support.ClassPathXmlApplicationContext
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets

class LambdaApp : RequestStreamHandler {

    companion object {
        private val appContext: ClassPathXmlApplicationContext =
            ClassPathXmlApplicationContext("applicationContext.xml")
    }

    override fun handleRequest(input: InputStream, output: OutputStream, context: Context) {
        println("Starting program")

        try {
            val messageId: String = appContext.getBean(JsonParserService::class.java).parseMessageIdFrom(input)
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
        val file: InputStream = appContext.getBean(MailService::class.java).getFirstAttachmentFromS3Mail(messageId)
        val retoureModels: MutableList<RetoureModel> =
            appContext.getBean(ExcelToModelConverter::class.java).convert(file)

        val awsSns = appContext.getBean(AwsSns::class.java)
        val xml: MutableList<String> = appContext.getBean(ModelToXmlConverter::class.java).convert(retoureModels)
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
