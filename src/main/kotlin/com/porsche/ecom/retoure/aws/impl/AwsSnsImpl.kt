package com.porsche.ecom.retoure.aws.impl

import com.porsche.ecom.retoure.aws.AwsSns
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component
import software.amazon.awssdk.core.exception.SdkClientException
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.regions.internal.util.EC2MetadataUtils
import software.amazon.awssdk.services.sns.SnsClient
import software.amazon.awssdk.services.sns.model.PublishRequest
import software.amazon.awssdk.services.sns.model.PublishResponse
import software.amazon.awssdk.services.sns.model.SnsException

@Component
class AwsSnsImpl(private val environment: Environment) : AwsSns {

    private val region: Region = try {
        Region.of(EC2MetadataUtils.getEC2InstanceRegion())
    } catch (e: SdkClientException) {
        Region.EU_WEST_1
    }

    companion object {
        private const val SNS_TOPIC_ENV_VAR = "SNS_TOPIC_ARN"
    }

    override fun publish(retoureXml: String): Boolean {
        if (retoureXml.isEmpty()) {
            println("Can't publish the retoure xml because it is null or empty")
            return false
        }

        try {
            publishMessage(retoureXml)
            return true
        } catch (e: SnsException) {
            println("A SnsException occurred: '${e.message}'")
            e.printStackTrace()
        }
        return false
    }

    private fun publishMessage(message: String) {
        getSnsClient().use {
            val result: PublishResponse =
                it.publish(PublishRequest.builder().topicArn(environment.getProperty(SNS_TOPIC_ENV_VAR)).message(message).build())

            println("Successfully published to SNS with messageId '${result.messageId()}' and message '$message'")
        }
    }

    private fun getSnsClient(): SnsClient = SnsClient.builder().region(region).build()
}
