package com.porsche.ecom.retoure.infrastructure

class MailAttachmentNotFoundException(mailId: String) : Exception("No attachments found in mail '$mailId'")
