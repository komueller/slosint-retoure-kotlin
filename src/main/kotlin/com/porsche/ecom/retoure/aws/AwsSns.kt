package com.porsche.ecom.retoure.aws

interface AwsSns {
    fun publish(retoureXml: String): Boolean
}
