package com.porsche.ecom.retoure.infrastructure

class TooManyMailAttachmentsException(mailId: String) : Exception("Mail '$mailId' has more than one attachment")
