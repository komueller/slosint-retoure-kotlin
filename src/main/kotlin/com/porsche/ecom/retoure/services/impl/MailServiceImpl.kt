package com.porsche.ecom.retoure.services.impl

import com.porsche.ecom.retoure.aws.AwsS3
import com.porsche.ecom.retoure.infrastructure.MailAttachmentNotFoundException
import com.porsche.ecom.retoure.infrastructure.TooManyMailAttachmentsException
import com.porsche.ecom.retoure.services.MailService
import software.amazon.awssdk.utils.StringUtils
import java.io.ByteArrayInputStream
import java.io.FileNotFoundException
import java.io.InputStream
import javax.mail.BodyPart
import javax.mail.Multipart
import javax.mail.Part
import javax.mail.Session
import javax.mail.internet.MimeMessage

class MailServiceImpl(private val awsS3: AwsS3) : MailService {

    companion object {
        private const val S3_PREFIX = "emails/"
    }

    override fun getFirstAttachmentFromS3Mail(filename: String): InputStream {
        println("MailService: get bytes from S3 Bucket with filename '$filename'")

        val mailBytes: ByteArray = awsS3.getFile(S3_PREFIX + filename)
        if (mailBytes.isEmpty()) {
            throw FileNotFoundException("The file '$filename' could not be found on the S3 Bucket")
        }

        val attachments: MutableList<InputStream> = getAttachments(mailBytes)
        if (attachments.isEmpty()) {
            throw MailAttachmentNotFoundException(filename)
        }
        if (attachments.size > 1) {
            throw TooManyMailAttachmentsException(filename)
        }

        return attachments[0]
    }

    private fun getAttachments(mailBytes: ByteArray): MutableList<InputStream> {
        println("Getting attachments from mail")

        val session: Session = Session.getInstance(System.getProperties(), null)
        val message = MimeMessage(session, ByteArrayInputStream(mailBytes))
        val content = message.content

        val result: MutableList<InputStream> = mutableListOf()

        if (content is Multipart) {
            val multipart: Multipart = content

            for (i in 0 until multipart.count) {
                result.addAll(getAttachments(multipart.getBodyPart(i)))
            }
        }

        println("Parsed '${result.size}' attachments from mail")
        return result
    }

    private fun getAttachments(part: BodyPart): MutableList<InputStream> {
        val result: MutableList<InputStream> = mutableListOf()
        val content = part.content

        if (content is InputStream || content is String) {
            if (Part.ATTACHMENT.equals(part.disposition, true) || StringUtils.isNotBlank(part.fileName)) {
                println("MailService: Found attachment with name '${part.fileName}'")

                result.add(part.inputStream)
            }
            return result
        }

        if (content is Multipart) {
            val multipart: Multipart = content
            for (i in 0 until multipart.count) {
                result.addAll(getAttachments(multipart.getBodyPart(i)))
            }
        }
        return result
    }
}
