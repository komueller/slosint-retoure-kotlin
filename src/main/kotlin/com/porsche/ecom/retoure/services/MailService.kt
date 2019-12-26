package com.porsche.ecom.retoure.services

import java.io.InputStream

interface MailService {
    fun getFirstAttachmentFromS3Mail(filename: String): InputStream
}
