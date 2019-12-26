package com.porsche.ecom.retoure.aws

interface AwsS3 {
    fun getFile(filename: String): ByteArray
}
